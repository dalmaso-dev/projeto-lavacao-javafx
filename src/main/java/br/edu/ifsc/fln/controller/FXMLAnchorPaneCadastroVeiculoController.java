/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package br.edu.ifsc.fln.controller;

import br.edu.ifsc.fln.model.dao.VeiculoDAO;
import br.edu.ifsc.fln.model.database.Database;
import br.edu.ifsc.fln.model.database.DatabaseFactory;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;

import br.edu.ifsc.fln.model.domain.*;
import br.edu.ifsc.fln.model.exceptions.DAOException;
import br.edu.ifsc.fln.utils.AlertDialog;
import javafx.beans.property.SimpleStringProperty;
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
public class FXMLAnchorPaneCadastroVeiculoController implements Initializable {

    @FXML
    private Button btAlterar;

    @FXML
    private Button btExcluir;

    @FXML
    private Button btInserir;

    @FXML
    private Label lbVeiculoId;

    @FXML
    private Label lbVeiculoPlaca;

    @FXML
    private Label lbVeiculoObservacoes;

    @FXML
    private Label lbVeiculoCor;

    @FXML
    private Label lbVeiculoProprietario;

    @FXML
    private Label lbModeloDescricao;

    @FXML
    private Label lbModeloCategoria;

    @FXML
    private Label lbMarcaNome;

    @FXML
    private Label lbMotorCombustivel;

    @FXML
    private Label lbMotorPotencia;

    @FXML
    private TableColumn<Veiculo, String> tableColumnVeiculoPlaca;

    @FXML
    private TableColumn<Veiculo, String> tableColumnModeloDescricao;

    @FXML
    private TableColumn<Veiculo, String> tableColumnMarcaNome;

    @FXML
    private TableView<Veiculo> tableViewVeiculos;

    private List<Veiculo> listaVeiculos;
    private ObservableList<Veiculo> observableListVeiculos;

    private final Database database = DatabaseFactory.getDatabase("mysql");
    private final Connection connection = database.conectar();
    private final VeiculoDAO veiculoDAO = new VeiculoDAO();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        veiculoDAO.setConnection(connection);
        carregarTableViewVeiculo();

        tableViewVeiculos.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selecionarItemTableViewVeiculos(newValue));
    }

    public void carregarTableViewVeiculo() {
        tableColumnVeiculoPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));

        tableColumnModeloDescricao.setCellValueFactory(cellData -> {
            Veiculo veiculo = cellData.getValue();
            String descModelo = veiculo.getModelo().getDescricao();

            return new SimpleStringProperty(descModelo);
        });

        tableColumnMarcaNome.setCellValueFactory(cellData -> {
            Veiculo veiculo = cellData.getValue();
            String nomeMarca = veiculo.getModelo().getMarca().getNome();

            return new SimpleStringProperty(nomeMarca);
        });

        try {
            listaVeiculos = veiculoDAO.listar();
        } catch (DAOException e) {
            AlertDialog.exceptionMessage(e);
        }

        observableListVeiculos = FXCollections.observableArrayList(listaVeiculos);
        tableViewVeiculos.setItems(observableListVeiculos);
    }

    public void selecionarItemTableViewVeiculos(Veiculo veiculo) {
        if (veiculo != null) {
            lbVeiculoId.setText(String.valueOf(veiculo.getId()));
            lbVeiculoPlaca.setText(veiculo.getPlaca());
            lbVeiculoObservacoes.setText(veiculo.getObservacoes());
            lbVeiculoCor.setText(veiculo.getCor().getNome());
            lbVeiculoProprietario.setText(veiculo.getProprietario().getNome());
            lbModeloDescricao.setText(veiculo.getModelo().getDescricao());
            lbModeloCategoria.setText(veiculo.getModelo().getCategoria().name());
            lbMarcaNome.setText(veiculo.getModelo().getMarca().getNome());
            lbMotorCombustivel.setText(veiculo.getModelo().getMotor().getTipoCombustivel().name());
            lbMotorPotencia.setText(String.valueOf(veiculo.getModelo().getMotor().getPotencia()));
        } else {
            lbVeiculoId.setText("");
            lbVeiculoPlaca.setText("");
            lbVeiculoObservacoes.setText("");
            lbVeiculoCor.setText("");
            lbVeiculoProprietario.setText("");
            lbModeloDescricao.setText("");
            lbModeloCategoria.setText("");
            lbMarcaNome.setText("");
            lbMotorCombustivel.setText("");
            lbMotorPotencia.setText("");
        }
    }

    @FXML
    public void handleBtInserir() throws IOException {
        Veiculo veiculo = new Veiculo();

        boolean btConfirmarClicked = showFXMLAnchorPaneCadastroVeiculoDialogController(veiculo);
        if (btConfirmarClicked) {
            try {
                veiculoDAO.inserir(veiculo);
            } catch (DAOException e) {
                AlertDialog.exceptionMessage(e);
            }
            carregarTableViewVeiculo();
        }
    }

    @FXML
    public void handleBtAlterar() throws IOException {
        Veiculo veiculo = tableViewVeiculos.getSelectionModel().getSelectedItem();
        if (veiculo != null) {
            boolean btConfirmarClicked = showFXMLAnchorPaneCadastroVeiculoDialogController(veiculo);
            if (btConfirmarClicked) {
                try {
                    veiculoDAO.alterar(veiculo);
                } catch (DAOException e) {
                    AlertDialog.exceptionMessage(e);
                }
                carregarTableViewVeiculo();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Esta operação requer a seleção \nde um Veículo na tabela ao lado");
            alert.show();
        }
    }

    @FXML
    public void handleBtExcluir() throws IOException {
        Veiculo veiculo = tableViewVeiculos.getSelectionModel().getSelectedItem();
        if (veiculo != null) {
            try {
                veiculoDAO.remover(veiculo);
            } catch (DAOException e) {
                AlertDialog.exceptionMessage(e);
            }
            carregarTableViewVeiculo();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Esta operação requer a seleção \nde um Veículo na tabela ao lado");
            alert.show();
        }
    }

    private boolean showFXMLAnchorPaneCadastroVeiculoDialogController(Veiculo veiculo) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXMLAnchorPaneCadastroVeiculoController.class.getResource("/view/FXMLAnchorPaneCadastroVeiculoDialog.fxml"));
        AnchorPane page;
        page = (AnchorPane) loader.load();

        //criação de um estágio de diálogo (StageDialog)
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Cadastro de Veículo");
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        //enviando o obejto modelo para o controller
        FXMLAnchorPaneCadastroVeiculoDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setVeiculo(veiculo);

        //apresenta o diálogo e aguarda a confirmação do usuário
        dialogStage.showAndWait();

        return controller.isBtConfirmarClicked();
    }

}
