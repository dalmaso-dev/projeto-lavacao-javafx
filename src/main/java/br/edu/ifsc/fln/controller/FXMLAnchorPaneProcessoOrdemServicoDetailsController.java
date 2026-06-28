/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsc.fln.controller;

import br.edu.ifsc.fln.model.dao.*;
import br.edu.ifsc.fln.model.domain.*;
import br.edu.ifsc.fln.model.exceptions.ExceptionLavacao;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
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
    private TableColumn<ItemOS, String> tableColumnServico;
    @FXML
    private TableColumn<ItemOS, Integer> tableColumnObservacao;
    @FXML
    private TableColumn<ItemOS, Double> tableColumnValor;
    @FXML
    private TextField tfTotal;
    @FXML
    private TextField tfDesconto;
    @FXML
    private Label lbCliente;
    @FXML
    private Label lbVeiculo;
    @FXML
    private Label lbStatus;
    @FXML
    private Label lbAgenda;

    private ObservableList<ItemOS> observableListItensOS;

    private Stage dialogStage;
    private OrdemServico ordemServico;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        tableColumnServico.setCellValueFactory(cellData -> {
            ItemOS item = cellData.getValue();

            return new SimpleStringProperty(item.getServico().getDescricao());
        });
        tableColumnObservacao.setCellValueFactory(new PropertyValueFactory<>("observacoes"));
        tableColumnValor.setCellValueFactory(new PropertyValueFactory<>("valorServico"));
    }

    /**
     * @return the dialogStage
     */
    public Stage getDialogStage() {
        return dialogStage;
    }

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
            lbVeiculo.setText(this.ordemServico.getVeiculo().getModelo().getDescricao() + " - " + this.ordemServico.getVeiculo().getPlaca());
            lbStatus.setText(this.ordemServico.getStatus().name());
            lbAgenda.setText(this.ordemServico.getAgenda().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
    }
}
