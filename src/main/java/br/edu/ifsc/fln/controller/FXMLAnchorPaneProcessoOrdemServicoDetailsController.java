/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsc.fln.controller;

import br.edu.ifsc.fln.model.dao.*;
import br.edu.ifsc.fln.model.domain.*;
import br.edu.ifsc.fln.model.exceptions.DAOException;
import br.edu.ifsc.fln.model.exceptions.ExceptionLavacao;
import br.edu.ifsc.fln.utils.AlertDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author mpisching
 */
public class FXMLAnchorPaneProcessoOrdemServicoDetailsController implements Initializable {

    @FXML
    private TableView<ItemOS> tableViewItensOS;
    @FXML
    private TableColumn<ItemOS, Servico> tableColumnServico;
    @FXML
    private TableColumn<ItemOS, Integer> tableColumnObservacao;
    @FXML
    private TableColumn<ItemOS, Double> tableColumnValor;
    @FXML
    private TextField tfTotal;
    @FXML
    private TextField tfDesconto;
    @FXML
    private TextField lbCliente;
    @FXML
    private TextField lbVeiculo;

    private ObservableList<ItemOS> observableListItensOS;

    private Stage dialogStage;
    private boolean buttonConfirmarClicked = false;
    private OrdemServico ordemServico;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tableColumnServico.setCellValueFactory(new PropertyValueFactory<>("servico"));
        tableColumnObservacao.setCellValueFactory(new PropertyValueFactory<>("observacoes"));
        tableColumnValor.setCellValueFactory(new PropertyValueFactory<>("valorServico"));
    }

    /**
     * @return the dialogStage
     */
    public Stage getDialogStage() {
        return dialogStage;
    }

    /**
     * @param dialogStage the dialogStage to set
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setOrdemServico(OrdemServico ordemServico) {
        this.ordemServico = ordemServico;
        if (ordemServico.getNumero() != 0) {
            observableListItensOS = FXCollections.observableArrayList(this.ordemServico.getListaItemOS());
            tableViewItensOS.setItems(observableListItensOS);

            String total = "";

            try {
                total = String.format("%.2f", this.ordemServico.calcularServico());
            } catch (ExceptionLavacao e) {
                Logger.getLogger(FXMLAnchorPaneProcessoOrdemServicoDetailsController.class.getName()).log(Level.SEVERE, null, e);
            }

            tfTotal.setText(total);
            tfDesconto.setText(String.format("%.2f", this.ordemServico.getDesconto()));
            lbCliente.setText(this.ordemServico.getVeiculo().getProprietario().getNome());
            lbVeiculo.setText(this.ordemServico.getVeiculo().getModelo().getDescricao() + " - " );
        }
    }

    @FXML
    public void handleButtonAdicionar() throws ExceptionLavacao {
        Servico servico;
        ItemOS itemOS = new ItemOS();

        if (comboBoxServicos.getSelectionModel().getSelectedItem() != null) {
            servico = comboBoxServicos.getSelectionModel().getSelectedItem();

            itemOS.setServico(servico);
            itemOS.setValorServico(servico.getValor() - (ordemServico.getDesconto()/100 * servico.getValor()));
            itemOS.setObservacoes(textAreaObservacoes.getText());

            for (ItemOS item : ordemServico.getListaItemOS()) {
                if (item.getServico().getDescricao().equals(servico.getDescricao())) {
                    throw new ExceptionLavacao("Este serviço já está inserido na lista de itens da OS.");
                }
            }

            ordemServico.getListaItemOS().add(itemOS);
            observableListItensOS = FXCollections.observableArrayList(ordemServico.getListaItemOS());
            tableViewItensOS.setItems(observableListItensOS);

            try {
                ordemServico.calcularServico();
            } catch (ExceptionLavacao e) {
                throw new RuntimeException(e);
            }

            tfTotal.setText(String.format("%.2f", ordemServico.getTotal()));
        }
    }

    @FXML
    private void handleButtonConfirmar() {
        if (validarEntradaDeDados()) {
            ordemServico.setVeiculo(comboBoxVeiculos.getSelectionModel().getSelectedItem());
            ordemServico.setAgenda(datePickerAgenda.getValue());
            ordemServico.setStatus(choiceBoxStatus.getSelectionModel().getSelectedItem());
            ordemServico.setDesconto(Double.valueOf(tfDesconto.getText()));
            try {
                ordemServico.calcularServico();
            } catch (ExceptionLavacao e) {
                throw new RuntimeException(e);
            }
            buttonConfirmarClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleButtonCancelar() {
        dialogStage.close();
    }

    @FXML
    void handleTableViewMouseClicked(MouseEvent event) {
        ItemOS itemOS
                = tableViewItensOS.getSelectionModel().getSelectedItem();
        if (itemOS == null) {
            contextMenuItemRemoverItem.setDisable(true);
            contextMenuItemAtualizarObs.setDisable(true);
        } else {
            contextMenuItemAtualizarObs.setDisable(false);
            contextMenuItemRemoverItem.setDisable(false);
        }
    }

    @FXML
    private void handleContextMenuItemAtualizarObs() {
        ItemOS itemOS = tableViewItensOS.getSelectionModel().getSelectedItem();
        int index = tableViewItensOS.getSelectionModel().getSelectedIndex();

        String obsAtualizada = inputDialog(itemOS.getObservacoes());

        itemOS.setObservacoes(obsAtualizada);

        ordemServico.getListaItemOS().set(index, itemOS);
        tableViewItensOS.refresh();
    }

    private String inputDialog(String value) {
        TextInputDialog dialog = new TextInputDialog(value);
        dialog.setTitle("Entrada de dados.");
        dialog.setHeaderText("Atualização da observação de itensOS.");
        dialog.setContentText("Observação: ");

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        return result.get();
    }

    @FXML
    private void handleContextMenuItemRemoverItem() {
        int index = tableViewItensOS.getSelectionModel().getSelectedIndex();
        ordemServico.getListaItemOS().remove(index);
        observableListItensOS = FXCollections.observableArrayList(ordemServico.getListaItemOS());
        tableViewItensOS.setItems(observableListItensOS);

        try {
            ordemServico.calcularServico();
        } catch (ExceptionLavacao e) {
            throw new RuntimeException(e);
        }

        tfTotal.setText(String.format("%.2f", ordemServico.getTotal()));
    }

    @FXML
    private void onActionComboBoxClientes() {
        Cliente cliente = comboBoxClientes.getValue();
        List<Veiculo> listaVeiculos = null;
        try {
            listaVeiculos = veiculoDAO.listarPorCliente(cliente);
        } catch (DAOException e) {
            AlertDialog.exceptionMessage(e);
        }
        ObservableList<Veiculo> observableListVeiculos = FXCollections.observableArrayList(listaVeiculos);
        comboBoxVeiculos.setItems(observableListVeiculos);
        comboBoxVeiculos.setDisable(false);
    }

    @FXML
    private void onActionComboBoxVeiculos() {
        carregarComboBoxServicos();
        comboBoxServicos.setDisable(false);
    }

    @FXML
    private void onActionTextFieldDesconto() {
        String errorMessage = "";

        DecimalFormat df = new DecimalFormat("0.00");
        try {
            tfDesconto.setText(df.parse(tfDesconto.getText()).toString());
            this.ordemServico.setDesconto(Double.valueOf(tfDesconto.getText()));
            try {
                this.ordemServico.calcularServico();
            } catch (ExceptionLavacao e) {
                throw new RuntimeException(e);
            }
            tfTotal.setText(String.format("%.2f", ordemServico.getTotal()));
            tableViewItensOS.refresh();
        } catch (ParseException ex) {
            errorMessage += "A taxa de desconto está incorreta! Use \",\" como ponto decimal.\n";

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro no cadastro");
            alert.setHeaderText("Campos inválidos, por favor corrija...");
            alert.setContentText(errorMessage);
            alert.show();
        }

    }


    //validar entrada de dados do cadastro
    private boolean validarEntradaDeDados() {
        String errorMessage = "";

        if (comboBoxVeiculos.getSelectionModel().getSelectedItem() == null) {
            errorMessage += "Veículo inválido!\n";
        }

        if (datePickerAgenda.getValue() == null) {
            errorMessage += "Data inválida!\n";
        }

        if (observableListItensOS == null) {
            errorMessage += "Itens de venda inválidos!\n";
        }

        DecimalFormat df = new DecimalFormat("0.00");
        try {
            tfDesconto.setText(df.parse(tfDesconto.getText()).toString());
        } catch (ParseException ex) {
            errorMessage += "A taxa de desconto está incorreta! Use \",\" como ponto decimal.\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro no cadastro");
            alert.setHeaderText("Campos inválidos, por favor corrija...");
            alert.setContentText(errorMessage);
            alert.show();
            return false;
        }
    }
}
