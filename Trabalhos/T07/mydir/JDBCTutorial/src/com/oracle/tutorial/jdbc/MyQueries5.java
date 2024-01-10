package com.oracle.tutorial.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// T07 - 03)
public class MyQueries5 {

  Connection con;
  JDBCUtilities settings;

  public MyQueries5(Connection connArg, JDBCUtilities settingsArg) {
    this.con = connArg;
    this.settings = settingsArg;
  }

  public static void getMyData3(Connection con) throws SQLException {
    Statement stmt = null;
    String query = "SELECT " +
        "c.nome_cliente AS Cliente, " +
        "co.nome_agencia AS Agencia, " +
        "co.numero_conta AS Conta, " +
        "SUM(COALESCE(d.saldo_deposito, 0) - COALESCE(e.valor_emprestimo, 0)) AS Saldo_Total " +
        "FROM cliente AS c " +
        "LEFT JOIN conta AS co ON co.nome_cliente = c.nome_cliente " +
        "LEFT JOIN deposito AS d ON d.numero_conta = co.numero_conta AND d.nome_agencia = co.nome_agencia " +
        "LEFT JOIN emprestimo AS e ON e.numero_conta = co.numero_conta AND e.nome_agencia = co.nome_agencia " +
        "GROUP BY Cliente, Agencia, Conta " +
        "ORDER BY Cliente";

    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);

      System.out.printf("\n%-12s | %-12s | %-12s | %-12s%n", "Cliente", "Agencia", "Conta", "Saldo Total");
      System.out.println("-------------------------------------------------------");

      while (rs.next()) {
        String nome_cliente = rs.getString(1);
        String agencia = rs.getString(2);
        Integer conta = rs.getInt(3);
        Integer saldo_total = rs.getInt(4);

        System.out.println(nome_cliente + ", " + agencia + ", " + conta.toString()
            + ", " + saldo_total.toString());
      }
      System.out.println();

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
      getMyData3(myConnection);

    } catch (SQLException e) {
      JDBCUtilities.printSQLException(e);
    } finally {
      JDBCUtilities.closeConnection(myConnection);
    }

  }
}
