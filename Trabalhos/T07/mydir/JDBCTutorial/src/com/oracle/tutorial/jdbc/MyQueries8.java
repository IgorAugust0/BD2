package com.oracle.tutorial.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// T07 - 06)
public class MyQueries8 {

  Connection con;
  JDBCUtilities settings;

  public MyQueries8(Connection connArg, JDBCUtilities settingsArg) {
    this.con = connArg;
    this.settings = settingsArg;
  }

  public static void modifyPrices(Connection con) throws SQLException {
    Statement stmt = null;
    try {
      stmt = con.createStatement();

      // Erro: definir ResultSet.CONCUR_READ_ONLY, indica-se explicitamente que o
      // conjunto de resultados (ResultSet) é somente leitura, ou seja, não pode ser
      // atualizado. Sendo que está logo abaixo tenta-se atualizar o valor do preço.
      // A abordagem correta é definir ResultSet.CONCUR_UPDATABLE, que indica que o
      // conjunto de resultados pode ser atualizado.
      stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
      ResultSet uprs = stmt.executeQuery("SELECT * FROM COFFEES");
      while (uprs.next()) {
        float f = uprs.getFloat("PRICE");

        // Erro: o método updateFloat não possui uma assinatura que aceite uma string e
        // um double como parâmetros, deve receber um valor float, portanto, é
        // necessário apenas inserir "f" após o valor: uprs.updateFloat("PRICE", f *
        // 1.005f);
        uprs.updateFloat("PRICE", f * 1.005f); // não indica erro pois acrescentei o "f" após o valor
        uprs.updateRow();
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
