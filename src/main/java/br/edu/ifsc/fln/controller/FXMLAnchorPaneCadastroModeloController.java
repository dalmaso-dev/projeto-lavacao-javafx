/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package br.edu.ifsc.fln.controller;

import br.edu.ifsc.fln.model.dao.ModeloDAO;
import br.edu.ifsc.fln.model.database.Database;
import br.edu.ifsc.fln.model.database.DatabaseFactory;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;

import br.edu.ifsc.fln.model.domain.ECategoria;
import br.edu.ifsc.fln.model.domain.Modelo;
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
public class FXMLAnchorPaneCadastroModeloController implements Initializable {

    @FXML
    private Button btAlterar;

    @FXML
    private Button btExcluir;

    @FXML
    private Button btInserir;

    @FXML
    private Label lbMarcaNome;

    @FXML
    private Label lbModeloCategoria;

    @FXML
    private Label lbModeloDescricao;

    @FXML
    private Label lbModeloId;

    @FXML
    private Label lbMotorCombustivel;

    @FXML
    private Label lbMotorPotencia;

    @FXML
    private TableColumn<Modelo, String> tableColumnModeloDescricao;

    @FXML
    private TableView<Modelo> tableViewModelos;

    private List<Modelo> listaModelos;
    private ObservableList<Modelo> observableListModelos;

    private final Database database = DatabaseFactory.getDatabase("mysql");
    private final Connection connection = database.conectar();
    private final ModeloDAO modeloDAO = new ModeloDAO();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        modeloDAO.setConnection(connection);
        carregarTableViewModelo();

        tableViewModelos.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selecionarItemTableViewModelos(newValue));
    }

    public void carregarTableViewModelo() {
        tableColumnModeloDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));


        try {
            listaModelos = modeloDAO.listar();
        } catch (DAOException e) {
            AlertDialog.exceptionMessage(e);
        }

        observableListModelos = FXCollections.observableArrayList(listaModelos);
        tableViewModelos.setItems(observableListModelos);
    }

    public void selecionarItemTableViewModelos(Modelo modelo) {
        if (modelo != null) {
            lbModeloId.setText(String.valueOf(modelo.getId()));
            lbModeloDescricao.setText(modelo.getDescricao());
            lbModeloCategoria.setText(modelo.getCategoria().name());
            lbMarcaNome.setText(modelo.getMarca().getNome());
            lbMotorCombustivel.setText(modelo.getMotor().getTipoCombustivel().name());
            lbMotorPotencia.setText(String.valueOf(modelo.getMotor().getPotencia()));
        } else {
            lbModeloId.setText("");
            lbModeloDescricao.setText("");
            lbModeloCategoria.setText("");
            lbMarcaNome.setText("");
            lbMotorCombustivel.setText("");
            lbMotorPotencia.setText("");
        }
    }

    @FXML
    public void handleBtInserir() throws IOException {
        Modelo modelo = new Modelo();
        modelo.setCategoria(ECategoria.PADRAO);

        boolean btConfirmarClicked = showFXMLAnchorPaneCadastroModeloDialogController(modelo);
        if (btConfirmarClicked) {
            try {
                modeloDAO.inserir(modelo);
            } catch (DAOException e) {
                AlertDialog.exceptionMessage(e);
            }
            carregarTableViewModelo();
        }
    }

    @FXML
    public void handleBtAlterar() throws IOException {
        Modelo modelo = tableViewModelos.getSelectionModel().getSelectedItem();
        if (modelo != null) {
            boolean btConfirmarClicked = showFXMLAnchorPaneCadastroModeloDialogController(modelo);
            if (btConfirmarClicked) {
                try {
                    modeloDAO.alterar(modelo);
                } catch (DAOException e) {
                    AlertDialog.exceptionMessage(e);
                }
                carregarTableViewModelo();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Esta operação requer a seleção \nde um Modelo na tabela ao lado");
            alert.show();
        }
    }

    @FXML
    public void handleBtExcluir() throws IOException {
        Modelo modelo = tableViewModelos.getSelectionModel().getSelectedItem();
        if (modelo != null) {
            try {
                modeloDAO.remover(modelo);
            } catch (DAOException e) {
                AlertDialog.exceptionMessage(e);
            }
            carregarTableViewModelo();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Esta operação requer a seleção \nde um Modelo na tabela ao lado");
            alert.show();
        }
    }

    private boolean showFXMLAnchorPaneCadastroModeloDialogController(Modelo modelo) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXMLAnchorPaneCadastroModeloController.class.getResource("/view/FXMLAnchorPaneCadastroModeloDialog.fxml"));
        AnchorPane page;
        page = (AnchorPane) loader.load();

        //criação de um estágio de diálogo (StageDialog)
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Cadastro de Serviço");
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        //enviando o obejto modelo para o controller
        FXMLAnchorPaneCadastroModeloDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setModelo(modelo);

        //apresenta o diálogo e aguarda a confirmação do usuário
        dialogStage.showAndWait();

        return controller.isBtConfirmarClicked();
    }

}
