/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package br.edu.ifsc.fln.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.ResourceBundle;

import br.edu.ifsc.fln.model.dao.ServicoDAO;
import br.edu.ifsc.fln.model.database.Database;
import br.edu.ifsc.fln.model.database.DatabaseFactory;
import br.edu.ifsc.fln.model.domain.Servico;
import br.edu.ifsc.fln.model.exceptions.DAOException;
import br.edu.ifsc.fln.utils.AlertDialog;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author mpisc
 */
public class FXMLAnchorPaneAjusteServicoPontosController implements Initializable {

    @FXML
    private Button btAlterar;

    @FXML
    private TextField tfPontos;

    private final Database database = DatabaseFactory.getDatabase("mysql");
    private final Connection connection = database.conectar();
    private final ServicoDAO servicoDAO = new ServicoDAO();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        servicoDAO.setConnection(connection);
        int pontos = 0;
        try {
            pontos = servicoDAO.buscarPontos();
        } catch (DAOException e) {
            AlertDialog.exceptionMessage(e);
        }

        tfPontos.setText(String.valueOf(pontos));
    }

    @FXML
    public void handleBtAlterar () throws IOException {
        if (validarEntradaDeDados()) {
            Servico.setPontos(Integer.parseInt(tfPontos.getText()));
            try {
                servicoDAO.alterarPontos();
            } catch (DAOException e) {
                AlertDialog.exceptionMessage(e);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Status de atualização de pontos de serviço.");
            alert.setHeaderText("Sucesso");
            alert.setContentText("Sua solicitação para alteração de pontos de serviço foi realizada com sucesso!");
            alert.showAndWait();
        }
    }

    //método para validar a entrada de dados
    private boolean validarEntradaDeDados() throws IOException {
        String errorMessage = "";
        if ((this.tfPontos.getText().isEmpty()) || (Integer.parseInt(this.tfPontos.getText())) < 0) {
            errorMessage += "Descrição inválida.\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            //exibindo uma mensagem de erro
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro no cadastro");
            alert.setHeaderText("Corrija o campo inválido!");
            alert.setContentText(errorMessage);
            alert.show();
            return false;
        }
    }

}
