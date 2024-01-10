package com.oracle.tutorial.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

// T05 - Estabelencendo conexões
public class MyQueries3 {

  Connection con;
  JDBCUtilities settings;

  public MyQueries3(Connection connArg, JDBCUtilities settingsArg) {
    this.con = connArg;
    this.settings = settingsArg;
  }

  public static void getMyData(Connection con) throws SQLException {
    Statement stmt = null;
    // Consulta para retornar os nomes dos clientes que possuem depósitos e
    // empréstimos (ambos), com as respectivas somas
    String query = "SELECT DISTINCT d.nome_cliente AS \"Nome do Cliente\", round(SUM(d.saldo_deposito)::numeric, 2) AS \"Soma Depósitos\", round(SUM(e.valor_emprestimo)::numeric, 2) AS \"Soma Empréstimos\" "
        +
        "FROM deposito AS d FULL OUTER JOIN emprestimo AS e " +
        "ON d.nome_cliente = e.nome_cliente " +
        "WHERE e.valor_emprestimo IS NOT NULL " +
        "AND d.saldo_deposito IS NOT NULL " +
        "GROUP BY d.nome_cliente;";

    try {
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(query);
      System.out.println("\nSoma de depositos e emprestimos dos clientes: \n");
      List<String[]> rows = new ArrayList<>(); // Lista para armazenar as linhas de dados
      ResultSetMetaData rsmd = rs.getMetaData(); // Obtém os metadados do ResultSet
      int columnCount = rsmd.getColumnCount(); // Obtém o número de colunas no ResultSet
      while (rs.next()) { // Itera pelas linhas de dados
        String[] row = new String[columnCount]; // Inicializa um array para armazenar os valores das células
        for (int i = 1; i <= columnCount; i++) { // Itera pelas colunas
          row[i - 1] = rs.getString(i); // Obtém o valor da célula e adiciona ao array
        }
        rows.add(row); // Adiciona a linha de dados à lista
      }
      printTable(rows, rsmd); // Imprime a tabela de dados
    } catch (SQLException e) {
      JDBCUtilities.printSQLException(e);
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }
  }

  // Imprime uma tabela de dados com base nos nomes e valores das colunas
  private static void printTable(List<String[]> rows, ResultSetMetaData rsmd) throws SQLException {
    // Obtém o número de colunas no ResultSet
    int columnCount = rsmd.getColumnCount();
    // Array para armazenar a largura de cada coluna com base nos nomes das colunas
    int[] columnWidths = new int[columnCount];

    for (int i = 1; i <= columnCount; i++) {
      // Calcula a largura inicial com base nos nomes das colunas
      columnWidths[i - 1] = rsmd.getColumnName(i).length();
    }

    // Itera pelas linhas de dados para encontrar a largura máxima em cada coluna
    for (String[] row : rows) {
      for (int i = 0; i < row.length; i++) {
        // Verifica se o valor da célula é maior que a largura atual da coluna
        if (row[i] != null && row[i].length() > columnWidths[i]) {
          // Atualiza a largura da coluna se o valor da célula for maior
          columnWidths[i] = row[i].length();
        }
      }
    }

    // Inicializa uma StringBuilder para construir a tabela
    StringBuilder sb = new StringBuilder();

    // Constrói a linha de cabeçalho da tabela com os nomes das colunas
    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
      // Adiciona o nome e preenche com espaços para alinhar com a largura da coluna
      sb.append(rsmd.getColumnName(i)).append(" ".repeat(columnWidths[i - 1] - rsmd.getColumnName(i).length() + 2));
    }
    sb.append("\n");

    // Constrói a linha horizontal separadora com hífens
    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
      sb.append("-".repeat(columnWidths[i - 1] + 2)).append("");
    }
    sb.append("\n");

    // Itera pelas linhas de dados para construir a tabela de dados
    for (String[] row : rows) {
      for (int i = 0; i < row.length; i++) {
        // Add valor da célula e preenche com espaços para alinhar com largura da coluna
        sb.append(row[i]).append(" ".repeat(columnWidths[i] - row[i].length() + 2));
      }
      sb.append("\n");
    }

    // Imprime a tabela resultante no console
    System.out.println(sb);
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

      MyQueries3.getMyData(myConnection);

    } catch (SQLException e) {
      JDBCUtilities.printSQLException(e);
    } finally {
      JDBCUtilities.closeConnection(myConnection);
    }

  }
}