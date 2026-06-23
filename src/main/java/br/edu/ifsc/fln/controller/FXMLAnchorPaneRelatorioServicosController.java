/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.ifsc.fln.controller;

import br.edu.ifsc.fln.model.exceptions.DAOException;
import br.edu.ifsc.fln.model.dao.ServicoDAO;
import br.edu.ifsc.fln.model.database.Database;
import br.edu.ifsc.fln.model.database.DatabaseFactory;
import br.edu.ifsc.fln.model.domain.ECategoria;
import br.edu.ifsc.fln.model.domain.Servico;
import br.edu.ifsc.fln.utils.AlertDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FXML Controller class
 *
 * @author mpisching
 */
public class FXMLAnchorPaneRelatorioServicosController implements Initializable {

    @FXML
    private TableView<Servico> tableView;
    @FXML
    private TableColumn<Servico, Integer> tableColumnServicoID;
    @FXML
    private TableColumn<Servico, String> tableColumnServicoDescricao;
    @FXML
    private TableColumn<Servico, BigDecimal> tableColumnServicoValor;
    @FXML
    private TableColumn<Servico, ECategoria> tableColumnServicoCategoria;
    @FXML
    private Button buttonImprimir;
    
    private List<Servico> listaServicos;
    private ObservableList<Servico> observableListServicos;
    
    private final Database database = DatabaseFactory.getDatabase("mysql");
    private final Connection connection = database.conectar();
    private final ServicoDAO servicoDAO = new ServicoDAO();

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        servicoDAO.setConnection(connection);
        carregarTableView();
    }    
    
    private void carregarTableView() {
        try {
            listaServicos = servicoDAO.listar();
        } catch (DAOException ex) {
            Logger.getLogger(FXMLAnchorPaneRelatorioServicosController.class.getName()).log(Level.SEVERE, null, ex);
            AlertDialog.exceptionMessage(ex);
            return;
        }
        
        tableColumnServicoID.setCellValueFactory(new PropertyValueFactory<>("id"));
        tableColumnServicoDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        tableColumnServicoValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        tableColumnServicoCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        observableListServicos = FXCollections.observableArrayList(listaServicos);
        tableView.setItems(observableListServicos);
    }
    
    //@FXML
    public void handleImprimir() throws JRException {
        URL url = getClass().getResource("/report/PrjSistemaLavacaoRelServicos.jasper");
        JasperReport jasperReport = (JasperReport)JRLoader.loadObject(url);
        
        //null: caso não existam filtros
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, connection);
        
        //false: não deixa fechar a aplicação principal
        JasperViewer jasperViewer = new JasperViewer(jasperPrint, false);
        jasperViewer.setVisible(true);  
    }
    
}
