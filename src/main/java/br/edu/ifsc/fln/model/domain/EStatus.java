package br.edu.ifsc.fln.model.domain;

public enum EStatus {
    ABERTA("ABERTA", "- Há possibilidade de alterar ou cancelar a ordem de serviço."),
    FECHADA("FECHADA", "- Não há possibilidade de alterar ou cancelar a ordem de serviço."),
    CANCELADA("CANCELADA", "- Não há possibilidade de alterar ou cancelar a ordem de serviço.");

    private String nome;
    private String descricao;

    private EStatus(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}
