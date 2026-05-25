package br.edu.ifsc.fln.model.domain;

import br.edu.ifsc.fln.model.exceptions.ExceptionLavacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrdemServico {
    private long numero;
    private double total;
    private LocalDate agenda;
    private double desconto = 0;

    private EStatus status = EStatus.ABERTA;

    private Veiculo veiculo;

    private List<ItemOS> listaItemOS = new ArrayList<>();

    public OrdemServico() {}

    public OrdemServico(long numero, LocalDate agenda, Veiculo veiculo) {
        this.numero = numero;
        this.agenda = agenda;
        this.veiculo = veiculo;
    }

    public void add(ItemOS itemOS) throws ExceptionLavacao {
        //verificando se a categoria do serviço é compatível com o veículo
        if (!this.getVeiculo().getModelo().getCategoria().equals(itemOS.getServico().getCategoria())) {
            throw new ExceptionLavacao
                    ("O serviço a ser adicionado não é compatível com a categoria do seu veículo");
        }
        else {
            //verificando se o itemOS já existe na ordem de serviço
            for (ItemOS item : listaItemOS) {
                if (item.getServico().equals(itemOS.getServico())) {
                    throw new ExceptionLavacao("Este item já está vinculado a ordem de serviço");
                }
            }
            listaItemOS.add(itemOS);
        }
    }

    public void remove(ItemOS itemOS) {
        listaItemOS.remove(itemOS);
    }

    public double calcularServico() throws ExceptionLavacao {
        total = 0;
        if (listaItemOS.isEmpty()) {
            throw new ExceptionLavacao
                    ("Nenhum item encontrado, portando não há serviços na lista para serem calculados na " +
                            "Ordem de Serviço");
        }
        else {
            for(ItemOS itemOS : listaItemOS) {
                total += itemOS.getValorServico();
            }
            //verificando se há desconto para não calculá-lo sem necessidade
            if (desconto > 0) {
                total -= (desconto/100) * total;
            }
        }

        return total;
    }

    //getters
    public long getNumero() {
        return numero;
    }

    public double getTotal() {
        return total;
    }

    public LocalDate getAgenda() {
        return agenda;
    }

    public double getDesconto() {
        return desconto;
    }

    public EStatus getStatus() {
        return status;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public List<ItemOS> getListaItemOS() {
        return listaItemOS;
    }

    //setters
    public void setDesconto(double desconto) {
        this.desconto = desconto;
    }

    public void setAgenda(LocalDate agenda) {
        this.agenda = agenda;
    }

    public void setStatus(EStatus status) {
        this.status = status;
    }

    public void setNumero(long numero) {
        this.numero = numero;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }
}
