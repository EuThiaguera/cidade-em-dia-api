package br.com.fiap.cidadeemdia.dao;

import br.com.fiap.cidadeemdia.exception.ErroBancoException;
import br.com.fiap.cidadeemdia.model.Categoria;
import br.com.fiap.cidadeemdia.model.Morador;
import br.com.fiap.cidadeemdia.model.Ocorrencia;
import br.com.fiap.cidadeemdia.model.StatusOcorrencia;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela tb_ocorrencia com JDBC puro.
 *
 * As consultas fazem JOIN com tb_categoria e tb_morador para já trazer os nomes,
 * e uma subconsulta conta quantas confirmações cada ocorrência tem.
 */
@Repository
public class OcorrenciaDAO {

    /** Metros por grau de latitude (aproximação usada no filtro retangular). */
    private static final double METROS_POR_GRAU = 111_320;

    private static final String SELECT_BASE =
            "SELECT o.*, c.nm_categoria, c.ds_categoria, "
                    + "       m.nm_morador, m.ds_email, m.nm_subprefeitura, m.dt_cadastro, "
                    + "       (SELECT COUNT(*) FROM tb_confirmacao cf WHERE cf.cd_ocorrencia = o.cd_ocorrencia) AS qt_confirmacoes "
                    + "  FROM tb_ocorrencia o "
                    + "  JOIN tb_categoria c ON c.cd_categoria = o.cd_categoria "
                    + "  JOIN tb_morador m ON m.cd_morador = o.cd_morador ";

    private final DataSource dataSource;

