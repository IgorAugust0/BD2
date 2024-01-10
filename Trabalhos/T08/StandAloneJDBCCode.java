import java.util.Properties; // Objeto genérico que armazena propriedades com usuário e senha
import java.sql.DriverManager; // Objeto que criará a conexão do sistema de banco de dados
import java.sql.Connection; // Objeto que armazenará o objeto de conexão ao banco de dados
import java.sql.Statement; // Objeto para disparar um comando para o SGBD
import java.sql.ResultSet; // Objeto que armazenará as tuplas resultantes de um comando SQL
import java.sql.SQLException; // Objeto para capturar eventos de erro no acesso ao banco de dados

public class StandAloneJDBCCode {
    /**
     * Cria uma conexão com o banco de dados PostgreSQL.
     * Configura propriedades de conexão, como nome de usuário e senha.
     * Define a URL de conexão com o banco de dados.
     * 
     * @return a conexão com o banco de dados
     */
    public static Connection getConnection() {
        Connection conn = null;
        String currentUrlString = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", "postgres");
        connectionProps.put("password", "..kB@e6PAT%JS3j");
        currentUrlString = "jdbc:postgresql://db.umsozekvkyejwaoazrfl.supabase.co:5432/postgres";

        try {
            conn = DriverManager.getConnection(currentUrlString, connectionProps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * Executa uma consulta SQL na tabela cliente e imprime o nome de cada cliente
     * na saída padrão.
     * 
     * @param conn a conexão JDBC com o banco de dados
     * @throws SQLException se ocorrer um erro ao executar a consulta
     */
    public static void myquery(Connection conn) throws SQLException {
        Statement stmt = null;
        String query = "SELECT nome_cliente FROM cliente";

        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            System.out.println("Lista de nomes: ");
            while (rs.next()) {
                String name_cliente = rs.getString(1);
                System.out.println("\t" + name_cliente);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
     * Fecha a conexão com o banco de dados e libera todos os recursos associados,
     * se a conexão não for nula.
     * 
     * @param conn a conexão a ser fechada
     */
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
            System.out.println("Released all database resources.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Método principal que inicia a execução do programa.
     * Verifica se há argumentos passados por linha de comando e, em seguida,
     * estabelece uma conexão com o banco de dados e executa uma consulta.
     * 
     * @param args Os argumentos passados por linha de comando.
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No arguments.");
        }
        Connection myConnection = null;
        try {
            myConnection = getConnection();
            myquery(myConnection);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection(myConnection);
        }
    }
}
