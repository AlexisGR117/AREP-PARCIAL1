package edu.eci.arep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;

/**
 *  Fachada de servicios que delega el cómputo a ReflexCalculator.
 *
 * @author Jefer Alexis Gonzalez Romero
 * @version 1.0 (20/02/2024)
 */
public class ServiceFacade {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String GET_URL = "http://localhost:36001/compreflex?";

    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(36000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 36000.");
            System.exit(1);
        }
        boolean running = true;
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            String outputLine = "";
            inputLine = in.readLine();
            if (inputLine != null) {
                URI uri = new URI(inputLine.split(" ")[1]);
                String path = uri.getPath();
                String query = uri.getQuery();
                if (path.startsWith("/computar")) outputLine = invokeRestService(query);
                else if (path.startsWith("/calculadora")) outputLine = webClient();
                out.println(outputLine);
            }
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }


    /**
     * Entrega el cliente web en formato html + js.
     *
     * @return String con la respuesta de la petición.
     */
    public static String webClient() {
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <title>Calculadora</title>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1>Calculadora</h1>\n" +
                "        <form action=\"/hello\">\n" +
                "            <label for=\"command\">Comando:</label><br>\n" +
                "            <input type=\"text\" id=\"command\" name=\"name\" value=\"sin\"><br><br>\n" +
                "            <label for=\"params\">Parametros (Si es más de uno, toca separarlos por comas sin espacios):</label><br>\n" +
                "            <input type=\"text\" id=\"params\" name=\"name\" value=\"-3.67\"><br><br>\n" +
                "            <input type=\"button\" value=\"Calcular\" onclick=\"loadGetMsg()\">\n" +
                "        </form> \n" +
                "        <h3>Resultado</h3>\n" +
                "        <div id=\"getrespmsg\"></div>\n" +
                "\n" +
                clientJS() +
                "    </body>";
    }

    /**
     * Da el código de java script necesario para el cliente.
     *
     * @return String con el script de javascript.
     */
    public static String clientJS() {
        return  "        <script>\n" +
                "            function loadGetMsg() {\n" +
                "                let commandVar = document.getElementById(\"command\").value;\n" +
                "                let paramsVar = document.getElementById(\"params\").value;\n" +
                "                const xhttp = new XMLHttpRequest();\n" +
                "                xhttp.onload = function() {\n" +
                "                    document.getElementById(\"getrespmsg\").innerHTML =\n" +
                "                    this.responseText;\n" +
                "                }\n" +
                "                xhttp.open(\"GET\", \"/computar?comando=\"+commandVar +\"(\" + paramsVar +\")\");\n" +
                "                xhttp.send();\n" +
                "            }\n" +
                "        </script>\n";
    }

    /**
     * Hace la petición para ReflexCalculator para que haga el cómputo.
     *
     * @param query Comando y parámetros para hacer el cómputo.
     * @return El resultado del cómputo dado por ReflexCalculator.
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    public static String invokeRestService(String query) throws IOException {
        String outputLine = "";
        URL obj = new URL(GET_URL + query);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader inC = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLineC;
            StringBuffer response = new StringBuffer();
            while ((inputLineC = inC.readLine()) != null) {
                response.append(inputLineC);
            }
            outputLine = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: application/json\r\n"
                    + "\r\n" + response;
            inC.close();
        } else {
            System.out.println("GET request not worked");
        }
        System.out.println("GET DONE");
        return outputLine;
    }
}
