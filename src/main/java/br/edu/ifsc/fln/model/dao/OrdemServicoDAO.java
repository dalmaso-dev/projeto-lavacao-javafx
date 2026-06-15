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

public class OrdemServicoDAO {
    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void inserir(OrdemServico ordemServico) {
        String sqlOS = "INSERT INTO ordem_servico(total, agenda, desconto, status, id_veiculo) VALUES(?,?,?,?,?)";

        try {
            connection.setAutoCommit(false);

            //CREATE da ordem de serviço na tabela ordem_servico
            PreparedStatement stmt = connection.prepareStatement(sqlOS);
            stmt.setDouble(1, ordemServico.getTotal());
            stmt.setObject(2, ordemServico.getAgenda());
            stmt.setDouble(3, ordemServico.getDesconto());
            stmt.setString(4, ordemServico.getStatus().name());
            stmt.setInt(5, ordemServico.getVeiculo().getId());
            stmt.execute();

            //READ do número da OS gerado para fazer CREATE nos ItemOS
            stmt = connection.prepareStatement("(SELECT MAX(numero) FROM ordem_servico)");
            ResultSet rs = stmt.executeQuery();

            ordemServico.setNumero(rs.getLong("numero_os"));

            //CREATE dos itens da OS
            inserirItemOS(ordemServico);

            connection.commit();
        }
        catch (SQLException ex) {
            Logger.getLogger(OrdemServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
            try {
                connection.rollback();
            } catch (SQLException e) {
                Logger.getLogger(OrdemServicoDAO.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public boolean alterar(OrdemServico ordemServico) {
        String sql1 = "UPDATE ordem_servico SET total=?, desconto=?, status=?, agenda=? WHERE numero=?";
        String sql2 = "DELETE FROM item_os WHERE numero_os=?";

        try {
            connection.setAutoCommit(false);

            //atualizando a ordem de serviço
            PreparedStatement stmt = connection.prepareStatement(sql1);
            stmt.setDouble(1, ordemServico.getTotal());
            stmt.setDouble(2, ordemServico.getDesconto());
            stmt.setString(3, ordemServico.getStatus().name());
            stmt.setObject(4, ordemServico.getAgenda());
            stmt.setLong(5, ordemServico.getNumero());
            stmt.execute();

            //deletando os itens os
            stmt = connection.prepareStatement(sql2);
            stmt.setLong(1, ordemServico.getNumero());
            stmt.execute();

            //inserindo os itens os
            inserirItemOS(ordemServico);


            //inserindo os pontos para cliente
            if (ordemServico.getStatus() == EStatus.FECHADA) {
                // atribuindo e calculando a quantidade de pontos do cliente
                int pontos = ordemServico.getListaItemOS().size() * Servico.getPontos();
                ordemServico.getVeiculo().getProprietario().getPontuacao().adicionar(pontos);

                String sl3 = "UPDATE pontuacao SET quantidade=? WHERE id_cliente=?";

                stmt = connection.prepareStatement(sl3);
                stmt.setInt(1, ordemServico.getVeiculo().getProprietario().getPontuacao().saldo());
                stmt.setInt(2, ordemServico.getVeiculo().getProprietario().getId());
                stmt.execute();
            }

            connection.commit();

            return true;
        } catch (SQLException ex) {
            Logger.getLogger(OrdemServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
            try {
                connection.rollback();
            } catch (SQLException e) {
                Logger.getLogger(OrdemServicoDAO.class.getName()).log(Level.SEVERE, null, e);
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                Logger.getLogger(OrdemServicoDAO.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public boolean remover(OrdemServico ordemServico) {
        String sql1 = "DELETE FROM ordem_servico WHERE numero=?";
        String sql2 = "DELETE FROM item_os WHERE numero_os=?";

        try {
            connection.setAutoCommit(false);

            PreparedStatement stmt = connection.prepareStatement(sql1);
            stmt.setLong(1, ordemServico.getNumero());
            stmt.execute();

            stmt = connection.prepareStatement(sql2);
            stmt.setLong(1, ordemServico.getNumero());
            stmt.execute();

            connection.commit();
            return true;

        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                Logger.getLogger(OrdemServicoDAO.class.getName()).log(Level.SEVERE, null, e);
            }
            Logger.getLogger(OrdemServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                Logger.getLogger(OrdemServicoDAO.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public List<OrdemServico> listar() {
        String sql = """
            SELECT os.numero AS numero_os,
            os.agenda as agenda_os,
            os.desconto as desconto_os,
            os.status as status_os,
            v.placa as placa_veiculo,
            v.observacoes as  observacoes_veiculo,
            cor.nome as cor,
            mdl.descricao as descricao_modelo,
            mdl.categoria as categoria_modelo,
            mrc.nome as nome_marca,
            mtr.tipo_combustivel as combustivel_motor,
            c.id as id_cliente,
            c.nome as nome_cliente,
            c.celular as celular_cliente,
            p.quantidade as quantidade_pontuacao,
            pf.cpf as cpf_cliente,
            pj.cnpj as  cnpj_cliente
            FROM ordem_servico os INNER JOIN veiculo v ON os.id_veiculo=v.id
            INNER JOIN cor ON v.id_cor = cor.id
            INNER JOIN modelo mdl ON v.id_modelo = mdl.id
            INNER JOIN marca mrc ON mdl.marca_id = mrc.id
            INNER JOIN motor mtr ON mdl.id = mtr.id_modelo
            INNER JOIN cliente c ON v.id_cliente = c.id
            INNER JOIN pontuacao p ON c.id = p.id_cliente
            LEFT JOIN pessoa_fisica pf ON c.id = pf.id_cliente
            LEFT JOIN pessoa_juridica pj ON c.id = pj.id_cliente
            """;

        List<OrdemServico> ordemServicos = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet resultado = stmt.executeQuery();
            while (resultado.next()) {
                OrdemServico ordemServico = populateVO(resultado);
                ordemServicos.add(ordemServico);
            }

            for(OrdemServico os : ordemServicos) {
                listarItensOS(os);
            }
        } catch (SQLException ex) {
            Logger.getLogger(OrdemServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ordemServicos;
    }

    public OrdemServico buscar(OrdemServico ordemServico) {
        OrdemServico retorno = buscar(ordemServico.getNumero());
        return retorno;
    }

    public OrdemServico buscar(long numero) {
        String sql = """
            SELECT os.numero AS numero_os,
            os.agenda as agenda_os,
            os.desconto as desconto_os,
            os.status as status_os,
            v.placa as placa_veiculo,
            v.observacoes as  observacoes_veiculo,
            cor.nome as cor,
            mdl.descricao as descricao_modelo,
            mdl.categoria as categoria_modelo,
            mrc.nome as nome_marca,
            mtr.tipo_combustivel as combustivel_motor,
            c.id as id_cliente,
            c.nome as nome_cliente,
            c.celular as celular_cliente,
            p.quantidade as quantidade_pontuacao,
            pf.cpf as cpf_cliente,
            pj.cnpj as  cnpj_cliente
            FROM ordem_servico os INNER JOIN veiculo v ON os.id_veiculo=v.id
            INNER JOIN cor ON v.id_cor = cor.id
            INNER JOIN modelo mdl ON v.id_modelo = mdl.id
            INNER JOIN marca mrc ON mdl.marca_id = mrc.id
            INNER JOIN motor mtr ON mdl.id = mtr.id_modelo
            INNER JOIN cliente c ON v.id_cliente = c.id
            INNER JOIN pontuacao p ON c.id = p.id_cliente
            LEFT JOIN pessoa_fisica pf ON c.id = pf.id_cliente
            LEFT JOIN pessoa_juridica pj ON c.id = pj.id_cliente WHERE os.numero=?
            """;

        OrdemServico ordemServico = new OrdemServico();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setLong(1, numero);
            ResultSet resultado = stmt.executeQuery();
            while (resultado.next()) {
                ordemServico = populateVO(resultado);
            }

            listarItensOS(ordemServico);

        } catch (SQLException ex) {
            Logger.getLogger(OrdemServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ordemServico;
    }

    private OrdemServico populateVO(ResultSet rs) throws SQLException {
        OrdemServico ordemServico = new OrdemServico();
        Veiculo veiculo = new Veiculo();
        Cor cor = new Cor();
        Modelo modelo = new Modelo();
        Marca marca = new Marca();
        Cliente cliente;

        ordemServico.setNumero(rs.getInt("numero_os"));
        ordemServico.setAgenda(rs.getObject("agenda_os", LocalDate.class));
        ordemServico.setStatus(EStatus.valueOf(rs.getString("status_os")));
        ordemServico.setDesconto(rs.getDouble("desconto_os"));

        veiculo.setPlaca(rs.getString("placa_veiculo"));
        veiculo.setObservacoes(rs.getString("observacoes_veiculo"));

        cor.setNome(rs.getString("cor"));

        modelo.setDescricao(rs.getString("descricao_modelo"));
        modelo.setCategoria(Enum.valueOf(ECategoria.class, rs.getString("categoria_modelo")));

        modelo.getMotor().setTipoCombustivel
                (Enum.valueOf(ETipoCombustivel.class, rs.getString("combustivel_motor")));

        marca.setNome(rs.getString("nome_marca"));

        if (rs.getString("cpf_cliente").isEmpty()) {
            cliente = new PessoaJuridica();
            ((PessoaJuridica) cliente).setCnpj(rs.getString("cnpj_cliente"));
        } else {
            cliente = new PessoaFisica();
            ((PessoaFisica) cliente).setCpf(rs.getString("cpf_cliente"));
        }

        cliente.setId(rs.getInt("id_cliente"));
        cliente.setNome(rs.getString("nome_cliente"));
        cliente.setCelular(rs.getString("celular_cliente"));

        cliente.getPontuacao().adicionar(rs.getInt("quantidade_pontuacao"));

        modelo.setMarca(marca);
        veiculo.setModelo(modelo);
        veiculo.setCliente(cliente);
        veiculo.setCor(cor);
        ordemServico.setVeiculo(veiculo);

        return ordemServico;
    }

    private OrdemServico listarItensOS(OrdemServico ordemServico) throws SQLException {
        String sql = """
            SELECT i.valor_servico as valor_item,
            i.observacoes as  observacoes_item,
            s.id as id_servico,
            s.valor as valor_servico,
            s.categoria as categoria_servico,
            s.descricao as  descricao_servico
            FROM item_os i INNER JOIN servico s ON i.id_servico = s.id WHERE i.numero_os=?
            """;

        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setLong(1, ordemServico.getNumero());
        ResultSet resultado = stmt.executeQuery();

        while (resultado.next()) {
            ItemOS itemOS = new ItemOS();
            Servico servico = new Servico();

            servico.setId(resultado.getInt("id_servico"));
            servico.setValor(resultado.getDouble("valor_servico"));
            servico.setCategoria(Enum.valueOf(ECategoria.class, resultado.getString("categoria_servico")));
            servico.setDescricao(resultado.getString("descricao_servico"));

            itemOS.setValorServico(resultado.getDouble("valor_item"));
            itemOS.setObservacoes(resultado.getString("observacoes_item"));
            itemOS.setServico(servico);

            ordemServico.getListaItemOS().add(itemOS);
        }

        return ordemServico;
    }

    private void inserirItemOS(OrdemServico ordemServico) {
        String sqlItemOS = "INSERT INTO item_os(numero_os, id_servico, valor_servico) VALUES(?,?,?)";

        try {
            PreparedStatement stmt = connection.prepareStatement(sqlItemOS);

            stmt.setLong(1, (ordemServico.getNumero()));

            for (ItemOS itemOS : ordemServico.getListaItemOS()) {
                stmt.setInt(2, itemOS.getServico().getId());
                stmt.setDouble(3, itemOS.getValorServico());
                stmt.execute();
            }
        } catch (SQLException ex) {
            Logger.getLogger(OrdemServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
