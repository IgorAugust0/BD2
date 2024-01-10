package com.oracle.tutorial.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

// T04 e T06
public class MyQueriesT06 {

  Connection con;
  JDBCUtilities settings;

  public MyQueriesT06(Connection connArg, JDBCUtilities settingsArg) {
    this.con = connArg;
    this.settings = settingsArg;
  }

  public static void populateTable(Connection con) throws SQLException, IOException {
    Statement stmt = null;
    
    try {
      // Inicializar as variáveis
      BufferedReader inputStream = null;
      Scanner scanned_line = null;
      String line;
      String[] value;
      value = new String[7];
      int countv;

      // Abrir o arquivo de entrada e criar o objeto Statement para executar os comandos SQL
      inputStream = new BufferedReader(
          new FileReader("/home/igor/Downloads/mydir/JDBCTutorial/debito-populate-table.txt"));
      stmt = con.createStatement();

      // Limpar a tabela para evitar erro de chave duplicada, violando a integridade referencial
      stmt.executeUpdate("truncate table debito;");

      // Ler o arquivo de entrada linha a linha
      while ((line = inputStream.readLine()) != null) {
        countv = 0;
        System.out.println("<<");
        scanned_line = new Scanner(line);
        scanned_line.useDelimiter("\t"); // Delimitador de campos

        // Ler os campos separados pelo delimitador
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

        System.out.println("Executando DDL/DML:");

        // Executar o comando SQL
        stmt.executeUpdate(query);
      }

      // Fechar o objeto BufferedReader
      if (inputStream != null) {
        inputStream.close();
      }
    } catch (SQLException e) {
      // Tratar exceções SQL
      JDBCUtilities.printSQLException(e);
    } catch (IOException e) {
      // Tratar exceções de I/O
      e.printStackTrace();
    } finally {
      // Fechar o objeto Statement
      if (stmt != null) {
        stmt.close();
      }
    }
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
      //MyQueriesT06.getMyData(myConnection);
      MyQueriesT06.populateTable(myConnection);

    } catch (SQLException e) {
      JDBCUtilities.printSQLException(e);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      JDBCUtilities.closeConnection(myConnection);
    }

  }
}