    public OcorrenciaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Ocorrencia inserir(Ocorrencia o) {
        String sql = "INSERT INTO tb_ocorrencia (cd_ocorrencia, cd_categoria, cd_morador, ds_titulo, ds_ocorrencia, "
                + "nr_latitude, nr_longitude, ds_endereco, ds_url_foto, ds_status, dt_abertura) "
                + "VALUES (sq_tb_ocorrencia.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"CD_OCORRENCIA"})) {

            ps.setLong(1, o.getCategoria().getId());
            ps.setLong(2, o.getMorador().getId());
            ps.setString(3, o.getTitulo());
            ps.setString(4, o.getDescricao());
            ps.setDouble(5, o.getLatitude());
            ps.setDouble(6, o.getLongitude());
            ps.setString(7, o.getEndereco());
            ps.setString(8, o.getUrlFoto());
            ps.setString(9, o.getStatus().name());
            ps.setTimestamp(10, Timestamp.valueOf(o.getDataAbertura()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    o.setId(rs.getLong(1));
                }
            }
            return o;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao inserir ocorrência", e);
        }
    }

    /**
     * Lista ocorrências com filtros opcionais. As mais confirmadas aparecem primeiro:
     * é a "força" do problema para a gestão pública.
     */
    public List<Ocorrencia> listar(StatusOcorrencia status, Long categoriaId) {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append(" WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (status != null) {
            sql.append(" AND o.ds_status = ? ");
            parametros.add(status.name());
        }
        if (categoriaId != null) {
            sql.append(" AND o.cd_categoria = ? ");
            parametros.add(categoriaId);
        }
        sql.append(" ORDER BY qt_confirmacoes DESC, o.dt_abertura DESC");

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                return mapearLista(rs);
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao listar ocorrências", e);
        }
    }

    public Optional<Ocorrencia> buscarPorId(Long id) {
        String sql = SELECT_BASE + " WHERE o.cd_ocorrencia = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao buscar ocorrência", e);
        }
    }

    /**
     * Busca CANDIDATAS a duplicata: abertas, da mesma categoria, dentro de um "quadrado"
     * em volta do ponto. O cálculo exato da distância (Haversine) é feito depois, em Java.
     */
    public List<Ocorrencia> buscarAbertasProximas(Long categoriaId, double latitude, double longitude,
                                                  double raioMetros, Long ignorarId) {
        double deltaLat = raioMetros / METROS_POR_GRAU;
        double deltaLon = raioMetros / (METROS_POR_GRAU * Math.cos(Math.toRadians(latitude)));

        String sql = SELECT_BASE
                + " WHERE o.cd_categoria = ? "
                + "   AND o.ds_status NOT IN ('CONCLUIDA', 'DESCARTADA') "
                + "   AND o.nr_latitude BETWEEN ? AND ? "
                + "   AND o.nr_longitude BETWEEN ? AND ? "
                + "   AND o.cd_ocorrencia <> ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, categoriaId);
            ps.setDouble(2, latitude - deltaLat);
            ps.setDouble(3, latitude + deltaLat);
            ps.setDouble(4, longitude - deltaLon);
            ps.setDouble(5, longitude + deltaLon);
            ps.setLong(6, ignorarId == null ? -1 : ignorarId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapearLista(rs);
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao buscar ocorrências próximas", e);
        }
    }

    /** Atualiza os dados descritivos. O status NÃO muda aqui: só por uma Atualização. */
    public boolean atualizar(Ocorrencia o) {
        String sql = "UPDATE tb_ocorrencia SET cd_categoria = ?, ds_titulo = ?, ds_ocorrencia = ?, "
                + "nr_latitude = ?, nr_longitude = ?, ds_endereco = ?, ds_url_foto = ? WHERE cd_ocorrencia = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, o.getCategoria().getId());
            ps.setString(2, o.getTitulo());
            ps.setString(3, o.getDescricao());
            ps.setDouble(4, o.getLatitude());
            ps.setDouble(5, o.getLongitude());
            ps.setString(6, o.getEndereco());
            ps.setString(7, o.getUrlFoto());
            ps.setLong(8, o.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao atualizar ocorrência", e);
        }
    }

    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_ocorrencia WHERE cd_ocorrencia = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao remover ocorrência", e);
        }
    }

    public int contarPorCategoria(Long categoriaId) {
        return contar("SELECT COUNT(*) FROM tb_ocorrencia WHERE cd_categoria = ?", categoriaId);
    }

    public int contarPorMorador(Long moradorId) {
        return contar("SELECT COUNT(*) FROM tb_ocorrencia WHERE cd_morador = ?", moradorId);
    }

    private int contar(String sql, Long id) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao contar ocorrências", e);
        }
    }

    private List<Ocorrencia> mapearLista(ResultSet rs) throws SQLException {
        List<Ocorrencia> lista = new ArrayList<>();
        while (rs.next()) {
            lista.add(mapear(rs));
        }
        return lista;
    }

    private Ocorrencia mapear(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(
                rs.getLong("cd_categoria"),
                rs.getString("nm_categoria"),
                rs.getString("ds_categoria"));

        Morador morador = new Morador();
        morador.setId(rs.getLong("cd_morador"));
        morador.setNome(rs.getString("nm_morador"));
        morador.setEmail(rs.getString("ds_email"));
        morador.setSubprefeitura(rs.getString("nm_subprefeitura"));
        morador.setDataCadastro(rs.getTimestamp("dt_cadastro").toLocalDateTime());

        Ocorrencia o = new Ocorrencia();
        o.setId(rs.getLong("cd_ocorrencia"));
        o.setCategoria(categoria);
        o.setMorador(morador);
        o.setTitulo(rs.getString("ds_titulo"));
        o.setDescricao(rs.getString("ds_ocorrencia"));
        o.setLatitude(rs.getDouble("nr_latitude"));
        o.setLongitude(rs.getDouble("nr_longitude"));
        o.setEndereco(rs.getString("ds_endereco"));
        o.setUrlFoto(rs.getString("ds_url_foto"));
        o.setStatus(StatusOcorrencia.valueOf(rs.getString("ds_status")));
        o.setDataAbertura(rs.getTimestamp("dt_abertura").toLocalDateTime());
        o.setTotalConfirmacoes(rs.getInt("qt_confirmacoes"));
        return o;
    }
}
