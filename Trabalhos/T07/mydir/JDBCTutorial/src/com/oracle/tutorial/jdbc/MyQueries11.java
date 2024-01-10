package com.oracle.tutorial.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

// T07 - 09)
public class MyQueries11 {

  Connection con;
  JDBCUtilities settings;

  public MyQueries11(Connection connArg, JDBCUtilities settingsArg) {
    this.con = connArg;
    this.settings = settingsArg;
  }

  public static void populateTable(Connection con) throws SQLException, IOException {
    Statement stmt = null;

    try {
      BufferedReader inputStream = null;
      Scanner scanned_line = null;
      String line;
      String[] value;
      value = new String[7];
      int countv;

      inputStream = new BufferedReader(
          new FileReader("/home/igor/Downloads/mydir/JDBCTutorial/debito-populate-table.txt"));
      stmt = con.createStatement();

      stmt.executeUpdate("truncate table debito;");

      // Desabilitar o commit automático
      con.setAutoCommit(false);

      while ((line = inputStream.readLine()) != null) {
        countv = 0;
        System.out.println("<<");
        scanned_line = new Scanner(line);
        scanned_line.useDelimiter("\t");

        while (scanned_line.hasNext()) {
          System.out.println(value[countv++] = scanned_line.next());
        }

        // Fechar o objeto Scanner
        if (scanned_line != null) {
          scanned_line.close();
        }
        System.out.println(">>");

        // Montar o comando SQL para inserir os dados na tabela debito
        String query = "insert into debito (numero_debito, valor_debito,  motivo_debito, data_debito, numero_conta, nome_agencia, nome_cliente) "
            + "values (" + value[0] + ", " + value[1] + ", " + value[2] + ", '" + value[3] + "', " + value[4] + ", '"
            + value[5] + "', '" + value[6] + "');";
        System.out.println("Adicionando ao lote de comandos:"); // System.out.println("Executando DDL/DML:");

        // Adicionar o comando SQL ao lote de comandos para este objeto Statement
        stmt.addBatch(query);
        // stmt.executeUpdate(query);
      }

      // Executar o lote de comandos para este objeto Statement
      stmt.executeBatch();

      // Reabilitar o commit automático
      con.setAutoCommit(true);

      if (inputStream != null) {
        inputStream.close();
      }
    } catch (SQLException e) {
      JDBCUtilities.printSQLException(e);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }
  }

  // Fiz as seguintes alterações:
  // 1) Adicionei a desabilitação do commit automático com con.setAutoCommit(false); no início do método
  // 2) Adicionei os comandos stmt.addBatch(query); e stmt.executeBatch(); para adicionar e executar o lote de comandos, respectivamente
  // 3) Removi o comando stmt.executeUpdate(query); que executava o comando SQL para inserir os dados na tabela debito pois o comando foi adicionado ao lote de comandos, então não é mais necessário executá-lo
  // 4) Adicionei a reabilitação do commit automático com con.setAutoCommit(true); no final do método


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
      populateTable(myConnection);

    } catch (SQLException e) {
      JDBCUtilities.printSQLException(e);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      JDBCUtilities.closeConnection(myConnection);
    }

  }
}
