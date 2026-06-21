/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package br.edu.ifsc.fln.controller;

import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;

import br.edu.ifsc.fln.model.dao.MarcaDAO;
import br.edu.ifsc.fln.model.database.Database;
import br.edu.ifsc.fln.model.database.DatabaseFactory;
import br.edu.ifsc.fln.model.domain.ECategoria;
import br.edu.ifsc.fln.model.domain.ETipoCombustivel;
import br.edu.ifsc.fln.model.domain.Marca;
import br.edu.ifsc.fln.model.domain.Modelo;
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
public class FXMLAnchorPaneCadastroModeloDialogController implements Initializable {

    @FXML
    private Button btCancelar;

    @FXML
    private Button btConfirmar;

    @FXML
    private TextField tfDescricao;

    @FXML
    private TextField tfPotencia;

    @FXML
    private ChoiceBox<Marca> cbMarca;

    @FXML
    private ChoiceBox<String> cbCombustivel;

    @FXML
    private ChoiceBox<String> cbCategoria;

    private Stage dialogStage;
    private boolean btConfirmarClicked = false;
    private Modelo modelo;

    private final Database database = DatabaseFactory.getDatabase("mysql");
    private final Connection connection = database.conectar();
    private final MarcaDAO marcaDAO = new MarcaDAO();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        marcaDAO.setConnection(connection);

        List<Marca> marcas = null;
        try {
            marcas = marcaDAO.listar();
        } catch (DAOException e) {
            AlertDialog.exceptionMessage(e);
        }

        String[] categorias = {"GRANDE", "MEDIO", "PEQUENO", "MOTO", "PADRAO"};
        String[] combustiveis = {"GASOLINA", "DIESEL", "GNV", "ETANOL", "FLEX", "OUTRO"};

        //Adicionando os tipos de enum aos itens do ChoiceBox
        cbCategoria.getItems().addAll(categorias);
        cbCombustivel.getItems().addAll(combustiveis);
        cbMarca.getItems().addAll(marcas);

        //Definindo qual item aparecerá selecionado por padrão
        cbCategoria.setValue("PADRAO");
        cbCombustivel.setValue("GASOLINA");

        cbMarca.setConverter(new StringConverter<Marca>() {
            @Override
            public String toString(Marca marca) {
                return marca == null? "" : marca.getNome();
            }

            @Override
            public Marca fromString(String string) {
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

    public Modelo getModelo() {
        return modelo;
    }

    public void setModelo(Modelo modelo) {
        this.modelo = modelo;
        this.tfDescricao.setText(modelo.getDescricao());
        this.tfPotencia.setText(String.valueOf(modelo.getMotor().getPotencia()));
        this.cbCategoria.setValue(modelo.getCategoria().name());

        if (modelo.getMotor().getTipoCombustivel() == null) {
            modelo.getMotor().setTipoCombustivel(ETipoCombustivel.GASOLINA);
        }
        this.cbCombustivel.setValue(modelo.getMotor().getTipoCombustivel().name());
        this.cbMarca.setValue(modelo.getMarca());
    }


    @FXML
    public void handleBtConfirmar() {
        if (validarEntradaDeDados()) {
            modelo.setDescricao(tfDescricao.getText());
            modelo.setCategoria(Enum.valueOf(ECategoria.class, cbCategoria.getValue()));
            modelo.setMarca(cbMarca.getValue());
            modelo.getMotor().setTipoCombustivel(Enum.valueOf(ETipoCombustivel.class, cbCombustivel .getValue()));
            modelo.getMotor().setPotencia(Integer.parseInt(tfPotencia.getText()));

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
        if ((this.tfDescricao.getText().equals("")) || (this.tfPotencia.getText().equals(""))) {
            errorMessage += "Descrição inválida.\n";
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
