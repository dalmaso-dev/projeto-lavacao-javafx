/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package br.edu.ifsc.fln.controller;

import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;

import br.edu.ifsc.fln.model.dao.ClienteDAO;
import br.edu.ifsc.fln.model.dao.CorDAO;
import br.edu.ifsc.fln.model.dao.MarcaDAO;
import br.edu.ifsc.fln.model.dao.ModeloDAO;
import br.edu.ifsc.fln.model.database.Database;
import br.edu.ifsc.fln.model.database.DatabaseFactory;
import br.edu.ifsc.fln.model.domain.*;
import br.edu.ifsc.fln.model.exceptions.DAOException;
import br.edu.ifsc.fln.utils.AlertDialog;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author mpisc
 */
public class FXMLAnchorPaneCadastroVeiculoDialogController implements Initializable {

    @FXML
    private Button btCancelar;

    @FXML
    private Button btConfirmar;

    @FXML
    private TextField tfPlaca;

    @FXML
    private TextField tfObservacoes;

    @FXML
    private ChoiceBox<Modelo> cbModelo;

    @FXML
    private ChoiceBox<Cliente> cbCliente;

    @FXML
    private ChoiceBox<Cor> cbCor;

    private Stage dialogStage;
    private boolean btConfirmarClicked = false;
    private Veiculo veiculo;

    private final Database database = DatabaseFactory.getDatabase("mysql");
    private final Connection connection = database.conectar();
    private final CorDAO corDAO = new CorDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ModeloDAO modeloDAO = new ModeloDAO();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        clienteDAO.setConnection(connection);
        corDAO.setConnection(connection);
        modeloDAO.setConnection(connection);

        List<Cliente> clientes = null;
        try {
            clientes = clienteDAO.listar();
        } catch (DAOException e) {
            AlertDialog.exceptionMessage(e);
        }
        List<Cor> cores = null;

        try {
            cores = corDAO.listar();
        } catch (DAOException e) {
            AlertDialog.exceptionMessage(e);
        }

        List<Modelo> modelos = null;

        try {
            modelos = modeloDAO.listar();
        } catch (DAOException e) {
            AlertDialog.exceptionMessage(e);
        }

        //Adicionando os tipos de enum aos itens do ChoiceBox
        cbCliente.getItems().addAll(clientes);
        cbCor.getItems().addAll(cores);
        cbModelo.getItems().addAll(modelos);

        // Definindo o conversor personalizado aos choiceboxes
        cbCliente.setConverter(new StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                // O que aparecerá na tela
                return (cliente == null) ? "" : cliente.getNome();
            }

            @Override
            public Cliente fromString(String string) {
                return null;
            }
        });

        cbModelo.setConverter(new StringConverter<Modelo>() {
            @Override
            public String toString(Modelo modelo) {
                // O que aparecerá na tela
                return (modelo == null) ? "" : modelo.getDescricao() + " - " + modelo.getMarca().getNome();
            }

            @Override
            public Modelo fromString(String string) {
                return null;
            }
        });

        cbCor.setConverter(new StringConverter<Cor>() {
            @Override
            public String toString(Cor cor) {
                // O que aparecerá na tela
                return (cor == null) ? "" : cor.getNome();
            }

            @Override
            public Cor fromString(String string) {
                return null;
            }
        });
    }

    public boolean isBtConfirmarClicked() {
        return btConfirmarClicked;
    }

    public void setBtConfirmarClicked(boolean btConfirmarClicked) {
        this.btConfirmarClicked = btConfirmarClicked;
    }

    public Stage getDialogStage() {
        return dialogStage;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
        this.tfPlaca.setText(veiculo.getPlaca());
        this.tfObservacoes.setText(veiculo.getObservacoes());

        if (veiculo.getModelo() != null && veiculo.getCor() != null && veiculo.getProprietario() != null) {
            this.cbModelo.setValue(veiculo.getModelo());
            this.cbCor.setValue(veiculo.getCor());
            this.cbCliente.setValue(veiculo.getProprietario());
        }
    }


    @FXML
    public void handleBtConfirmar() {
        if (validarEntradaDeDados()) {
            veiculo.setPlaca(tfPlaca.getText());
            veiculo.setObservacoes(tfObservacoes.getText());
            veiculo.setModelo(cbModelo.getValue());
            veiculo.setCor(cbCor.getValue());
            veiculo.setCliente(cbCliente.getValue());

            btConfirmarClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    public void handleBtCancelar() {
        dialogStage.close();
    }

    //método para validar a entrada de dados
    private boolean validarEntradaDeDados() {
        String errorMessage = "";
        if (this.tfPlaca.getText().isEmpty()) {
            errorMessage += "Placa inválida.\n";
        }
        if (this.cbModelo.getValue() == null) {
            errorMessage += "Modelo inválido.\n";
        }
        if (this.cbCor.getValue() == null) {
            errorMessage += "Cor inválida.\n";
        }
        if (this.cbCliente.getValue() == null) {
            errorMessage += "Cliente inválido.\n";
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            //exibindo uma mensagem de erro
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro no cadastro");
            alert.setHeaderText("Corrija os campos inválidos!");
            alert.setContentText(errorMessage);
            alert.show();
            return false;
        }
    }

}
