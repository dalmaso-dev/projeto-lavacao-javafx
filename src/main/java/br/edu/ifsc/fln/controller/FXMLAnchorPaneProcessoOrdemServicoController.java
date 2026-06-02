/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsc.fln.controller;

import br.edu.ifsc.fln.model.dao.EstoqueDAO;
import br.edu.ifsc.fln.model.dao.ItemDeVendaDAO;
import br.edu.ifsc.fln.model.dao.ProdutoDAO;
import br.edu.ifsc.fln.model.dao.VendaDAO;
import br.edu.ifsc.fln.model.database.Database;
import br.edu.ifsc.fln.model.database.DatabaseFactory;
import br.edu.ifsc.fln.model.domain.ItemDeVenda;
import br.edu.ifsc.fln.model.domain.OrdemServico;
import br.edu.ifsc.fln.model.domain.Venda;
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
    private CheckBox checkBoxVendaPago;

    @FXML
    private Label labelVendaCliente;

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
    private TableColumn<OrdemServico, Long> tableColumnOrdemServicoId;

    @FXML
    private TableColumn<OrdemServico, LocalDate> tableColumnOrdemServicoAgenda;

    @FXML
    private TableColumn<OrdemServico, String> tableColumnOrdemServicoCliente;

    private List<OrdemServico> listaOrdensServicos;
    private ObservableList<OrdemServico> observableListOrdensServicos;

    //acesso ao banco de dados
    private final Database database = DatabaseFactory.getDatabase("mysql");
    private final Connection connection = database.conectar();
    private final VendaDAO vendaDAO = new VendaDAO();
    private final ItemDeVendaDAO itemDeVendaDAO = new ItemDeVendaDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final EstoqueDAO estoqueDAO = new EstoqueDAO();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        vendaDAO.setConnection(connection);

        carregarTableView();

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selecionarItemTableView(newValue));
    }

    public void carregarTableView() {
        DateTimeFormatter myDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        tableColumnVendaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        //tableColumnVendaData.setCellValueFactory(new PropertyValueFactory<>("data"));
        tableColumnVendaData.setCellFactory(column -> {
            return new TableCell<Venda, LocalDate>() {
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
       
        tableColumnVendaData.setCellValueFactory(new PropertyValueFactory<>("data"));
        tableColumnVendaCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));

        listaVendas = vendaDAO.listar();

        observableListVendas = FXCollections.observableArrayList(listaVendas);
        tableView.setItems(observableListVendas);
    }

    public void selecionarItemTableView(Venda venda) {
        if (venda != null) {
            labelVendaId.setText(Integer.toString(venda.getId()));
            labelVendaData.setText(String.valueOf(
                    venda.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            labelVendaTotal.setText(String.format("%.2f", venda.getTotal()));
            labelVendaDesconto.setText((String.format("%.2f", venda.getTaxaDesconto())) + "%");
            checkBoxVendaPago.setSelected(venda.isPago());
            labelVendaSituacao.setText(venda.getStatusVenda().name());
            labelVendaCliente.setText(venda.getCliente().getNome());
        } else {
            labelVendaId.setText("");
            labelVendaData.setText("");
            labelVendaTotal.setText("");
            labelVendaDesconto.setText("");
            checkBoxVendaPago.setSelected(false);
            labelVendaSituacao.setText("");
            labelVendaCliente.setText("");
        }
    }

    @FXML
    private void handleButtonInserir(ActionEvent event) throws IOException, SQLException {
        Venda venda = new Venda();
        List<ItemDeVenda> itensDeVenda = new ArrayList<>();
        venda.setItensDeVenda(itensDeVenda);
        boolean buttonConfirmarClicked = showFXMLAnchorPaneProcessoVendaDialog(venda);
        if (buttonConfirmarClicked) {
            //O código comentado a seguir (bloco try..catch) evidencia uma má prática de programação, haja vista que o boa parte da lógica de negócio está implementada no controller
            //PROBLEMA: caso haja necessidade de levar esta aplicação para outro nível (uma aplicação web, por exemplo), todo esse código deverá ser repetido no controller, o que
            //de fato pode se tornar inconsistente caso uma nova lógica seja necessária, implicando na necessidade de rever todos os controllers das aplicações, mas, o que garante
            // que todas equipes farão isso?
            //SOLUÇÃO: levar a lógica de negócio para o VendaDAO, afinal, estamos tratando de uma venda. É ela que deve resolver o problema
//            try {
//                connection.setAutoCommit(false);
//                vendaDAO.setConnection(connection);
//                vendaDAO.inserir(venda);
//                itemDeVendaDAO.setConnection(connection);
//                produtoDAO.setConnection(connection);
//                estoqueDAO.setConnection(connection);
//                for (ItemDeVenda itemDeVenda: venda.getItensDeVenda()) {
//                    Produto produto = itemDeVenda.getProduto();
//                    itemDeVenda.setVenda(vendaDAO.buscarUltimaVenda());
//                    itemDeVendaDAO.inserir(itemDeVenda);
//                    produto.getEstoque().setQuantidade(
//                            produto.getEstoque().getQuantidade() - itemDeVenda.getQuantidade());
//                    estoqueDAO.atualizar(produto.getEstoque());
//                }
//                connection.commit();
//                carregarTableView();
//            } catch (SQLException exc) {
//                try {
//                    connection.rollback();
//                } catch (SQLException exc1) {
//                    Logger.getLogger(FXMLAnchorPaneProcessoVendaController.class.getName()).log(Level.SEVERE, null, exc1);
//                }
//                Logger.getLogger(FXMLAnchorPaneProcessoVendaController.class.getName()).log(Level.SEVERE, null, exc);
//            }   
//        }
            vendaDAO.setConnection(connection);
            vendaDAO.inserir(venda);
            carregarTableView();
        }
    }

    @FXML
    private void handleButtonAlterar(ActionEvent event) throws IOException {
        Venda venda = tableView.getSelectionModel().getSelectedItem();
        if (venda != null) {
            boolean buttonConfirmarClicked = showFXMLAnchorPaneProcessoVendaDialog(venda);
            if (buttonConfirmarClicked) {
                vendaDAO.alterar(venda);
                carregarTableView();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Por favor, escolha um venda na Tabela.");
            alert.show();
        }        
    }

    @FXML
    private void handleButtonRemover(ActionEvent event) throws SQLException {
        Venda venda = tableView.getSelectionModel().getSelectedItem();
        if (venda != null) {
            if (AlertDialog.confirmarExclusao("Tem certeza que deseja excluir a venda " + venda.getId())) {
                vendaDAO.setConnection(connection);
                vendaDAO.remover(venda);
                carregarTableView();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Por favor, escolha uma venda na tabela!");
            alert.show();
        }
    }

    public boolean showFXMLAnchorPaneProcessoVendaDialog(Venda venda) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXMLAnchorPaneProcessoOrdemServicoDialogController.class.getResource(
                "/view/FXMLAnchorPaneProcessoOrdemServicoDialog.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        //criando um estágio de diálogo  (Stage Dialog)
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Cadastro de vendas");
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        //Setando o venda ao controller
        FXMLAnchorPaneProcessoOrdemServicoDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setVenda(venda);

        //Mostra o diálogo e espera até que o usuário o feche
        dialogStage.showAndWait();

        return controller.isButtonConfirmarClicked();
    }

}
