package br.edu.ifsc.fln.model.dao;

import br.edu.ifsc.fln.model.domain.ECategoria;
import br.edu.ifsc.fln.model.domain.Servico;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServicoDAO {

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public boolean inserir(Servico servico) {
        String sql = "INSERT INTO servico(descricao, valor, categoria) VALUES(?,?,?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, servico.getDescricao());
            stmt.setDouble(2, servico.getValor());
            stmt.setString(3,servico.getCategoria().name());
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean alterar(Servico servico) {
        String sql = "UPDATE servico SET descricao=?,valor=?,categoria=? WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, servico.getDescricao());
            stmt.setDouble(2, servico.getValor());
            stmt.setString(3,servico.getCategoria().name());
            stmt.setInt(4, servico.getId());
            stmt.execute();

            return true;
        } catch (SQLException ex) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean remover(Servico servico) {
        String sql = "DELETE FROM servico WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, servico.getId());
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public List<Servico> listar() {
        String sql = "SELECT * FROM servico";
        List<Servico> retorno = new ArrayList<>();

        Servico.setPontos(buscarPontos());

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultado = stmt.executeQuery();

            while (resultado.next()) {
                Servico servico = new Servico();
                servico.setId(resultado.getInt("id"));
                servico.setDescricao(resultado.getString("descricao"));
                servico.setValor(resultado.getDouble("valor"));
                servico.setCategoria(Enum.valueOf(ECategoria.class,resultado.getString("categoria")));
                retorno.add(servico);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retorno;
    }

    public boolean alterarPontos() {
        String sql = "UPDATE parametros_de_sistema SET pontos=? WHERE chave='pontos' ";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, Servico.getPontos());
            stmt.execute();

            return true;
        } catch (SQLException ex) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public int buscarPontos() {
        String sql = "SELECT pontos FROM parametros_de_sistema WHERE chave=?";
        int pontos = 0;
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1,"pontos");
            ResultSet resultado = stmt.executeQuery();
            if (resultado.next()) {
                pontos = resultado.getInt("pontos");
            }
        }
         catch (SQLException ex) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return pontos;
    }

    public Servico buscar(Servico servico) {
        Servico retorno = buscar(servico.getId());
        return retorno;
    }

    public Servico buscar(int id) {
        String sql = "SELECT * FROM servico WHERE id=?";
        Servico retorno = new Servico();
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet resultado = stmt.executeQuery();
            if (resultado.next()) {
                retorno.setId(resultado.getInt("id"));
                retorno.setDescricao(resultado.getString("descricao"));
                retorno.setValor(resultado.getDouble("valor"));
                retorno.setCategoria(Enum.valueOf(ECategoria.class,resultado.getString("categoria")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retorno;
    }

    public List<Servico> listarPorCategoria(String categoria) {
        String sql = "SELECT * FROM servico where categoria=? or categoria='PADRAO'";
        List<Servico> retorno = new ArrayList<>();

        Servico.setPontos(buscarPontos());

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, categoria);
            ResultSet resultado = stmt.executeQuery();

            while (resultado.next()) {
                Servico servico = new Servico();
                servico.setId(resultado.getInt("id"));
                servico.setDescricao(resultado.getString("descricao"));
                servico.setValor(resultado.getDouble("valor"));
                servico.setCategoria(Enum.valueOf(ECategoria.class,resultado.getString("categoria")));
                retorno.add(servico);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retorno;
    }
}

