package br.com.fiap.cidadeemdia.dao;

import br.com.fiap.cidadeemdia.exception.ErroBancoException;
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

/** Acesso à tabela tb_morador com JDBC puro. */
@Repository
public class MoradorDAO {

    private final DataSource dataSource;

    public MoradorDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Morador inserir(Morador morador) {
        String sql = "INSERT INTO tb_morador (cd_morador, nm_morador, ds_email, nr_cpf, nr_telefone, nm_subprefeitura, ds_senha, dt_cadastro) "
                + "VALUES (sq_tb_morador.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"CD_MORADOR"})) {

            ps.setString(1, morador.getNome());
            ps.setString(2, morador.getEmail());
            ps.setString(3, morador.getCpf());
            ps.setString(4, morador.getTelefone());
            ps.setString(5, morador.getSubprefeitura());
            ps.setString(6, morador.getSenha());
            ps.setTimestamp(7, Timestamp.valueOf(morador.getDataCadastro()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    morador.setId(rs.getLong(1));
                }
            }
            return morador;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao inserir morador", e);
        }
    }

    public List<Morador> listar() {
        String sql = "SELECT * FROM tb_morador ORDER BY nm_morador";
        List<Morador> lista = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao listar moradores", e);
        }
    }

    public Optional<Morador> buscarPorId(Long id) {
        String sql = "SELECT * FROM tb_morador WHERE cd_morador = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao buscar morador", e);
        }
    }

    public boolean atualizar(Morador morador) {
        String sql = "UPDATE tb_morador SET nm_morador = ?, ds_email = ?, nr_cpf = ?, nr_telefone = ?, nm_subprefeitura = ?, ds_senha = ? "
                + "WHERE cd_morador = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, morador.getNome());
            ps.setString(2, morador.getEmail());
            ps.setString(3, morador.getCpf());
            ps.setString(4, morador.getTelefone());
            ps.setString(5, morador.getSubprefeitura());
            ps.setString(6, morador.getSenha());
            ps.setLong(7, morador.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao atualizar morador", e);
        }
    }

    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_morador WHERE cd_morador = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao remover morador", e);
        }
    }

    /** Busca o morador pelo e-mail (usado no login). */
    public Optional<Morador> buscarPorEmail(String email) {
        String sql = "SELECT * FROM tb_morador WHERE LOWER(ds_email) = LOWER(?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao buscar morador por e-mail", e);
        }
    }

    /** Verifica se o CPF já pertence a outro morador. */
    public boolean existeCpf(String cpf, Long ignorarId) {
        String sql = "SELECT COUNT(*) FROM tb_morador WHERE nr_cpf = ? AND cd_morador <> ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cpf);
            ps.setLong(2, ignorarId == null ? -1 : ignorarId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao verificar CPF do morador", e);
        }
    }

    /** Verifica se o e-mail já pertence a outro morador. */
    public boolean existeEmail(String email, Long ignorarId) {
        String sql = "SELECT COUNT(*) FROM tb_morador WHERE LOWER(ds_email) = LOWER(?) AND cd_morador <> ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setLong(2, ignorarId == null ? -1 : ignorarId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao verificar e-mail do morador", e);
        }
    }

    private Morador mapear(ResultSet rs) throws SQLException {
        Morador m = new Morador();
        m.setId(rs.getLong("cd_morador"));
        m.setNome(rs.getString("nm_morador"));
        m.setEmail(rs.getString("ds_email"));
        m.setCpf(rs.getString("nr_cpf"));
        m.setTelefone(rs.getString("nr_telefone"));
        m.setSubprefeitura(rs.getString("nm_subprefeitura"));
        m.setSenha(rs.getString("ds_senha"));
        m.setDataCadastro(rs.getTimestamp("dt_cadastro").toLocalDateTime());
        return m;
    }
}
