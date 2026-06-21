package br.edu.ifsc.fln.model.dao;

import br.edu.ifsc.fln.model.domain.*;
import br.edu.ifsc.fln.model.exceptions.DAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModeloDAO {

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void inserir(Modelo modelo) throws DAOException {
        String sql1 = "INSERT INTO modelo(descricao, marca_id, categoria) VALUES(?,?,?)",
        sql2 = "INSERT INTO motor(id_modelo, potencia, tipo_combustivel) VALUES((SELECT MAX(id) from modelo),?,?)";

        try {
            connection.setAutoCommit(false);

            PreparedStatement stmt = connection.prepareStatement(sql1);
            stmt.setString(1, modelo.getDescricao());
            stmt.setInt(2, modelo.getMarca().getId());
            stmt.setString(3,modelo.getCategoria().name());
            stmt.execute();

            stmt = connection.prepareStatement(sql2);
            stmt.setInt(1, modelo.getMotor().getPotencia());
            stmt.setString(2, modelo.getMotor().getTipoCombustivel().name());
            stmt.execute();

            connection.commit();
        }
        catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                Logger.getLogger(ModeloDAO.class.getName()).log(Level.SEVERE, null, e);
            }
            Logger.getLogger(ModeloDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao realizar registro no banco de dados.", ex);
        }
        finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                Logger.getLogger(ModeloDAO.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }


    public void alterar(Modelo modelo) throws DAOException {
        String sql1 = "UPDATE modelo SET descricao=?, marca_id=?, categoria=? WHERE id=?",
        sql2 = "UPDATE motor SET potencia=?, tipo_combustivel=? WHERE id_modelo=?";

        try {
            connection.setAutoCommit(false);

            PreparedStatement stmt = connection.prepareStatement(sql1);
            stmt.setString(1, modelo.getDescricao());
            stmt.setInt(2, modelo.getMarca().getId());
            stmt.setString(3, modelo.getCategoria().name());
            stmt.setInt(4,modelo.getId());
            stmt.execute();

            stmt = connection.prepareStatement(sql2);
            stmt.setInt(1,modelo.getMotor().getPotencia());
            stmt.setString(2,modelo.getMotor().getTipoCombustivel().name());
            stmt.setInt(3, modelo.getId());
            stmt.execute();
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                Logger.getLogger(ModeloDAO.class.getName()).log(Level.SEVERE, null, e);
            }
            Logger.getLogger(ModeloDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao alterar dados do registro no banco de dados.", ex);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                Logger.getLogger(ModeloDAO.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public void remover(Modelo modelo) throws DAOException {
        String sql = "DELETE FROM modelo WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, modelo.getId());
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(ModeloDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao exluir registro no banco de dados.", ex);
        }
    }

    public List<Modelo> listar() throws DAOException {
        String sql = """
        SELECT
        mrc.id AS id_marca,
        mrc.nome AS nome_marca,
        mdl.id AS id_modelo,
        mdl.descricao AS descricao_modelo,
        mdl.categoria AS categoria_modelo,
        mot.potencia AS potencia_motor,
        mot.tipo_combustivel AS combustivel_motor
        FROM modelo mdl INNER JOIN marca mrc ON mdl.marca_id = mrc.id
            INNER JOIN motor mot on mdl.id = mot.id_modelo ORDER BY mdl.id
        """;

        List<Modelo> modelos = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultado = stmt.executeQuery();
            while (resultado.next()) {
                Modelo modelo = populateVO(resultado);
                modelos.add(modelo);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ModeloDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao realizar pesquisa no banco de dados.", ex);
        }
        return modelos;
    }

    public Modelo buscar(Modelo modelo) throws DAOException {
        Modelo retorno = buscar(modelo.getId());
        return retorno;
    }

    public Modelo buscar(int id) throws DAOException {
        String sql = """
        SELECT
        mrc.id AS id_marca,
        mrc.nome AS nome_marca,
        mdl.id AS id_modelo,
        mdl.descricao AS descricao_modelo,
        mdl.categoria AS categoria_modelo,
        mot.potencia AS potencia_motor,
        mot.tipo_combustivel AS combustivel_motor
        FROM modelo mdl INNER JOIN marca mrc ON mdl.marca_id = mrc.id
            INNER JOIN motor mot on mdl.id = mot.id_modelo WHERE mdl.id = ?
        """;

        Modelo modelo = new Modelo();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet resultado = stmt.executeQuery();
            if (resultado.next()) {
                modelo = populateVO(resultado);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ModeloDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao realizar pesquisa no banco de dados.", ex);

        }
        return modelo;
    }

    private Modelo populateVO(ResultSet rs) throws SQLException {
        Modelo modelo = new Modelo();
        Marca marca = new Marca();
        modelo.setMarca(marca);

        marca.setId(rs.getInt("id_marca"));
        marca.setNome(rs.getString("nome_marca"));
        modelo.setId(rs.getInt("id_modelo"));
        modelo.setDescricao(rs.getString("descricao_modelo"));
        modelo.setCategoria(Enum.valueOf(ECategoria.class, rs.getString("categoria_modelo")));
        modelo.getMotor().setPotencia(rs.getInt("potencia_motor"));
        modelo.getMotor().setTipoCombustivel(Enum.valueOf(ETipoCombustivel.class, rs.getString("combustivel_motor")));

        return modelo;
    }
}