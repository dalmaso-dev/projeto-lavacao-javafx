/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsc.fln.controller;

import br.edu.ifsc.fln.model.dao.*;
import br.edu.ifsc.fln.model.database.Database;
import br.edu.ifsc.fln.model.database.DatabaseFactory;
import br.edu.ifsc.fln.model.domain.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author mpisching
 */
public class FXMLAnchorPaneProcessoOrdemServicoDialogController implements Initializable {

    @FXML
    private DatePicker datePickerAgenda;
    @FXML
    private TableView<ItemOS> tableViewItensOS;
    @FXML
    private TableColumn<ItemOS, Servico> tableColumnServico;
    @FXML
    private TableColumn<ItemOS, Integer> tableColumnObservacao;
    @FXML
    private TableColumn<ItemOS, Double> tableColumnValor;
    @FXML
    private TextField textFieldValor;
    @FXML
    private ComboBox<Veiculo> comboBoxVeiculos;
    @FXML
    private ComboBox<Cliente> comboBoxClientes;
    @FXML
    private ComboBox<Servico> comboBoxServicos;
    @FXML
    private Button buttonAdicionar;
    @FXML
    private Button buttonConfirmar;
    @FXML
    private Button buttonCancelar;
    @FXML
    private ContextMenu contextMenuTableView;
    @FXML
    private MenuItem contextMenuItemAtualizarQtd;
    @FXML
    private MenuItem contextMenuItemRemoverItem;
    @FXML
    private ChoiceBox<String> choiceBoxStatus;
    @FXML
    private MenuItem contextMenuItemAtualizarObs;
    @FXML
    private TextField tfDesconto;

    @FXML
    private TextField tfTotal;


    private List<Cliente> listaClientes;
    private List<Servico> listaServicos;
    private ObservableList<Cliente> observableListClientes;
    private ObservableList<Servico> observableListServicos;
    private ObservableList<ItemOS> observableListItensOS;

    //atributos para manipulação de banco de dados
    private final Database database = DatabaseFactory.getDatabase("mysql");
    private final Connection connection = database.conectar();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ServicoDAO servicoDAO = new ServicoDAO();
    private final VeiculoDAO veiculoDAO = new VeiculoDAO();

