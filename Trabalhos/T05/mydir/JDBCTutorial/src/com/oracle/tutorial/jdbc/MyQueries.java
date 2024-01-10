package com.oracle.tutorial.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MyQueries {

  Connection con;
  JDBCUtilities settings;

  public MyQueries(Connection connArg, JDBCUtilities settingsArg) {
    this.con = connArg;
    this.settings = settingsArg;
  }

  public static void getMyData(Connection con) throws SQLException {
    Statement stmt = null;

    // para mostrar apenas os fornecedores que possuem cafés cadastrados
    String query = "SELECT s.sup_name, count(c.cof_name) " +
        "FROM coffees c INNER JOIN suppliers s ON s.sup_id = c.sup_id " +
        "GROUP BY s.sup_name";

    // para mostrar todos os fornecedores, mesmo que não possuam cafés cadastrados
    // String query = "SELECT s.sup_name, count(c.cof_name) " +
    // "FROM suppliers s LEFT JOIN coffees c ON s.sup_id = c.sup_id " +
    // "GROUP BY s.sup_name";

    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      System.out.println("Fornecedores de café acompanhados da quantidade de tipos de cafés vendidos para a loja:");
      System.out.println("---------------------------------------------------------------------------------------");
      while (rs.next()) {
        String supplierName = rs.getString(1);
        System.out.println("     " + supplierName);
        String qtd = rs.getString(2);
        System.out.println("     " + qtd);
      }
      System.out.println("---------------------------------------------------------------------------------------");
    } catch (SQLException e) {
      JDBCUtilities.printSQLException(e);
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

      MyQueries.getMyData(myConnection);

    } catch (SQLException e) {
      JDBCUtilities.printSQLException(e);
    } finally {
      JDBCUtilities.closeConnection(myConnection);
    }

  }
}
