package br.com.fiap.cidadeemdia.dao;

import br.com.fiap.cidadeemdia.exception.ErroBancoException;
import br.com.fiap.cidadeemdia.model.Atualizacao;
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
 * Acesso à tabela tb_atualizacao com JDBC puro.
 *
 * Registrar ou desfazer uma atualização mexe em DUAS tabelas (tb_atualizacao e o status em
 * tb_ocorrencia). Por isso esses métodos usam uma TRANSAÇÃO manual: ou grava tudo (commit)
 * ou não grava nada (rollback).
 */
@Repository
public class AtualizacaoDAO {

    private final DataSource dataSource;

    public AtualizacaoDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Insere a atualização E muda o status da ocorrência, na mesma transação. */
    public Atualizacao inserirMudandoStatus(Atualizacao a) {
        String sqlInsert = "INSERT INTO tb_atualizacao (cd_atualizacao, cd_ocorrencia, ds_status_anterior, "
                + "ds_status_novo, ds_atualizacao, nm_responsavel, ds_url_evidencia, dt_atualizacao) "
                + "VALUES (sq_tb_atualizacao.NEXTVAL, ?, ?, ?, ?, ?, ?, ?)";
        String sqlStatus = "UPDATE tb_ocorrencia SET ds_status = ? WHERE cd_ocorrencia = ?";

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert, new String[]{"CD_ATUALIZACAO"});
                 PreparedStatement psStatus = con.prepareStatement(sqlStatus)) {

                psInsert.setLong(1, a.getOcorrenciaId());
                psInsert.setString(2, a.getStatusAnterior().name());
                psInsert.setString(3, a.getStatusNovo().name());
                psInsert.setString(4, a.getDescricao());
                psInsert.setString(5, a.getResponsavel());
                psInsert.setString(6, a.getUrlEvidencia());
                psInsert.setTimestamp(7, Timestamp.valueOf(a.getDataAtualizacao()));
                psInsert.executeUpdate();

                try (ResultSet rs = psInsert.getGeneratedKeys()) {
                    if (rs.next()) {
                        a.setId(rs.getLong(1));
                    }
                }

                psStatus.setString(1, a.getStatusNovo().name());
                psStatus.setLong(2, a.getOcorrenciaId());
                psStatus.executeUpdate();

                con.commit();
                return a;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao registrar atualização", e);
        }
    }

    /** Remove a atualização E devolve a ocorrência ao status anterior, na mesma transação. */
    public void removerRevertendoStatus(Atualizacao a) {
        String sqlDelete = "DELETE FROM tb_atualizacao WHERE cd_atualizacao = ?";
        String sqlStatus = "UPDATE tb_ocorrencia SET ds_status = ? WHERE cd_ocorrencia = ?";

        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement psDelete = con.prepareStatement(sqlDelete);
                 PreparedStatement psStatus = con.prepareStatement(sqlStatus)) {

                psDelete.setLong(1, a.getId());
                psDelete.executeUpdate();

                psStatus.setString(1, a.getStatusAnterior().name());
                psStatus.setLong(2, a.getOcorrenciaId());
                psStatus.executeUpdate();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao remover atualização", e);
        }
    }

    public List<Atualizacao> listarPorOcorrencia(Long ocorrenciaId) {
        String sql = "SELECT * FROM tb_atualizacao WHERE cd_ocorrencia = ? ORDER BY dt_atualizacao, cd_atualizacao";
        List<Atualizacao> lista = new ArrayList<>();

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
            throw new ErroBancoException("Erro ao listar atualizações", e);
        }
    }

    public Optional<Atualizacao> buscarPorId(Long id) {
        String sql = "SELECT * FROM tb_atualizacao WHERE cd_atualizacao = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao buscar atualização", e);
        }
    }

    /** Id da atualização mais recente de uma ocorrência (a única que pode ser desfeita). */
    public Optional<Long> buscarIdDaUltima(Long ocorrenciaId) {
        String sql = "SELECT MAX(cd_atualizacao) FROM tb_atualizacao WHERE cd_ocorrencia = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, ocorrenciaId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                long id = rs.getLong(1);
                return rs.wasNull() ? Optional.empty() : Optional.of(id);
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao buscar última atualização", e);
        }
    }

    /** Edita só os textos. A transição de status registrada não muda (é histórico). */
    public boolean atualizar(Atualizacao a) {
        String sql = "UPDATE tb_atualizacao SET ds_atualizacao = ?, nm_responsavel = ?, ds_url_evidencia = ? "
                + "WHERE cd_atualizacao = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, a.getDescricao());
            ps.setString(2, a.getResponsavel());
            ps.setString(3, a.getUrlEvidencia());
            ps.setLong(4, a.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao atualizar atualização", e);
        }
    }

    public int contarPorOcorrencia(Long ocorrenciaId) {
        String sql = "SELECT COUNT(*) FROM tb_atualizacao WHERE cd_ocorrencia = ?";

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, ocorrenciaId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new ErroBancoException("Erro ao contar atualizações", e);
        }
    }

    private Atualizacao mapear(ResultSet rs) throws SQLException {
        Atualizacao a = new Atualizacao();
        a.setId(rs.getLong("cd_atualizacao"));
        a.setOcorrenciaId(rs.getLong("cd_ocorrencia"));
        a.setStatusAnterior(StatusOcorrencia.valueOf(rs.getString("ds_status_anterior")));
        a.setStatusNovo(StatusOcorrencia.valueOf(rs.getString("ds_status_novo")));
        a.setDescricao(rs.getString("ds_atualizacao"));
        a.setResponsavel(rs.getString("nm_responsavel"));
        a.setUrlEvidencia(rs.getString("ds_url_evidencia"));
        a.setDataAtualizacao(rs.getTimestamp("dt_atualizacao").toLocalDateTime());
        return a;
    }
}
