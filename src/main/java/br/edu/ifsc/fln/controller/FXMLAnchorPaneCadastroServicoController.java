/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package br.edu.ifsc.fln.controller;

import br.edu.ifsc.fln.model.dao.ServicoDAO;
import br.edu.ifsc.fln.model.database.Database;
import br.edu.ifsc.fln.model.database.DatabaseFactory;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;

import br.edu.ifsc.fln.model.domain.ECategoria;
import br.edu.ifsc.fln.model.domain.Servico;
import br.edu.ifsc.fln.model.exceptions.DAOException;
import br.edu.ifsc.fln.utils.AlertDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author mpisc
 */
public class FXMLAnchorPaneCadastroServicoController implements Initializable {


    @FXML
    private Button btnAlterar;

    @FXML
    private Button btExcluir;

    @FXML
    private Button btInserir;

    @FXML
    private Label lbServicoPontos;

    @FXML
    private Label lbServicoCategoria;

    @FXML
    private Label lbServicoValor;

    @FXML
    private Label lbServicoDescricao;

    @FXML
    private Label lbServicoId;

    @FXML
    private TableColumn<Servico, String> tableColumnServicoDescricao;

    @FXML
    private TableView<Servico> tableViewServicos;

    private List<Servico> listaServicos;
    private ObservableList<Servico> observableListServicos;

    private final Database database = DatabaseFactory.getDatabase("mysql");
    private final Connection connection = database.conectar();
    private final ServicoDAO servicoDAO = new ServicoDAO();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        servicoDAO.setConnection(connection);
        carregarTableViewServico();

        tableViewServicos.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selecionarItemTableViewServicos(newValue));
    }

    public void carregarTableViewServico() {
        tableColumnServicoDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

        try {
            listaServicos = servicoDAO.listar();
        } catch (DAOException e) {
            AlertDialog.exceptionMessage(e);
        }

        observableListServicos = FXCollections.observableArrayList(listaServicos);
        tableViewServicos.setItems(observableListServicos);
    }

    public void selecionarItemTableViewServicos(Servico servico) {
        if (servico != null) {
            lbServicoId.setText(String.valueOf(servico.getId()));
            lbServicoDescricao.setText(servico.getDescricao());
            lbServicoValor.setText(String.valueOf(servico.getValor()));
            lbServicoCategoria.setText(servico.getCategoria().getTamanho());
            lbServicoPontos.setText(String.valueOf(Servico.getPontos()));
        } else {
            lbServicoId.setText("");
            lbServicoDescricao.setText("");
            lbServicoValor.setText("");
            lbServicoPontos.setText("");
        }

    }

    @FXML
    public void handleBtInserir() throws IOException {
        Servico servico = new Servico();
        if (servico.getCategoria() == null) {
            servico.setCategoria(ECategoria.PADRAO);
        }
        boolean btConfirmarClicked = showFXMLAnchorPaneCadastroServicoDialogController(servico);
        if (btConfirmarClicked) {
            try {
                servicoDAO.inserir(servico);
            } catch (DAOException e) {
                AlertDialog.exceptionMessage(e);
            }
            carregarTableViewServico();
        }
    }

    @FXML
    public void handleBtAlterar() throws IOException {
        Servico servico = tableViewServicos.getSelectionModel().getSelectedItem();
        if (servico != null) {
            boolean btConfirmarClicked = showFXMLAnchorPaneCadastroServicoDialogController(servico);
            if (btConfirmarClicked) {
                try {
                    servicoDAO.alterar(servico);
                } catch (DAOException e) {
                    throw new RuntimeException(e);
                }
                carregarTableViewServico();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Esta operação requer a seleção \nde uma Serviço na tabela ao lado");
            alert.show();
        }
    }

    @FXML
    public void handleBtExcluir() throws IOException {
        Servico servico = tableViewServicos.getSelectionModel().getSelectedItem();
        if (servico != null) {
            try {
                servicoDAO.remover(servico);
            }  catch (DAOException e) {
                AlertDialog.exceptionMessage(e);
            }

            carregarTableViewServico();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Esta operação requer a seleção \nde uma Serviço na tabela ao lado");
            alert.show();
        }
    }

    private boolean showFXMLAnchorPaneCadastroServicoDialogController(Servico servico) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXMLAnchorPaneCadastroServicoController.class.getResource("/view/FXMLAnchorPaneCadastroServicoDialog.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        //criação de um estágio de diálogo (StageDialog)
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Cadastro de Serviço");
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        //enviando o obejto servico para o controller
        FXMLAnchorPaneCadastroServicoDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setServico(servico);

        //apresenta o diálogo e aguarda a confirmação do usuário
        dialogStage.showAndWait();

        return controller.isBtConfirmarClicked();
    }

}
