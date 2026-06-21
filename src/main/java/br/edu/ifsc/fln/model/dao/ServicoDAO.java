package br.edu.ifsc.fln.model.dao;

import br.edu.ifsc.fln.model.domain.ECategoria;
import br.edu.ifsc.fln.model.domain.Servico;
import br.edu.ifsc.fln.model.exceptions.DAOException;

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

    public void inserir(Servico servico) throws DAOException {
        String sql = "INSERT INTO servico(descricao, valor, categoria) VALUES(?,?,?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, servico.getDescricao());
            stmt.setDouble(2, servico.getValor());
            stmt.setString(3,servico.getCategoria().name());
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha no registro ao banco de dados.", ex);
        }
    }

    public void alterar(Servico servico) throws DAOException {
        String sql = "UPDATE servico SET descricao=?,valor=?,categoria=? WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, servico.getDescricao());
            stmt.setDouble(2, servico.getValor());
            stmt.setString(3,servico.getCategoria().name());
            stmt.setInt(4, servico.getId());
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao alterar dados do registro no banco de dados.", ex);
        }
    }

    public void remover(Servico servico) throws DAOException {
        String sql = "DELETE FROM servico WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, servico.getId());
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao excluir registro no banco de dados.", ex);
        }
    }

    public List<Servico> listar() throws DAOException {
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
            throw new DAOException("Falha na pesquisa no banco de dados.", ex);
        }
        return retorno;
    }

    public void alterarPontos() throws DAOException {
        String sql = "UPDATE parametros_de_sistema SET pontos=? WHERE chave='pontos' ";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, Servico.getPontos());
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao alterar pontos no banco de dados", ex);
        }
    }

    public int buscarPontos() throws DAOException {
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
            throw new DAOException("Falha ao realizar pesquisa no banco de dodos.", ex);
        }

        return pontos;
    }

    public Servico buscar(Servico servico) throws DAOException {
        Servico retorno;

        try {
            retorno = buscar(servico.getId());
        } catch (DAOException e) {
            throw new DAOException(e);
        }
        return retorno;
    }

    public Servico buscar(int id) throws DAOException {
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
            throw new DAOException("Falha ao realizar pesquisa no banco de dados.", ex);
        }
        return retorno;
    }

    public List<Servico> listarPorCategoria(String categoria) throws DAOException {
        String sql = "SELECT * FROM servico where categoria=? or categoria='PADRAO'";
        List<Servico> retorno = new ArrayList<>();

        try {
            Servico.setPontos(buscarPontos());
        } catch (DAOException e) {
            Logger.getLogger(ServicoDAO.class.getName()).log(Level.SEVERE, null, e);
            throw new DAOException("Falha ao realizar pesquisa no no banco de dados.", e);
        }

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
            throw new DAOException("Falha ao realizar pesquisa no no banco de dados.", ex);
        }
        return retorno;
    }
}

