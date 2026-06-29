package br.edu.ifsc.fln.model.domain;

public class Veiculo implements IDados{
    private int id;
    private String placa;
    private String observacoes;
    //associação unidirecional com Modelo
    private Modelo modelo;
    //associação unidirecional com Cor
    private Cor cor;
    //associação bidirecional com Cliente
    private Cliente proprietario;

    @Override
    public String getDados() {
        StringBuilder dados = new StringBuilder();
        dados.append("Placa: " + placa + "\n");
        dados.append("Modelo: " + modelo.getDescricao() + "\n");
        dados.append("Marca: " + modelo.getMarca().getNome() + "\n");
        dados.append("Categoria: "  + modelo.getCategoria() + "\n");
        dados.append("Potência do motor: " + modelo.getMotor().getPotencia());

        return dados.toString();
    }

    @Override
    public String getDados(String observacao) {
        return "";
    }

    //MÉTODOS CONSTRUTORES
    public Veiculo () {}

    public Veiculo (String placa) {
        this.placa = placa;
    }

    public Veiculo (String placa, Modelo modelo) {
        this.placa = placa;
        this.modelo = modelo;
    }

    public Veiculo(int id, String placa, String observacoes, Modelo modelo, Cor cor) {
        this.id = id;
        this.placa = placa;
        this.observacoes = observacoes;
        this.modelo = modelo;
        this.cor = cor;
    }

    //GETTERS
    public int getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public Modelo getModelo() {
        return modelo;
    }

    public Cor getCor() {
        return cor;
    }

    public Cliente getProprietario() {
        return proprietario;
    }

    //SETTERS
    public void setId(int id) {
        this.id = id;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public void setModelo(Modelo modelo) {
        this.modelo = modelo;
    }

    public void setCor(Cor cor) {
        this.cor = cor;
    }

    public void setCliente(Cliente proprietario) {
        this.proprietario = proprietario;
        this.proprietario.getVeiculos().add(this);
    }

    @Override
    public String toString() {
        return "Veiculo{" +
                "placa='" + placa + '\'' +
                ", observacoes='" + observacoes + '\'' +
                ", modelo=" + modelo +
                ", cor=" + cor.getNome() +
                ", proprietario=" + proprietario.getNome() +
                '}';
    }
}
