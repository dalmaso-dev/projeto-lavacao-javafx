/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsc.fln.controller;

import br.edu.ifsc.fln.model.dao.OrdemServicoDAO;
import br.edu.ifsc.fln.model.database.Database;
import br.edu.ifsc.fln.model.database.DatabaseFactory;
import br.edu.ifsc.fln.model.domain.ItemOS;
import br.edu.ifsc.fln.model.domain.OrdemServico;
import br.edu.ifsc.fln.model.exceptions.DAOException;
import br.edu.ifsc.fln.model.exceptions.ExceptionLavacao;
import br.edu.ifsc.fln.utils.AlertDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author mpisching
 */
public class FXMLAnchorPaneProcessoOrdemServicoController implements Initializable {

    @FXML
    private Button buttonAlterar;

    @FXML
    private Button buttonInserir;

    @FXML
    private Button buttonRemover;

    @FXML
    private Label labelOrdemServicoVeiculoPlaca;

    @FXML
    private Label labelOrdemServicoClienteNome;

    @FXML
    private Label labelOrdemServicoAgenda;

    @FXML
    private Label labelOrdemServicoDesconto;

    @FXML
    private Label labelOrdemServicoNumero;

    @FXML
    private Label labelOrdemServicoSituacao;

    @FXML
    private Label labelOrdemServicoTotal;

    @FXML
    private TableView<OrdemServico> tableView;

    @FXML
    private TableColumn<OrdemServico, Long> tableColumnOrdemServicoNumero;

    @FXML
    private TableColumn<OrdemServico, LocalDate> tableColumnOrdemServicoAgenda;

    @FXML
    private TableColumn<OrdemServico, String> tableColumnOrdemServicoCliente;

    private List<OrdemServico> listaOrdensServicos;
    private ObservableList<OrdemServico> observableListOrdensServicos;

    //acesso ao banco de dados
    private final Database database = DatabaseFactory.getDatabase("mysql");
    private final Connection connection = database.conectar();
    private final OrdemServicoDAO ordemServicoDAO = new OrdemServicoDAO();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ordemServicoDAO.setConnection(connection);

        carregarTableView();

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selecionarItemTableView(newValue));
    }

    public void carregarTableView() {
        DateTimeFormatter myDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        tableColumnOrdemServicoNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));

        tableColumnOrdemServicoAgenda.setCellFactory(column -> {
            return new TableCell<OrdemServico, LocalDate>() {
                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(myDateFormatter.format(item));
                    }
                }
            };
        });
       
        tableColumnOrdemServicoAgenda.setCellValueFactory(new PropertyValueFactory<>("agenda"));
        tableColumnOrdemServicoCliente.setCellValueFactory(new PropertyValueFactory<>("veiculo"));

        try {
            listaOrdensServicos = ordemServicoDAO.listar();
        } catch (DAOException e) {
            AlertDialog.exceptionMessage(e);
        }

        observableListOrdensServicos = FXCollections.observableArrayList(listaOrdensServicos);
        tableView.setItems(observableListOrdensServicos);
    }

    public void selecionarItemTableView(OrdemServico ordemServico) {
        if (ordemServico != null) {
            labelOrdemServicoNumero.setText(Long.toString(ordemServico.getNumero()));
            labelOrdemServicoAgenda.setText(ordemServico.getAgenda().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            try {
                labelOrdemServicoTotal.setText(String.format("%.2f", ordemServico.calcularServico()));
            } catch (ExceptionLavacao e) {
                throw new RuntimeException(e);
            }
            labelOrdemServicoDesconto.setText((String.format("%.2f", ordemServico.getDesconto())) + "%");
            labelOrdemServicoSituacao.setText(ordemServico.getStatus().name());
            labelOrdemServicoClienteNome.setText(ordemServico.getVeiculo().getProprietario().getNome());
            labelOrdemServicoVeiculoPlaca.setText(ordemServico.getVeiculo().getPlaca());
        } else {
            labelOrdemServicoNumero.setText("");
            labelOrdemServicoAgenda.setText("");
            labelOrdemServicoTotal.setText("");
            labelOrdemServicoDesconto.setText("");
            labelOrdemServicoSituacao.setText("");
            labelOrdemServicoClienteNome.setText("");
            labelOrdemServicoVeiculoPlaca.setText("");
        }
    }

    @FXML
    private void handleButtonInserir(ActionEvent event) throws IOException, SQLException {
        OrdemServico ordemServico = new OrdemServico();
        List<ItemOS> itensOS = new ArrayList<>();
        ordemServico.getListaItemOS().addAll(itensOS);

        boolean buttonConfirmarClicked = showFXMLAnchorPaneProcessoOrdemServicoDialog(ordemServico);
        if (buttonConfirmarClicked) {
            ordemServicoDAO.setConnection(connection);
            try {
                ordemServicoDAO.inserir(ordemServico);
            } catch (DAOException e) {
                AlertDialog.exceptionMessage(e);
            }
            carregarTableView();
        }
    }

    @FXML
    private void handleButtonAlterar(ActionEvent event) throws IOException {
        OrdemServico ordemServico = tableView.getSelectionModel().getSelectedItem();
        if (ordemServico != null) {
            boolean buttonConfirmarClicked = showFXMLAnchorPaneProcessoOrdemServicoDialog(ordemServico);
            if (buttonConfirmarClicked) {
                try {
                    ordemServicoDAO.alterar(ordemServico);
                } catch (DAOException e) {
                    AlertDialog.exceptionMessage(e);
                }
                carregarTableView();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Por favor, escolha uma Ordem de Serviço na Tabela.");
            alert.show();
        }        
    }

    @FXML
    private void handleButtonRemover(ActionEvent event) throws SQLException {
        OrdemServico ordemServico = tableView.getSelectionModel().getSelectedItem();
        if (ordemServico != null) {
            if (AlertDialog.confirmarExclusao("Tem certeza que deseja excluir a ordem de serviço " + ordemServico.getNumero())) {
                ordemServicoDAO.setConnection(connection);
                try {
                    ordemServicoDAO.remover(ordemServico);
                } catch (DAOException e) {
                    AlertDialog.exceptionMessage(e);
                }
                carregarTableView();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Por favor, escolha uma ordem de serviço na tabela!");
            alert.show();
        }
    }

    public boolean showFXMLAnchorPaneProcessoOrdemServicoDialog(OrdemServico ordemServico) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXMLAnchorPaneProcessoOrdemServicoDialogController.class.getResource(
                "/view/FXMLAnchorPaneProcessoOrdemServicoDialog.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        //criando um estágio de diálogo  (Stage Dialog)
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Cadastro de ordem de serviço");
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        //Setando o venda ao controller
        FXMLAnchorPaneProcessoOrdemServicoDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setOrdemServico(ordemServico);

        //Mostra o diálogo e espera até que o usuário o feche
        dialogStage.showAndWait();

        return controller.isButtonConfirmarClicked();
    }

}
