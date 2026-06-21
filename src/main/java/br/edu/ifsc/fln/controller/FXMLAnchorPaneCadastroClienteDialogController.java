package br.edu.ifsc.fln.controller;

import br.edu.ifsc.fln.model.domain.Cliente;
import br.edu.ifsc.fln.model.domain.PessoaFisica;
import br.edu.ifsc.fln.model.domain.PessoaJuridica;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class FXMLAnchorPaneCadastroClienteDialogController  implements Initializable {

    @FXML
    private Button btCancelar;

    @FXML
    private Button btConfirmar;

    @FXML
    private Group gbTipo;

    @FXML
    private RadioButton rbPessoaFisica;

    @FXML
    private RadioButton rbPessoaJuridica;

    @FXML
    private TextField tfCelular;

    @FXML
    private TextField tfCpfCnpj;

    @FXML
    private TextField tfDataNascInscEstadual;

    @FXML
    private TextField tfEmail;

    @FXML
    private TextField tfNome;

    @FXML
    private Label lbCpfCnpj;

    @FXML
    private Label lbInscEstDataNasc;

    @FXML
    private ToggleGroup tgTipo;

    private Stage dialogStage;
    private boolean btConfirmarClicked = false;
    private Cliente cliente;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;

        if (cliente.getId() != 0) {
            this.tfNome.setText(this.cliente.getNome());
            this.tfEmail.setText(this.cliente.getEmail());
            this.tfCelular.setText(this.cliente.getCelular());

            if (cliente instanceof PessoaFisica) {
                this.tfCpfCnpj.setText(((PessoaFisica) this.cliente).getCpf());
                // formatador com o padrão que deseja gerar a String
                DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String dataNascFormatada = ((PessoaFisica) this.cliente).getDataNascimento().format(formatador);
                this.tfDataNascInscEstadual.setText(dataNascFormatada);
            }
            else {
                this.tfCpfCnpj.setText(((PessoaJuridica) this.cliente).getCnpj());
                this.tfDataNascInscEstadual.setText(((PessoaJuridica) this.cliente).getInscricaoEstadual());
            }
            this.tfNome.requestFocus();
        }

        if (cliente instanceof PessoaJuridica) {
            this.rbPessoaJuridica.setSelected(true);

        }
        else {
            this.rbPessoaFisica.setSelected(true);
        }

        rbPessoaFisica.setDisable(true);
        rbPessoaJuridica.setDisable(true);

    }

    @FXML
    public void handleBtConfirmar() {
        if (validarEntradaDeDados()) {
            if (rbPessoaFisica.isSelected()) {
                ((PessoaFisica) cliente).setCpf(tfCpfCnpj.getText());

                DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                ((PessoaFisica) cliente).setDataNascimento(LocalDate.parse(tfDataNascInscEstadual.getText(), formatador));
            } else {
                ((PessoaJuridica) cliente).setCnpj(tfCpfCnpj.getText());
                ((PessoaJuridica) cliente).setInscricaoEstadual(tfDataNascInscEstadual.getText());
            }

            cliente.setNome(tfNome.getText());
            cliente.setEmail(tfEmail.getText());
            cliente.setCelular(tfCelular.getText());
            cliente.setDataCadastro(LocalDate.now());

            btConfirmarClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    public void handleBtCancelar() {
        dialogStage.close();
    }

    @FXML
    public void handleRbPessoaFisica() {
        this.lbCpfCnpj.setText("CPF:");
        this.lbInscEstDataNasc.setText("Data de Nascimento:");
    }

    @FXML
    public void handleRbPessoaJuridica() {
        this.lbCpfCnpj.setText("CNPJ:");
        this.lbInscEstDataNasc.setText("Inscrição Estadual:");
    }

    //método para validar a entrada de dados
    private boolean validarEntradaDeDados() {
        String errorMessage = "";
        if (this.tfNome.getText().isEmpty()) {
            errorMessage += "Nome inválido.\n";
        }

        if (this.tfCelular.getText().isEmpty()) {
            errorMessage += "Telefone inválido.\n";
        }

        if (this.tfEmail.getText().isEmpty() || !this.tfEmail.getText().contains("@")) {
            errorMessage += "Email inválido.\n";
        }

        if (rbPessoaFisica.isSelected()) {
            if (this.tfCpfCnpj.getText().isEmpty()) {
                errorMessage += "CPF inválido.\n";
            }
            if (this.tfDataNascInscEstadual.getText().isEmpty()) {
                errorMessage += "Data de nascimento inválida.\n";
            }
        } else {
            if (this.tfCpfCnpj.getText().isEmpty()) {
                errorMessage += "CNPJ inválido.\n";
            }
            if (this.tfDataNascInscEstadual.getText().isEmpty()) {
                errorMessage += "Inscrição Estadual inválida.\n";
            }
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
