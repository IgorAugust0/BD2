package com.oracle.tutorial.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// T07 - 10)
public class MyQueries12 {

  Connection con;
  JDBCUtilities settings;

  public MyQueries12(Connection connArg, JDBCUtilities settingsArg) {
    this.con = connArg;
    this.settings = settingsArg;
  }

  public static void insertRow(Connection con, int numero_debito, int numero_conta, int motivo_debito,
      String nome_agencia, double valor_debito, String data_debito, String nome_cliente) throws SQLException {
    Statement stmt = null;
    try {
      stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
          ResultSet.CONCUR_UPDATABLE);

      // Consulta para verificar se já existe um registro com os mesmos valores
      String verificaConsulta = "SELECT * FROM debito WHERE numero_debito = " + numero_debito
          + " AND numero_conta = " + numero_conta
          + " AND motivo_debito = " + motivo_debito
          + " AND nome_agencia = '" + nome_agencia + "'";

      ResultSet dadosExistentes = stmt.executeQuery(verificaConsulta);

      if (dadosExistentes.next()) {
        // Dados já existem na tabela, então exclua-os
        System.out.printf("\nOs dados já existem na tabela. Eles serão deletados para inserção.");
        // Realize a exclusão dos dados existentes
        dadosExistentes.deleteRow();
      }

      // Continue com a inserção dos novos dados
      ResultSet uprs = stmt.executeQuery("SELECT * FROM debito");
      // Mova o cursor para o final do conjunto de resultados
      uprs.last();

      uprs.moveToInsertRow();
      uprs.updateInt("numero_debito", numero_debito);
      uprs.updateInt("numero_conta", numero_conta);
      uprs.updateInt("motivo_debito", motivo_debito);
      uprs.updateString("nome_agencia", nome_agencia);
      uprs.updateDouble("valor_debito", valor_debito);
      uprs.updateDate("data_debito", Date.valueOf(data_debito));
      uprs.updateString("nome_cliente", nome_cliente);
      uprs.insertRow();

      // Exiba os dados inseridos no console
      System.out.println("\nNova linha inserida:");
      System.out.println("Número do débito: " + numero_debito);
      System.out.println("Valor do débito: " + Math.round(valor_debito));
      System.out.println("Motivo do débito: " + motivo_debito);
      System.out.println("Data do débito: " + data_debito);
      System.out.println("Número da conta: " + numero_conta);
      System.out.println("Nome da agência: " + nome_agencia);
      System.out.println("Nome do cliente: " + nome_cliente);
      System.out.println();

    } catch (SQLException e) {
      JDBCTutorialUtilities.printSQLException(e);
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }
  }

  public static void main(String[] args) {
    JDBCUtilities myJDBCUtilities;
    Connection myConnection = null;
    if (args[0] == null) {
      System.err.println("Properties file not specified at command line");
      return;
    } else {
      try {
        myJDBCUtilities = new JDBCUtilities(args[0]);
      } catch (Exception e) {
        System.err.println("Problem reading properties file " + args[0]);
        e.printStackTrace();
        return;
      }
    }

    try {
      myConnection = myJDBCUtilities.getConnection();
      insertRow(myConnection, 2000, 46248, 1, "UFU", 150, "2014-01-23", "Carla Soares Sousa");
      insertRow(myConnection, 2001, 26892, 2, "Glória", 200, "2014-01-23", "Carolina Soares Souza");
      insertRow(myConnection, 2002, 70044, 3, "Cidade Jardim", 500, "2014-01-23", "Eurides Alves da Silva");

    } catch (SQLException e) {
      JDBCUtilities.printSQLException(e);
    } finally {
      JDBCUtilities.closeConnection(myConnection);
    }

  }
}
