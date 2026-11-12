package br.com.fiap.cidadeemdia.dao;

import br.com.fiap.cidadeemdia.exception.ErroBancoException;
import br.com.fiap.cidadeemdia.model.Confirmacao;
import br.com.fiap.cidadeemdia.model.Morador;
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

/** Acesso à tabela tb_confirmacao com JDBC puro (JOIN com tb_morador para trazer o nome). */
@Repository
public class ConfirmacaoDAO {

    private static final String SELECT_BASE =
            "SELECT cf.*, m.nm_morador, m.ds_email, m.nm_subprefeitura, m.dt_cadastro "
                    + "  FROM tb_confirmacao cf "
                    + "  JOIN tb_morador m ON m.cd_morador = cf.cd_morador ";

    private final DataSource dataSource;

    public ConfirmacaoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Confirmacao inserir(Confirmacao c) {
        String sql = "INSERT INTO tb_confirmacao (cd_confirmacao, cd_ocorrencia, cd_morador, ds_comentario, dt_confirmacao) "
                + "VALUES (sq_tb_confirmacao.NEXTVAL, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"CD_CONFIRMACAO"})) {

            ps.setLong(1, c.getOcorrenciaId());
            ps.setLong(2, c.getMorador().getId());
            ps.setString(3, c.getComentario());
            ps.setTimestamp(4, Timestamp.valueOf(c.getDataConfirmacao()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getLong(1));
                }
            }
            return c;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao inserir confirmação", e);
        }
    }

    public List<Confirmacao> listarPorOcorrencia(Long ocorrenciaId) {
        String sql = SELECT_BASE + " WHERE cf.cd_ocorrencia = ? ORDER BY cf.dt_confirmacao";
        List<Confirmacao> lista = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, ocorrenciaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao listar confirmações", e);
        }
    }

    public Optional<Confirmacao> buscarPorId(Long id) {
        String sql = SELECT_BASE + " WHERE cf.cd_confirmacao = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao buscar confirmação", e);
        }
    }

    /** Só o comentário pode ser editado: quem confirmou e em qual ocorrência não muda. */
    public boolean atualizar(Confirmacao c) {
        String sql = "UPDATE tb_confirmacao SET ds_comentario = ? WHERE cd_confirmacao = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getComentario());
            ps.setLong(2, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao atualizar confirmação", e);
        }
    }

    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_confirmacao WHERE cd_confirmacao = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao remover confirmação", e);
        }
    }

    /** O morador já confirmou esta ocorrência? */
    public boolean existe(Long ocorrenciaId, Long moradorId) {
        String sql = "SELECT COUNT(*) FROM tb_confirmacao WHERE cd_ocorrencia = ? AND cd_morador = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, ocorrenciaId);
            ps.setLong(2, moradorId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao verificar confirmação", e);
        }
    }

    public int contarPorOcorrencia(Long ocorrenciaId) {
        return contar("SELECT COUNT(*) FROM tb_confirmacao WHERE cd_ocorrencia = ?", ocorrenciaId);
    }

    public int contarPorMorador(Long moradorId) {
        return contar("SELECT COUNT(*) FROM tb_confirmacao WHERE cd_morador = ?", moradorId);
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
            throw new ErroBancoException("Erro ao contar confirmações", e);
        }
    }

    private Confirmacao mapear(ResultSet rs) throws SQLException {
        Morador morador = new Morador();
        morador.setId(rs.getLong("cd_morador"));
        morador.setNome(rs.getString("nm_morador"));
        morador.setEmail(rs.getString("ds_email"));
        morador.setSubprefeitura(rs.getString("nm_subprefeitura"));
        morador.setDataCadastro(rs.getTimestamp("dt_cadastro").toLocalDateTime());

        Confirmacao c = new Confirmacao();
        c.setId(rs.getLong("cd_confirmacao"));
        c.setOcorrenciaId(rs.getLong("cd_ocorrencia"));
        c.setMorador(morador);
        c.setComentario(rs.getString("ds_comentario"));
        c.setDataConfirmacao(rs.getTimestamp("dt_confirmacao").toLocalDateTime());
        return c;
    }
}
