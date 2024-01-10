package com.oracle.tutorial.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

// T07 - 08)
public class MyQueries10 {

  Connection con;
  JDBCUtilities settings;

  public MyQueries10(Connection connArg, JDBCUtilities settingsArg) {
    this.con = connArg;
    this.settings = settingsArg;
  }

  public static void modifyPrices(Connection con) throws SQLException {
    Statement stmt = null;
    try {
      stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
          ResultSet.CONCUR_UPDATABLE);
      ResultSet uprs = stmt.executeQuery("SELECT * FROM DEPOSITO");

      System.out.println("Digite o multiplicador como um numero real (Ex.: 5% = 1.05):");
      try (Scanner in = new Scanner(System.in)) {
        float percentage = in.nextFloat();

        while (uprs.next()) {
          float f = uprs.getFloat("SALDO_DEPOSITO");
          uprs.updateFloat("SALDO_DEPOSITO", f * percentage);
          uprs.updateRow();
        }
      }
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
      modifyPrices(myConnection);

    } catch (SQLException e) {
      JDBCUtilities.printSQLException(e);
    } finally {
      JDBCUtilities.closeConnection(myConnection);
    }

  }
}
