import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

public class ReadFile {
    public static void main(String[] args) throws IOException {
        BufferedReader inputStream = null;
        Scanner scannedLine = null;
        String line;
        String[] values = new String[7];
        int countV;

        try {
            inputStream = new BufferedReader(new FileReader("debito-populate-table.txt"));
            while ((line = inputStream.readLine()) != null) {
                countV = 0;
                System.out.println("<<");

                // Split fields separated by tab delimiters
                scannedLine = new Scanner(line);
                scannedLine.useDelimiter("\t");

                while (scannedLine.hasNext()) {
                    values[countV++] = scannedLine.next();
                    System.out.println(values[countV - 1]);
                }

                System.out.println(">>");

                System.out.println(
                        "insert into debito (numero_debito, valor_debito, motivo_debito, data_debito, numero_conta, nome_agencia, nome_cliente) "
                                +
                                "values (" + values[0] + ", " + values[1] + ", " + values[2] + ", '" + values[3] + "', "
                                + values[4] + ", '" + values[5] + "', '" + values[6] + "');");
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