    private Stage dialogStage;
    private boolean buttonConfirmarClicked = false;
    private OrdemServico ordemServico;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clienteDAO.setConnection(connection);
        servicoDAO.setConnection(connection);
        veiculoDAO.setConnection(connection);
        carregarComboBoxClientes();
        carregarChoiceBoxStatus();
        setFocusLostHandle();
        tableColumnServico.setCellValueFactory(new PropertyValueFactory<>("servico"));
        tableColumnObservacao.setCellValueFactory(new PropertyValueFactory<>("observacoes"));
        tableColumnValor.setCellValueFactory(new PropertyValueFactory<>("valorServico"));
    }

    private void carregarComboBoxClientes() {
        listaClientes = clienteDAO.listar();
        observableListClientes = FXCollections.observableArrayList(listaClientes);
        comboBoxClientes.setItems(observableListClientes);
    }

    private void carregarComboBoxServicos() {
        /* carrega apenas os produtos  com estoque cuja SITUACAO está em ATIVO para operações */
        String categoria = comboBoxVeiculos.getValue().getModelo().getCategoria().name();
        listaServicos = servicoDAO.listarPorCategoria(categoria);
        observableListServicos = FXCollections.observableArrayList(listaServicos);
        comboBoxServicos.setItems(observableListServicos);
    }


    public void carregarChoiceBoxSituacao() {
        choiceBoxStatus.setItems( FXCollections.observableArrayList(Arrays.toString(EStatus.values())));
        choiceBoxStatus.getSelectionModel().select(0);
    }

    private void setFocusLostHandle() {
        tfDesconto.focusedProperty().addListener((ov, oldV, newV) -> {
        if (!newV) { // focus lost
                if (tfDesconto.getText() != null && !tfDesconto.getText().isEmpty()) {
                    //System.out.println("teste focus lost");
                    ordemServico.setDesconto(Double.parseDouble(tfDesconto.getText()));
                    textFieldValor.setText(String.valueOf(ordemServico.getTotal()));
                }
            }
        });
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

    /**
     * @return the buttonConfirmarClicked
     */
    public boolean isButtonConfirmarClicked() {
        return buttonConfirmarClicked;
    }

    /**
     * @param buttonConfirmarClicked the buttonConfirmarClicked to set
     */
    public void setButtonConfirmarClicked(boolean buttonConfirmarClicked) {
        this.buttonConfirmarClicked = buttonConfirmarClicked;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public void setOrdemServico(OrdemServico ordemServico) {
        this.ordemServico = ordemServico;
        if (ordemServico.getNumero() != 0) {
            comboBoxClientes.getSelectionModel().select(this.ordemServico.getVeiculo().getProprietario();
            datePickerAgenda.setValue(this.ordemServico.getAgenda());
            observableListItensOS = FXCollections.observableArrayList(
                    this.ordemServico.getListaItemOS());
            tableViewItensOS.setItems(observableListItensOS);
            textFieldValor.setText(String.format("%.2f", this.ordemServico.getTotal()));
            tfDesconto.setText(String.format("%.2d%%", this.ordemServico.getDesconto()));
            choiceBoxStatus.getSelectionModel().select(this.ordemServico.getStatus().name());
        }
    }

    @FXML
    public void handleButtonAdicionar() {
        Servico servico;
        ItemOS itemOS = new ItemOS();
        if (comboBoxServicos.getSelectionModel().getSelectedItem() != null) {
            //o comboBox possui dados sintetizados de Servico para evitar carga desnecessária de informação
            servico = comboBoxServicos.getSelectionModel().getSelectedItem();

                itemOS.setServico(servico);
                itemOS.setValorServico(Double.parseDouble(textFieldValor.getText()));
                

                itemDeVenda.setValor(produto.getPreco().multiply(BigDecimal.valueOf(itemDeVenda.getQuantidade())));
                itemDeVenda.setVenda(venda);
                venda.getItensDeVenda().add(itemDeVenda);
                observableListItensDeVenda = FXCollections.observableArrayList(venda.getItensDeVenda());
                tableViewItensDeVenda.setItems(observableListItensDeVenda);
                textFieldValor.setText(String.format("%.2f", venda.getTotal()));

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Problemas na escolha do produto");
                alert.setContentText("Não existe quantidade suficiente de produtos para venda.");
                alert.show();
            }
        }
    }

    @FXML
    private void handleButtonConfirmar() {
        if (validarEntradaDeDados()) {
            venda.setCliente(comboBoxClientes.getSelectionModel().getSelectedItem());
            venda.setPago(checkBoxPago.isSelected());
            venda.setData(datePickerData.getValue());
            venda.setStatusVenda((EStatusVenda)choiceBoxSituacao.getSelectionModel().getSelectedItem());
            venda.setTaxaDesconto(Double.parseDouble(textFieldDesconto.getText()));
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
        ItemDeVenda itemDeVenda
                = tableViewItensDeVenda.getSelectionModel().getSelectedItem();
        if (itemDeVenda == null) {
            contextMenuItemAtualizarQtd.setDisable(true);
            contextMenuItemRemoverItem.setDisable(true);
        } else {
            contextMenuItemAtualizarQtd.setDisable(false);
            contextMenuItemRemoverItem.setDisable(false);
        }

    }

    @FXML
    private void handleContextMenuItemAtualizarQtd() {
        ItemDeVenda itemDeVenda
                = tableViewItensDeVenda.getSelectionModel().getSelectedItem();
        int index = tableViewItensDeVenda.getSelectionModel().getSelectedIndex();

        int qtdAtualizada = Integer.parseInt(inputDialog(itemDeVenda.getQuantidade()));
        if (itemDeVenda.getProduto().getEstoque().getQuantidade() >= qtdAtualizada) {
            itemDeVenda.setQuantidade(qtdAtualizada);
            //venda.getItensDeVenda().set(venda.getItensDeVenda().indexOf(itemDeVenda),itemDeVenda);
            venda.getItensDeVenda().set(index, itemDeVenda);
            itemDeVenda.setValor(itemDeVenda.getProduto().getPreco().multiply(BigDecimal.valueOf(itemDeVenda.getQuantidade())));
            tableViewItensDeVenda.refresh();
            textFieldValor.setText(String.format("%.2f", venda.getTotal()));
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erro no estoque");
            alert.setContentText("Não há quantidade suficiente de produtos para venda.");
            alert.show();
        }
    }

    private String inputDialog(int value) {
        TextInputDialog dialog = new TextInputDialog(Integer.toString(value));
        dialog.setTitle("Entrada de dados.");
        dialog.setHeaderText("Atualização da quantidade de produtos.");
        dialog.setContentText("Quantidade: ");

        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        return result.get();
    }

    @FXML
    private void handleContextMenuItemRemoverItem() {
        ItemDeVenda itemDeVenda
                = tableViewItensDeVenda.getSelectionModel().getSelectedItem();
        int index = tableViewItensDeVenda.getSelectionModel().getSelectedIndex();
        venda.getItensDeVenda().remove(index);
        observableListItensDeVenda = FXCollections.observableArrayList(venda.getItensDeVenda());
        tableViewItensDeVenda.setItems(observableListItensDeVenda);

        textFieldValor.setText(String.format("%.2f", venda.getTotal()));
    }

    //validar entrada de dados do cadastro
    private boolean validarEntradaDeDados() {
        String errorMessage = "";

        if (comboBoxClientes.getSelectionModel().getSelectedItem() == null) {
            errorMessage += "Cliente inválido!\n";
        }

        if (datePickerData.getValue() == null) {
            errorMessage += "Data inválida!\n";
        }

        if (observableListItensDeVenda == null) {
            errorMessage += "Itens de venda inválidos!\n";
        }

        DecimalFormat df = new DecimalFormat("0.00");
        try {
            textFieldDesconto.setText(df.parse(textFieldDesconto.getText()).toString());
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
