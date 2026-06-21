package br.edu.ifsc.fln.model.dao;

import br.edu.ifsc.fln.model.domain.Marca;
import br.edu.ifsc.fln.model.exceptions.DAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MarcaDAO {

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void inserir(Marca marca) throws DAOException {
        String sql = "INSERT INTO marca(nome) VALUES(?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, marca.getNome());
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(MarcaDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao realizar registro no banco de dados.", ex);
        }
    }

    public void alterar(Marca marca) throws DAOException {
        String sql = "UPDATE marca SET nome=? WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, marca.getNome());
            stmt.setInt(2, marca.getId());
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(MarcaDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao alterar dados do registro no banco de dados.", ex);
        }
    }

    public void remover(Marca marca) throws DAOException {
        String sql = "DELETE FROM marca WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, marca.getId());
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(MarcaDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao exluir registro no banco de dados.", ex);
        }
    }

    public List<Marca> listar() throws DAOException {
        String sql = "SELECT * FROM marca";
        List<Marca> retorno = new ArrayList<>();
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultado = stmt.executeQuery();
            while (resultado.next()) {
                Marca marca = new Marca();
                marca.setId(resultado.getInt("id"));
                marca.setNome(resultado.getString("nome"));
                retorno.add(marca);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MarcaDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao realizar pesquisa no banco de dados.", ex);
        }
        return retorno;
    }

    public Marca buscar(Marca marca) throws DAOException {
        Marca retorno = buscar(marca.getId());
        return retorno;
    }

    public Marca buscar(int id) throws DAOException {
        String sql = "SELECT * FROM marca WHERE id=?";
        Marca retorno = new Marca();
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet resultado = stmt.executeQuery();
            if (resultado.next()) {
                retorno.setId(resultado.getInt("id"));
                retorno.setNome(resultado.getString("nome"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MarcaDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao realizar Pequisa no banco de dados");
        }
        return retorno;
    }
}
