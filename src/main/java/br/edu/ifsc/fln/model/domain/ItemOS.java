package br.edu.ifsc.fln.model.domain;

public class ItemOS {
    private double valorServico;
    private String observacoes;

    private Servico servico;

    public ItemOS() {}

    public ItemOS(String observacoes, Servico servico, double valorServico) {
        this.valorServico = valorServico;
        this.observacoes = observacoes;
        this.servico = servico;
    }

    public ItemOS(Servico servico,  double valorServico) {
        this.valorServico = valorServico;
        this.servico = servico;
    }

    public ItemOS(String observacoes, Servico servico) {
        this.valorServico = servico.getValor();
        this.observacoes = observacoes;
        this.servico = servico;
    }

    public ItemOS(Servico servico) {
        this.valorServico = servico.getValor();
        this.servico = servico;
    }

    public double getValorServico() {
        return valorServico;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public Servico getServico() {
        return servico;
    }

    public void setValorServico(double valorServico) {
        this.valorServico = valorServico;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }
}
