package br.edu.ifsc.fln.model.dao;

import br.edu.ifsc.fln.model.domain.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VeiculoDAO {

    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void inserir(Veiculo veiculo) {
        String sql1 = "INSERT INTO veiculo(placa, observacoes, id_cliente, id_cor, id_modelo) VALUES(?,?,?,?,?)";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql1);
            stmt.setString(1, veiculo.getPlaca());
            stmt.setString(2, veiculo.getObservacoes());
            stmt.setInt(3,veiculo.getProprietario().getId());
            stmt.setLong(4,veiculo.getCor().getId());
            stmt.setInt(5,veiculo.getModelo().getId());
            stmt.execute();
        }
        catch (SQLException ex) {
            Logger.getLogger(VeiculoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void alterar(Veiculo veiculo) {
        String sql = "UPDATE veiculo SET placa=?, observacoes=?, id_cor=?,id_cliente=?, id_modelo=? WHERE id=?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, veiculo.getPlaca());
            stmt.setString(2, veiculo.getObservacoes());
            stmt.setLong(3,veiculo.getCor().getId());
            stmt.setLong(4,veiculo.getProprietario().getId());
            stmt.setInt(5,veiculo.getModelo().getId());
            stmt.setInt(6,veiculo.getId());
            stmt.execute();
        } catch (SQLException ex) {
            Logger.getLogger(VeiculoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean remover(Veiculo veiculo) {
        String sql = "DELETE FROM veiculo WHERE id=?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, veiculo.getId());
            stmt.execute();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(VeiculoDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public List<Veiculo> listar() {
        String sql = """
        SELECT
        v.id as id_veiculo, v.placa as placa, v.observacoes as observacoes,
        cor.id as id_cor, cor.nome as nome_cor,
        mdl.id as id_modelo, mdl.descricao as desc_modelo, mdl.categoria as categoria_modelo,
        mot.potencia as potencia, mot.tipo_combustivel as combustivel,
        mrc.id as id_marca, mrc.nome as nome_marca,
        c.id as id_cliente, c.nome as nome_cliente, c.celular as celular_cliente, c.email as email_cliente,
        c.data_cadastro as  data_cadastro,
        p.quantidade as quantidade_pontuacao,
        pf.cpf as cpf, pf.data_nascimento as data_nasc,
        pj.cnpj as cnpj, pj.inscricao_estadual as insc_estadual
        FROM veiculo v INNER JOIN cor ON v.id_cor = cor.id
        INNER JOIN modelo mdl ON v.id_modelo = mdl.id
        INNER JOIN marca mrc ON mdl.marca_id = mrc.id
        INNER JOIN motor mot ON mdl.id = mot.id_modelo
        INNER JOIN cliente c ON c.id= v.id_cliente
        INNER JOIN pontuacao p on c.id = p.id_cliente
        LEFT JOIN pessoa_fisica pf on pf.id_cliente = c.id
        LEFT JOIN pessoa_juridica pj on pj.id_cliente = c.id
        """;

        List<Veiculo> veiculos = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultado = stmt.executeQuery();
            while (resultado.next()) {
                Veiculo veiculo = populateVO(resultado);
                veiculos.add(veiculo);
            }
        } catch (SQLException ex) {
            Logger.getLogger(VeiculoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return veiculos;
    }

    public List<Veiculo> listarPorCliente(Cliente cliente) {
        String sql = """
        SELECT
        v.id as id_veiculo, v.placa as placa, v.observacoes as observacoes,
        cor.id as id_cor, cor.nome as nome_cor,
        mdl.id as id_modelo, mdl.descricao as desc_modelo, mdl.categoria as categoria_modelo,
        mot.potencia as potencia, mot.tipo_combustivel as combustivel,
        mrc.id as id_marca, mrc.nome as nome_marca,
        c.id as id_cliente, c.nome as nome_cliente, c.celular as celular_cliente, c.email as email_cliente,
        c.data_cadastro as  data_cadastro,
        p.quantidade as quantidade_pontuacao,
        pf.cpf as cpf, pf.data_nascimento as data_nasc,
        pj.cnpj as cnpj, pj.inscricao_estadual as insc_estadual
        FROM veiculo v INNER JOIN cor ON v.id_cor = cor.id
        INNER JOIN modelo mdl ON v.id_modelo = mdl.id
        INNER JOIN marca mrc ON mdl.marca_id = mrc.id
        INNER JOIN motor mot ON mdl.id = mot.id_modelo
        INNER JOIN cliente c ON c.id= v.id_cliente
        INNER JOIN pontuacao p on c.id = p.id_cliente
        LEFT JOIN pessoa_fisica pf on pf.id_cliente = c.id
        LEFT JOIN pessoa_juridica pj on pj.id_cliente = c.id
        Where c.id = ?
        """;

        List<Veiculo> veiculos = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, cliente.getId());
            ResultSet resultado = stmt.executeQuery();
            while (resultado.next()) {
                Veiculo veiculo = populateVO(resultado);
                veiculos.add(veiculo);
            }
        } catch (SQLException ex) {
            Logger.getLogger(VeiculoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return veiculos;
    }

    public Veiculo buscar(Veiculo veiculo) {
        Veiculo retorno = buscar(veiculo.getId());
        return retorno;
    }

    public Veiculo buscar(int id) {
        String sql = """
        SELECT
        v.id as id_veiculo, v.placa as placa, v.observacoes as observacoes,
        cor.id as id_cor, cor.nome as nome_cor,
        mdl.id as id_modelo, mdl.descricao as desc_modelo, mdl.categoria as categoria_modelo,
        mot.potencia as potencia, mot.tipo_combustivel as combustivel,
        mrc.id as id_marca, mrc.nome as nome_marca,
        c.id as id_cliente, c.nome as nome_cliente, c.celular as celular_cliente, c.email as email_cliente,
        c.data_cadastro as  data_cadastro,
        p.quantidade as quantidade_pontuacao,
        pf.cpf as cpf, pf.data_nascimento as data_nasc,
        pj.cnpj as cnpj, pj.inscricao_estadual as insc_estadual
        FROM veiculo v INNER JOIN cor ON v.id_cor = cor.id
        INNER JOIN modelo mdl ON v.id_modelo = mdl.id
        INNER JOIN marca mrc ON mdl.marca_id = mrc.id
        INNER JOIN motor mot ON mdl.id = mot.id_modelo
        INNER JOIN cliente c ON c.id= v.id_cliente
        INNER JOIN pontuacao p on c.id = p.id_cliente
        LEFT JOIN pessoa_fisica pf on pf.id_cliente = c.id
        LEFT JOIN pessoa_juridica pj on pj.id_cliente = c.id WHERE v.id = ?
        """;

        Veiculo veiculo = new Veiculo();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet resultado = stmt.executeQuery();
            if (resultado.next()) {
                veiculo = populateVO(resultado);
            }
        } catch (SQLException ex) {
            Logger.getLogger(VeiculoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return veiculo;
    }

    private Veiculo populateVO(ResultSet rs) throws SQLException {
        Cor cor = new Cor();
        Marca marca = new Marca();
        Modelo modelo = new Modelo();
        Veiculo veiculo = new Veiculo();
        Cliente cliente;

        marca.setId(rs.getInt("id_marca"));
        marca.setNome(rs.getString("nome_marca"));

        cor.setId(rs.getInt("id_cor"));
        cor.setNome(rs.getString("nome_cor"));

        modelo.setId(rs.getInt("id_modelo"));
        modelo.setDescricao(rs.getString("desc_modelo"));
        modelo.setCategoria(Enum.valueOf(ECategoria.class, rs.getString("categoria_modelo")));
        modelo.getMotor().setPotencia(rs.getInt("potencia"));
        modelo.getMotor().setTipoCombustivel
                (Enum.valueOf(ETipoCombustivel.class, rs.getString("combustivel")));

        veiculo.setId(rs.getInt("id_veiculo"));
        veiculo.setPlaca(rs.getString("placa"));
        veiculo.setObservacoes(rs.getString("observacoes"));

        if (rs.getString("cpf") != null) {
            //é um cliente do tipo pessoa física
            cliente = new PessoaFisica();
            ((PessoaFisica) cliente).setCpf(rs.getString("cpf"));
            ((PessoaFisica)cliente).setDataNascimento(rs.getObject("data_nasc", LocalDate.class));
        }
        else  {
            //é um cliente do tipo pessoa jurídica
            cliente = new PessoaJuridica();
            ((PessoaJuridica)cliente).setCnpj(rs.getString("cnpj"));
            ((PessoaJuridica)cliente).setInscricaoEstadual(rs.getString("insc_estadual"));
        }

        cliente.setId(rs.getInt("id_cliente"));
        cliente.setNome(rs.getString("nome_cliente"));
        cliente.setCelular(rs.getString("celular_cliente"));
        cliente.setEmail(rs.getString("email_cliente"));
        cliente.setDataCadastro(rs.getObject("data_cadastro", LocalDate.class));
        cliente.getPontuacao().adicionar(rs.getInt("quantidade_pontuacao"));

        modelo.setMarca(marca);
        veiculo.setModelo(modelo);
        veiculo.setCor(cor);
        veiculo.setCliente(cliente);

        return veiculo;
    }
}