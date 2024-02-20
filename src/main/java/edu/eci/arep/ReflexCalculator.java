package edu.eci.arep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public class ReflexCalculator {
    public static void main(String[] args) throws IOException, URISyntaxException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(36002);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 36002.");
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
            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            String outputLine = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: application/json\r\n"
                    + "\r\n" + "{\"result\":";
            inputLine = in.readLine();
            if (inputLine != null) {
                URI uri = new URI(inputLine.split(" ")[1]);
                String path = uri.getPath();
                String query = uri.getQuery().replace("comando=", "");
                System.out.println(uri);
                if (path.startsWith("/compreflex")) {
                    String[] commandAndParams = query.split("\\(");
                    String command = commandAndParams[0];
                    String params = commandAndParams[1].replace(")", "");
                    if (command.equals("qck")) {
                        String[] arrayStrings = params.split(",");
                        double[] array = new double[arrayStrings.length];
                        for (int i = 0; i < arrayStrings.length; i++) array[i] = Double.parseDouble(arrayStrings[i]);
                        quickSort(array, 0, array.length - 1);
                        outputLine += Arrays.toString(array) + "}";
                    } else {
                        Method method = Math.class.getMethod(command, double.class);
                        double result = (double) method.invoke(null, Double.parseDouble(params));
                        outputLine += result + "}";
                        System.out.println(outputLine);
                    }
                    out.println(outputLine);
                }

            }
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    public static void quickSort(double[] array, int inicio, int fin) {
        if (inicio < fin) {
            double pivote = array[fin];
            int positionPivote = fin;
            for (int i = 0; i < array.length; i++) {
                if (array[i] > pivote && i < positionPivote || array[i] < pivote && i > positionPivote) {
                    array[positionPivote] = array[i];
                    array[i] = pivote;
                    positionPivote = i;
                }
            }
            quickSort(array, inicio, positionPivote - 1);
            quickSort(array, positionPivote + 1, fin);
        }
    }
}
