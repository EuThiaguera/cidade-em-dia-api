package br.com.fiap.cidadeemdia.model;

import java.time.LocalDateTime;

/**
 * Cidadão que usa a plataforma: registra ocorrências e confirma as de outros moradores.
 * Um morador possui várias ocorrências e várias confirmações (relacionamentos 1:N).
 */
public class Morador {

    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private String subprefeitura;
    private String senha;
    private LocalDateTime dataCadastro;

    public Morador() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getSubprefeitura() { return subprefeitura; }
    public void setSubprefeitura(String subprefeitura) { this.subprefeitura = subprefeitura; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }
}
