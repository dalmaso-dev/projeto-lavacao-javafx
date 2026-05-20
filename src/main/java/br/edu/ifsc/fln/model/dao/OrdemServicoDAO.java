package br.edu.ifsc.fln.model.dao;

import br.edu.ifsc.fln.model.domain.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            SELECT *
            FROM ordem_servico os INNER JOIN veiculo v ON os.id_veiculo=v.id 
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
        } catch (SQLException ex) {
            Logger.getLogger(OrdemServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ordemServicos;
    }

    public OrdemServico buscar(OrdemServico ordemServico) {
        OrdemServico retorno = buscar(ordemServico.getId());
        return retorno;
    }

    public OrdemServico buscar(int id) {
        String sql = """
        SELECT
        mrc.id AS id_marca,
        mrc.nome AS nome_marca,
        mdl.id AS id_ordemServico,
        mdl.descricao AS descricao_ordemServico,
        mdl.categoria AS categoria_ordemServico,
        mot.potencia AS potencia_motor,
        mot.tipo_combustivel AS combustivel_motor
        FROM ordemServico mdl INNER JOIN marca mrc ON mdl.marca_id = mrc.id
            INNER JOIN motor mot on mdl.id = mot.id_ordemServico WHERE mdl.id = ?
        """;

        OrdemServico ordemServico = new OrdemServico();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet resultado = stmt.executeQuery();
            if (resultado.next()) {
                ordemServico = populateVO(resultado);
            }
        } catch (SQLException ex) {
            Logger.getLogger(OrdemServicoDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ordemServico;
    }

    private OrdemServico osPopulateVO(ResultSet rs) throws SQLException {
        OrdemServico ordemServico = new OrdemServico();
        Marca marca = new Marca();
        ordemServico.setMarca(marca);

        marca.setId(rs.getInt("id_marca"));
        marca.setNome(rs.getString("nome_marca"));
        ordemServico.setId(rs.getInt("id_ordemServico"));
        ordemServico.setDescricao(rs.getString("descricao_ordemServico"));
        ordemServico.setCategoria(Enum.valueOf(ECategoria.class, rs.getString("categoria_ordemServico")));
        ordemServico.getMotor().setPotencia(rs.getInt("potencia_motor"));
        ordemServico.getMotor().setTipoCombustivel(Enum.valueOf(ETipoCombustivel.class, rs.getString("combustivel_motor")));

        return ordemServico;
    }

    private OrdemServico itemOsPopulateVO(ResultSet resultado) throws SQLException {

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
