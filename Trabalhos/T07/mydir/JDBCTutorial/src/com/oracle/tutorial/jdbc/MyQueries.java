package com.oracle.tutorial.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

/**
 * |-----------------------------------------------|
 * |Trabalho 07 - Recuperando e modificando valores|
 * |-----------------------------------------------|
 * |Igor Augusto Reis Gomes           - 12011BSI290|
 * |Heitor Guimarães da Fonseca Filho - 12011BSI203|
 * |-----------------------------------------------|
 */
public class MyQueries {

  Connection con;
  JDBCUtilities settings;

  public MyQueries(Connection connArg, JDBCUtilities settingsArg) {
    this.con = connArg;
    this.settings = settingsArg;
  }

  // --------------------------------------------------------------------------------------------------------------------------------------
  // 02)
  public static void getMyData3_1(Connection con) throws SQLException {
    Statement stmt = null;
    String query = "SELECT * FROM CONTA";
    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      System.out.println("\nContas da Instituicao Bancaria: ");
      while (rs.next()) {
        Integer conta = rs.getInt(1);
        String agencia = rs.getString(2);
        System.out.println(conta.toString() + ", " + agencia);
      }
    } catch (SQLException e) {
      JDBCUtilities.printSQLException(e);
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }
  }

  // --------------------------------------------------------------------------------------------------------------------------------------
  // 03)
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

  // --------------------------------------------------------------------------------------------------------------------------------------
  // 04)
  public static void cursorHoldabilitySupport_1(Connection conn)
      throws SQLException {
    DatabaseMetaData dbMetaData = conn.getMetaData();
    System.out.println("\nResultSet.HOLD_CURSORS_OVER_COMMIT = " +
        ResultSet.HOLD_CURSORS_OVER_COMMIT);
    System.out.println("ResultSet.CLOSE_CURSORS_AT_COMMIT = " +
        ResultSet.CLOSE_CURSORS_AT_COMMIT);
    System.out.println("Default cursor holdability: " +
        dbMetaData.getResultSetHoldability());
    System.out.println("Supports HOLD_CURSORS_OVER_COMMIT? " +
        dbMetaData.supportsResultSetHoldability(ResultSet.HOLD_CURSORS_OVER_COMMIT));
    System.out.println("Supports CLOSE_CURSORS_AT_COMMIT? "
        + dbMetaData.supportsResultSetHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT) + "\n");
  }

  // --------------------------------------------------------------------------------------------------------------------------------------
  // 05)
  public static void cursorHoldabilitySupport(Connection conn)
      throws SQLException {
    DatabaseMetaData dbMetaData = conn.getMetaData();

    System.out.println("\nSupports TYPE_FORWARD_ONLY with ResultSet.CONCUR_UPDATABLE ? " +
        dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE));
    System.out.println("Supports TYPE_FORWARD_ONLY with ResultSet.CONCUR_READ_ONLY ? " +
        dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY));

    System.out.println("Supports TYPE_SCROLL_INSENSITIVE with ResultSet.CONCUR_UPDATABLE ? " +
        dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE));
    System.out.println("Supports TYPE_SCROLL_INSENSITIVE with ResultSet.CONCUR_READ_ONLY ? " +
        dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY));

    System.out.println("Supports TYPE_SCROLL_SENSITIVE with ResultSet.CONCUR_UPDATABLE ? " +
        dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE));
    System.out.println("Supports TYPE_SCROLL_SENSITIVE with ResultSet.CONCUR_READ_ONLY ? " +
        dbMetaData.supportsResultSetConcurrency(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY));

    System.out.println("\nResultSet.HOLD_CURSORS_OVER_COMMIT = " +
        ResultSet.HOLD_CURSORS_OVER_COMMIT);
    System.out.println("ResultSet.CLOSE_CURSORS_AT_COMMIT = " +
        ResultSet.CLOSE_CURSORS_AT_COMMIT);
    System.out.println("Default cursor holdability: " +
        dbMetaData.getResultSetHoldability());
    System.out.println("Supports HOLD_CURSORS_OVER_COMMIT? " +
        dbMetaData.supportsResultSetHoldability(
            ResultSet.HOLD_CURSORS_OVER_COMMIT));
    System.out.println("Supports CLOSE_CURSORS_AT_COMMIT? " +
        dbMetaData.supportsResultSetHoldability(
            ResultSet.CLOSE_CURSORS_AT_COMMIT)
        + "\n");
  }

  // --------------------------------------------------------------------------------------------------------------------------------------
  // 07)
  public static void modifyPrices_1(Connection con) throws SQLException {
    Statement stmt = null;
    try {
      stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
      ResultSet uprs = stmt.executeQuery("SELECT * FROM DEPOSITO");

      while (uprs.next()) {
        float f = uprs.getFloat("SALDO_DEPOSITO");
        uprs.updateFloat("SALDO_DEPOSITO", f * 1.005F);
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

  // --------------------------------------------------------------------------------------------------------------------------------------
  // 08)
  public static void modifyPrices(Connection con) throws SQLException {
    Statement stmt = null;
    try {
      stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
          ResultSet.CONCUR_UPDATABLE);
      ResultSet uprs = stmt.executeQuery("SELECT * FROM DEPOSITO");

      System.out.println("Digite o multiplicador como um numero real (Ex.: 5% = 1,05):");
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

  // --------------------------------------------------------------------------------------------------------------------------------------
  // 09)
  /*
   * Fiz as seguintes alterações:
   * 1) Adicionei a desabilitação do commit automático com
   * con.setAutoCommit(false); no início do método.
   * 2) Adicionei os comandos stmt.addBatch(query); e stmt.executeBatch(); para
   * adicionar e executar o lote de comandos, respectivamente.
   * 3) Removi o comando stmt.executeUpdate(query); que executava o comando SQL
   * para inserir os dados na tabela debito pois o comando foi adicionado ao lote
   * de comandos, então não é mais necessário executá-lo.
   * 4) Adicionei a reabilitação do commit automático com con.setAutoCommit(true);
   * no final do método.
   */

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

  // --------------------------------------------------------------------------------------------------------------------------------------
  // 10)
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

  // --------------------------------------------------------------------------------------------------------------------------------------

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
      getMyData3_1(myConnection); // 02)
      getMyData3(myConnection); // 03)
      cursorHoldabilitySupport_1(myConnection); // 04)
      cursorHoldabilitySupport(myConnection); // 05)
      modifyPrices_1(myConnection); // 07)
      modifyPrices(myConnection); // 08)
      populateTable(myConnection); // 09)

      // 10)
      insertRow(myConnection, 2000, 46248, 1, "UFU", 150, "2014-01-23", "Carla Soares Sousa");
      insertRow(myConnection, 2001, 26892, 2, "Glória", 200, "2014-01-23", "Carolina Soares Souza");
      insertRow(myConnection, 2002, 70044, 3, "Cidade Jardim", 500, "2014-01-23", "Eurides Alves da Silva");

    } catch (SQLException e) {
      JDBCUtilities.printSQLException(e);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      JDBCUtilities.closeConnection(myConnection);
    }

  }
}
