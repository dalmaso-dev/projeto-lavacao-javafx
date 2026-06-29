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
import br.edu.ifsc.fln.model.exceptions.DAOException;
import br.edu.ifsc.fln.model.exceptions.ExceptionLavacao;
import br.edu.ifsc.fln.utils.AlertDialog;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
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
    private TextArea textAreaObservacoes;
    @FXML
    private DatePicker datePickerAgenda;
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
    private Button buttonAplicarDesconto;
    @FXML
    private ContextMenu contextMenuTableView;
    @FXML
    private MenuItem contextMenuItemRemoverItem;
    @FXML
    private ChoiceBox<EStatus> choiceBoxStatus;
    @FXML
    private MenuItem contextMenuItemAtualizarObs;
    @FXML
    private TextField tfDesconto;


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
        try {
            Servico.setPontos(servicoDAO.buscarPontos());
        } catch (DAOException e) {
            AlertDialog.exceptionMessage(e);
        }

        comboBoxClientes.setConverter(new StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                // Como o objeto deve aparecer na tela
                if (cliente == null) {
                    return "";
                }
                String documento = cliente instanceof PessoaFisica?((PessoaFisica) cliente).getCpf(): ((PessoaJuridica) cliente).getCnpj();
                return cliente.getNome() + " - " + documento + " - Celular: " + cliente.getCelular() + " - Pontos: " + cliente.getPontuacao().saldo();
            }

            @Override
            public Cliente fromString(String string) {
                // Só é necessário se o seu ComboBox for editável (o usuário pode digitar nele)
                // Caso contrário, pode retornar null
                return null;
            }
        });
        comboBoxVeiculos.setConverter(new StringConverter<Veiculo>() {
            @Override
            public String toString(Veiculo veiculo) {
                // Como o objeto deve aparecer na tela
                if (veiculo == null) {
                    return "";
                }
                return veiculo.getPlaca() + " - " + veiculo.getModelo().getDescricao() + " - " + veiculo.getCor().getNome() + " - obs: " + veiculo.getObservacoes();
            }

            @Override
            public Veiculo fromString(String string) {
                // Só é necessário se o seu ComboBox for editável (o usuário pode digitar nele)
                // Caso contrário, pode retornar null
                return null;
            }
        });
        comboBoxServicos.setConverter(new StringConverter<Servico>() {
            @Override
            public String toString(Servico servico) {
                // Como o objeto deve aparecer na tela
                if (servico == null) {
                    return "";
                }
                return servico.getDescricao() + " - R$" + servico.getValor();
            }

            @Override
            public Servico fromString(String string) {
                // Só é necessário se o seu ComboBox for editável (o usuário pode digitar nele)
                // Caso contrário, pode retornar null
                return null;
            }
        });
        carregarComboBoxClientes();
        carregarChoiceBoxStatus();
        tableColumnServico.setCellValueFactory(cellData -> {
            ItemOS item = cellData.getValue();

            return new SimpleStringProperty(item.getServico().getDescricao());
        });
        tableColumnObservacao.setCellValueFactory(new PropertyValueFactory<>("observacoes"));
        tableColumnValor.setCellValueFactory(new PropertyValueFactory<>("valorServico"));
    }

    private void carregarComboBoxClientes() {
        try {
            listaClientes = clienteDAO.listar();
        } catch (DAOException e) {
            AlertDialog.exceptionMessage(e);
        }
        observableListClientes = FXCollections.observableArrayList(listaClientes);
        comboBoxClientes.setItems(observableListClientes);
    }

    private void carregarComboBoxServicos() {
        String categoria = comboBoxVeiculos.getValue().getModelo().getCategoria().name();
        try {
            listaServicos = servicoDAO.listarPorCategoria(categoria);
        } catch (DAOException e) {
            AlertDialog.exceptionMessage(e);
        }
        observableListServicos = FXCollections.observableArrayList(listaServicos);
        comboBoxServicos.setItems(observableListServicos);
    }


    public void carregarChoiceBoxStatus() {
        choiceBoxStatus.setItems( FXCollections.observableArrayList(EStatus.values()));
        choiceBoxStatus.getSelectionModel().select(0);
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
            comboBoxClientes.getSelectionModel().select(this.ordemServico.getVeiculo().getProprietario());
            comboBoxVeiculos.getSelectionModel().select(this.ordemServico.getVeiculo());
            carregarComboBoxServicos();
            datePickerAgenda.setValue(this.ordemServico.getAgenda());
            observableListItensOS = FXCollections.observableArrayList(this.ordemServico.getListaItemOS());
            tableViewItensOS.setItems(observableListItensOS);
            tfTotal.setText(String.format("%.2f", this.ordemServico.getTotal()));
            tfDesconto.setText(String.format("%.2f", this.ordemServico.getDesconto()));
            choiceBoxStatus.getSelectionModel().select(this.ordemServico.getStatus());
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
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText(null);
                    alert.setContentText("Este serviço já está inserido na lista de itens da OS.");
                    alert.showAndWait();

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
            textAreaObservacoes.setText("");
            comboBoxServicos.setValue(null);
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
        comboBoxVeiculos.setValue(null);
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
        this.ordemServico.setDesconto(0);
        tfDesconto.setText(String.format("%.2f", this.ordemServico.getDesconto()));
        if (comboBoxVeiculos.getValue() != null) {
            Cliente cliente = comboBoxVeiculos.getValue().getProprietario();

            if (cliente.getPontuacao().saldo() >= 200) {

                ButtonType btnSim = new ButtonType("Sim", ButtonBar.ButtonData.YES);
                ButtonType btnNao = new ButtonType("Não", ButtonBar.ButtonData.NO);

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmação");
                alert.setHeaderText("Aplicar Desconto");
                alert.setContentText("O dono do veículo está elegível para desconto de 15% na OS,\nsabendo que a pontuação do cliente será reduzido 200 pontos,\ndeseja aplicá-lo?");

                alert.getButtonTypes().setAll(btnSim, btnNao);

                alert.showAndWait().ifPresent(resposta -> {
                    if (resposta == btnSim) {
                        this.ordemServico.setDesconto(15);
                        tfDesconto.setText(String.format("%.2f", this.ordemServico.getDesconto()));
                    } else {
                        alert.close();
                    }
                });
            }
        }
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

        if ((datePickerAgenda.getValue() == null) || (datePickerAgenda.getValue().isBefore(LocalDate.now()))) {
            errorMessage += "Data inválida!\n";
        }

        if (observableListItensOS == null) {
            errorMessage += "Nenhum item inserido!\n";
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
