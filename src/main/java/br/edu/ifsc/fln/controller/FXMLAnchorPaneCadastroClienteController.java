/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package br.edu.ifsc.fln.controller;

import br.edu.ifsc.fln.model.dao.ClienteDAO;
import br.edu.ifsc.fln.model.database.Database;
import br.edu.ifsc.fln.model.database.DatabaseFactory;
import br.edu.ifsc.fln.model.domain.PessoaFisica;
import br.edu.ifsc.fln.model.domain.PessoaJuridica;
import br.edu.ifsc.fln.model.domain.Cliente;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import br.edu.ifsc.fln.model.exceptions.DAOException;
import br.edu.ifsc.fln.utils.AlertDialog;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author mpisc
 */
public class FXMLAnchorPaneCadastroClienteController implements Initializable {

    @FXML
    private Button btAlterar;

    @FXML
    private Button btExcluir;

    @FXML
    private Button btInserir;

    @FXML
    private Label lbSubclasse1;

    @FXML
    private Label lbSubclasse2;

    @FXML
    private Label lbClienteEmail;

    @FXML
    private Label lbClienteId;

    @FXML
    private Label lbClienteNome;

    @FXML
    private Label lbClienteCelular;
    
    @FXML
    private Label lbClienteDataCadastro;

    @FXML
    private Label lbClientePontuacao;

    @FXML
    private Label lbClienteCpfCnpj;

    @FXML
    private Label lbClienteDataNascInscEstadual;

    @FXML
    private Label lbClienteTipo;

    @FXML
    private TableColumn<Cliente, String> tableColumnClienteNome;

    @FXML
    private TableColumn<Cliente, String> tableColumnClienteTipo;

    @FXML
    private TableColumn<Cliente, String> tableColumnClienteDataCadastro;

    @FXML
    private TableView<Cliente> tableViewClientes;


    private List<Cliente> listaClientes;
    private ObservableList<Cliente> observableListClientes;

    private final Database database = DatabaseFactory.getDatabase("mysql");
    private final Connection connection = database.conectar();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clienteDAO.setConnection(connection);
        carregarTableViewCliente();

        tableViewClientes.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selecionarItemTableViewClientes(newValue));
    }

    public void carregarTableViewCliente() {
        tableColumnClienteNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        tableColumnClienteDataCadastro.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();

            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String data = (cliente.getDataCadastro().format(formatador));

            return new SimpleStringProperty(data);
        });
        tableColumnClienteTipo.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            String tipo = "";

            if (cliente instanceof PessoaFisica) {
                tipo = "Físico";
            } else if (cliente instanceof PessoaJuridica) {
                tipo = "Jurídico";
            }

            return new SimpleStringProperty(tipo);
        });

        try {
            listaClientes = clienteDAO.listar();
        } catch (DAOException e) {
            AlertDialog.exceptionMessage(e);
        }

        observableListClientes = FXCollections.observableArrayList(listaClientes);
        tableViewClientes.setItems(observableListClientes);
    }

    public void selecionarItemTableViewClientes(Cliente cliente) {
        if (cliente != null) {
            lbClienteId.setText(String.valueOf(cliente.getId()));
            lbClienteNome.setText(cliente.getNome());
            lbClienteCelular.setText(cliente.getCelular());
            lbClienteEmail.setText(cliente.getEmail());

            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dataCadastroFormatada = (cliente.getDataCadastro().format(formatador));

            lbClienteDataCadastro.setText(dataCadastroFormatada);
            lbClientePontuacao.setText(String.valueOf(cliente.getPontuacao().saldo()));
            if (cliente instanceof PessoaJuridica) {
                lbClienteTipo.setText("Pessoa Jurídica");
                lbClienteCpfCnpj.setText(((PessoaJuridica)cliente).getCnpj());
                lbClienteDataNascInscEstadual.setText(((PessoaJuridica) cliente).getInscricaoEstadual());
            } else {
                lbClienteTipo.setText("Pessoa Física");
                lbClienteCpfCnpj.setText(((PessoaFisica)cliente).getCpf());

                String dataNascFormatada = ((PessoaFisica) cliente).getDataNascimento().format(formatador);
                lbClienteDataNascInscEstadual.setText(dataNascFormatada);
            }
        } else {
            lbClienteId.setText("");
            lbClienteNome.setText("");
            lbClienteCelular.setText("");
            lbClienteEmail.setText("");
            lbClienteDataCadastro.setText("");
            lbClienteTipo.setText("");
            lbClienteCpfCnpj.setText("");
            lbClienteDataNascInscEstadual.setText("");
            lbClientePontuacao.setText("");
        }

    }

    @FXML
    public void handleBtInserir() throws IOException {
        Cliente cliente = getTipoCliente();
        if (cliente != null) {
            boolean btConfirmarClicked = showFXMLAnchorPaneCadastroClienteDialogController(cliente);
            if (btConfirmarClicked) {
                try {
                    clienteDAO.inserir(cliente);
                } catch (DAOException e) {
                    AlertDialog.exceptionMessage(e);
                }
                carregarTableViewCliente();
            }
        }
    }

    private Cliente getTipoCliente() {
        List<String> opcoes = new ArrayList<>();
        opcoes.add("Pessoa Física");
        opcoes.add("Pessoa Jurídica");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Pessoa Física", opcoes);
        dialog.setTitle("Dialogo de Opções");
        dialog.setHeaderText("Escolha o tipo de cliente");
        dialog.setContentText("Tipo de cliente: ");
        Optional<String> escolha = dialog.showAndWait();
        if (escolha.isPresent()) {
            if (escolha.get().equalsIgnoreCase("Pessoa Física"))
                return new PessoaFisica();
            else
                return new PessoaJuridica();
        } else {
            return null;
        }
    }

    @FXML
    public void handleBtAlterar() throws IOException {
        Cliente cliente = tableViewClientes.getSelectionModel().getSelectedItem();
        if (cliente != null) {
            boolean btConfirmarClicked = showFXMLAnchorPaneCadastroClienteDialogController(cliente);
            if (btConfirmarClicked) {
                try {
                    clienteDAO.alterar(cliente);
                } catch (DAOException e) {
                    AlertDialog.exceptionMessage(e);
                }
                carregarTableViewCliente();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Esta operação requer a seleção \nde um Cliente na tabela ao lado");
            alert.show();
        }
    }

    @FXML
    public void handleBtExcluir() throws IOException {
        Cliente cliente = tableViewClientes.getSelectionModel().getSelectedItem();
        if (cliente != null) {
            if (AlertDialog.confirmarExclusao("Tem certeza que deseja excluir o cliente " + cliente.getNome())) {
                try {
                    clienteDAO.remover(cliente);
                } catch (DAOException e) {
                    AlertDialog.exceptionMessage(e);
                }
                carregarTableViewCliente();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Esta operação requer a seleção \nde um Cliente na tabela ao lado");
            alert.show();
        }
    }

    private boolean showFXMLAnchorPaneCadastroClienteDialogController(Cliente cliente) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXMLAnchorPaneCadastroClienteController.class.getResource
                ("/view/FXMLAnchorPaneCadastroClienteDialog.fxml"));
        AnchorPane page = (AnchorPane) loader.load();

        //criação de um estágio de diálogo (StageDialog)
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Cadastro de Clientes");
        Scene scene = new Scene(page);
        dialogStage.setScene(scene);

        //enviando o objeto cliente para o controller
        FXMLAnchorPaneCadastroClienteDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setCliente(cliente);

        //apresenta o diálogo e aguarda a confirmação do usuário
        dialogStage.showAndWait();

        return controller.isBtConfirmarClicked();
    }
}
