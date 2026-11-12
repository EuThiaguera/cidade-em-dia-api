package br.com.fiap.cidadeemdia.dao;

import br.com.fiap.cidadeemdia.exception.ErroBancoException;
import br.com.fiap.cidadeemdia.model.Categoria;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela tb_categoria com JDBC puro (PreparedStatement).
 * O DataSource (pool de conexões configurado pelo Spring) chega pelo construtor.
 */
@Repository
public class CategoriaDAO {

    private final DataSource dataSource;

    public CategoriaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Categoria inserir(Categoria categoria) {
        String sql = "INSERT INTO tb_categoria (cd_categoria, nm_categoria, ds_categoria) "
                + "VALUES (sq_tb_categoria.NEXTVAL, ?, ?)";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"CD_CATEGORIA"})) {

            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescricao());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    categoria.setId(rs.getLong(1));
                }
            }
            return categoria;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao inserir categoria", e);
        }
    }

    public List<Categoria> listar() {
        String sql = "SELECT * FROM tb_categoria ORDER BY nm_categoria";
        List<Categoria> lista = new ArrayList<>();

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao listar categorias", e);
        }
    }

    public Optional<Categoria> buscarPorId(Long id) {
        String sql = "SELECT * FROM tb_categoria WHERE cd_categoria = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao buscar categoria", e);
        }
    }

    public boolean atualizar(Categoria categoria) {
        String sql = "UPDATE tb_categoria SET nm_categoria = ?, ds_categoria = ? WHERE cd_categoria = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, categoria.getNome());
            ps.setString(2, categoria.getDescricao());
            ps.setLong(3, categoria.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao atualizar categoria", e);
        }
    }

    public boolean remover(Long id) {
        String sql = "DELETE FROM tb_categoria WHERE cd_categoria = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao remover categoria", e);
        }
    }

    /** Verifica se já existe outra categoria com o mesmo nome (ignorando maiúsculas). */
    public boolean existeNome(String nome, Long ignorarId) {
        String sql = "SELECT COUNT(*) FROM tb_categoria WHERE UPPER(nm_categoria) = UPPER(?) AND cd_categoria <> ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nome);
            ps.setLong(2, ignorarId == null ? -1 : ignorarId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao verificar nome da categoria", e);
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        return new Categoria(
                rs.getLong("cd_categoria"),
                rs.getString("nm_categoria"),
                rs.getString("ds_categoria"));
    }
}
