package com.oracle.tutorial.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MyQueries2 {

  Connection con;
  JDBCUtilities settings;

  public MyQueries2(Connection connArg, JDBCUtilities settingsArg) {
    this.con = connArg;
    this.settings = settingsArg;
  }

  public static void getMyData(Connection con) throws SQLException {
    Statement stmt = null;
    // Consulta para retornar os nomes de todos os clientes que possuem depósitos,
    // bem como a soma de depósitos de cada cliente
    String query = "SELECT d.nome_cliente, SUM(d.saldo_deposito)" +
        "FROM deposito d WHERE d.nome_cliente NOT IN" +
        "(SELECT e.nome_cliente FROM emprestimo e)" +
        "GROUP BY d.nome_cliente;";
    // nessa consulta, o subselect retorna os nomes dos clientes que possuem empréstimos e o select principal retorna os nomes dos clientes que não estão na lista retornada pelo subselect

    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      System.out.println("\nSoma dos depósitos dos clientes:\n");
      // Imprime o cabeçalho da tabela com o nome das colunas e a largura de cada
      // coluna usando o método format da classe String
      System.out.format("%-30s %10s\n", "Nome do Cliente", "Saldo Total");
      System.out.println("------------------------------------------");
      while (rs.next()) {
        String name = rs.getString(1);
        String sum = rs.getString(2);
        System.out.format("%-30s %10s\n", name, sum); // Imprime os valores das células com respectiva largura de coluna
      }
      System.out.println("");
    } catch (SQLException e) {
      JDBCUtilities.printSQLException(e);
    } catch (Exception e) {
      throw new RuntimeException("unhandled", e);
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
      MyQueries2.getMyData(myConnection);

    } catch (SQLException e) {
      JDBCUtilities.printSQLException(e);
    } finally {
      JDBCUtilities.closeConnection(myConnection);
    }

  }
}