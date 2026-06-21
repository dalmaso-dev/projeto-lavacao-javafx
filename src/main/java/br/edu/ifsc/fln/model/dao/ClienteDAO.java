package br.edu.ifsc.fln.model.dao;

import br.edu.ifsc.fln.model.domain.Cliente;
import br.edu.ifsc.fln.model.domain.PessoaFisica;
import br.edu.ifsc.fln.model.domain.PessoaJuridica;
import br.edu.ifsc.fln.model.exceptions.DAOException;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mpisc
 */
public class ClienteDAO {

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void inserir(Cliente cliente) throws DAOException {
        String sqlCliente = "INSERT INTO cliente(nome, celular, email, data_cadastro) VALUES(?, ?, ?, ?)";

        String sqlPontuacao = "INSERT INTO pontuacao(id_cliente, quantidade) VALUES ((SELECT max(id) FROM cliente), ?)";

        String sqlPJ = "INSERT INTO pessoa_juridica(id_cliente, cnpj, inscricao_estadual) VALUES ((SELECT max(id) FROM cliente), ?, ?)";

        String sqlPF = "INSERT INTO pessoa_fisica(id_cliente, cpf, data_nascimento) VALUES ((SELECT max(id) FROM cliente), ?, ?)";
        try {
            connection.setAutoCommit(false);

            //armazena os dados da superclasse
            PreparedStatement stmt = connection.prepareStatement(sqlCliente);
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getCelular());
            stmt.setString(3, cliente.getEmail());
            stmt.setObject(4, cliente.getDataCadastro());
            stmt.execute();

            //armazena os dados de pontuação do cliente
            stmt = connection.prepareStatement(sqlPontuacao);
            stmt.setInt(1, cliente.getPontuacao().saldo());
            stmt.execute();

            //armazena os dados da subclasse
            if (cliente instanceof PessoaJuridica) {
                stmt = connection.prepareStatement(sqlPJ);
                stmt.setString(1, ((PessoaJuridica)cliente).getCnpj());
                stmt.setString(2, ((PessoaJuridica)cliente).getInscricaoEstadual());
                stmt.execute();
            } else {
                stmt = connection.prepareStatement(sqlPF);
                stmt.setString(1, ((PessoaFisica)cliente).getCpf());
                stmt.setObject(2, ((PessoaFisica)cliente).getDataNascimento());
                stmt.execute();
            }
            connection.commit();
        } catch (SQLException ex) {

            try {
                connection.rollback();
                System.out.println("rollback executado com sucesso!!!");
            } catch (SQLException e) {
                Logger.getLogger(ClienteDAO.class.getName()).log(Level.SEVERE, null, e);
                System.out.println("falha na operação roolback...");
            }

            Logger.getLogger(ClienteDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao realizar registro no banco de dados.", ex);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                Logger.getLogger(ClienteDAO.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public void alterar(Cliente cliente) throws DAOException {
        String sqlCliente =     "UPDATE cliente SET nome=?, celular=?, email=? WHERE id=?";
        String sqlPontuacao =   "UPDATE pontuacao SET quantidade=? WHERE id_cliente=?";
        String sqlPF =          "UPDATE pessoa_fisica SET cpf=?, data_nascimento=? WHERE id_cliente = ?";
        String sqlPJ =          "UPDATE pessoa_juridica SET cnpj=?, inscricao_estadual=? WHERE id_cliente= ?";

        try {
            connection.setAutoCommit(false);

            //update na tabela cliente
            PreparedStatement stmt = connection.prepareStatement(sqlCliente);
            stmt.setString(1, cliente.getNome());
            stmt.setString(2, cliente.getCelular());
            stmt.setString(3, cliente.getEmail());
            stmt.setInt(4, cliente.getId());
            stmt.execute();

            //update na tabela pontuacao
            stmt = connection.prepareStatement(sqlPontuacao);
            stmt.setInt(1, cliente.getPontuacao().saldo());
            stmt.setInt(2, cliente.getId());
            stmt.execute();

            //update nas tabelas filhas
            if (cliente instanceof PessoaFisica) {
                stmt = connection.prepareStatement(sqlPF);
                stmt.setString(1, ((PessoaFisica)cliente).getCpf());
                stmt.setObject(2, ((PessoaFisica)cliente).getDataNascimento());
                stmt.setInt(3, cliente.getId());
                stmt.execute();
            } else {
                stmt = connection.prepareStatement(sqlPJ);
                stmt.setString(1, ((PessoaJuridica)cliente).getCnpj());
                stmt.setString(2, ((PessoaJuridica)cliente).getInscricaoEstadual());
                stmt.setInt(3, cliente.getId());
                stmt.execute();
            }
        } catch (SQLException ex) {
            try {
                connection.rollback();
                System.out.println("rollback executado com sucesso!!!");
            } catch (SQLException e) {
                System.out.println("falha na operação roolback...");
                Logger.getLogger(ClienteDAO.class.getName()).log(Level.SEVERE, null, e);
            }

            Logger.getLogger(ClienteDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao alterar registro no banco de dados.", ex);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                Logger.getLogger(ClienteDAO.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public void remover(Cliente cliente) throws DAOException {
        String sql = "DELETE FROM cliente WHERE id=?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, cliente.getId());
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(ClienteDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao exluir registro no banco de dados.", ex);
        }
    }

    public List<Cliente> listar() throws DAOException {
        String sql = """
            SELECT
            c.id as id,
            c.nome as nome,
            c.celular as celular,
            c.email as email,
            c.data_cadastro as data_cadastro,
            p.quantidade as saldo_pontuacao,
            pf.cpf as cpf,
            pf.data_nascimento as data_nasc,
            pj.cnpj as cnpj,
            pj.inscricao_estadual as insc_estadual
            FROM cliente c
            INNER JOIN pontuacao p on c.id = p.id_cliente
            LEFT JOIN pessoa_fisica pf on pf.id_cliente = c.id
            LEFT JOIN pessoa_juridica pj on pj.id_cliente = c.id
            """;
        List<Cliente> retorno = new ArrayList<>();
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultado = stmt.executeQuery();
            while (resultado.next()) {
                Cliente cliente = populateVO(resultado);
                retorno.add(cliente);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClienteDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao realizar pesquisa no banco de dados.", ex);
        }
        return retorno;
    }

    public Cliente buscar(Cliente cliente) throws DAOException {
        return buscar(cliente.getId());
    }

    public Cliente buscar(int id) throws DAOException {
        String sql = """
            SELECT
            c.id as id,
            c.nome as nome,
            c.celular as celular,
            c.email as email,
            c.data_cadastro as data_cadastro,
            p.quantidade as saldo_pontuacao,
            pf.cpf as cpf,
            pf.data_nascimento as data_nasc,
            pj.cnpj as cnpj,
            pj.inscricao_estadual as insc_estadual
            FROM cliente c
            INNER JOIN pontuacao p on c.id = p.id_cliente
            LEFT JOIN pessoa_fisica pf on pf.id_cliente = c.id
            LEFT JOIN pessoa_juridica pj on pj.id_cliente = c.id WHERE c.id = ?;
            """;

        Cliente retorno = null;
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet resultado = stmt.executeQuery();
            if (resultado.next()) {
                retorno = populateVO(resultado);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ClienteDAO.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Falha ao realizar pesquisa no banco de dados.", ex);
        }
        return retorno;
    }

    private Cliente populateVO(ResultSet rs) throws SQLException {
        Cliente cliente;

        if (rs.getString("cpf") == null) {
            //é um cliente do tipo pessoa jurídica
            cliente = new PessoaJuridica();
            ((PessoaJuridica)cliente).setCnpj(rs.getString("cnpj"));
            ((PessoaJuridica)cliente).setInscricaoEstadual(rs.getString("insc_estadual"));
        } else {
            //é um cliente tipo pessoa física
            cliente = new PessoaFisica();
            ((PessoaFisica)cliente).setCpf(rs.getString("cpf"));
            ((PessoaFisica)cliente).setDataNascimento(rs.getObject("data_nasc", LocalDate.class));
        }

        cliente.setId(rs.getInt("id"));
        cliente.setNome(rs.getString("nome"));
        cliente.setCelular(rs.getString("celular"));
        cliente.setEmail(rs.getString("email"));
        cliente.setDataCadastro(rs.getObject("data_cadastro", LocalDate.class));
        cliente.getPontuacao().adicionar(rs.getInt("saldo_pontuacao"));

        return cliente;
    }
}
