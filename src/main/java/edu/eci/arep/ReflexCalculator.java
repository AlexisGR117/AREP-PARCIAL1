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

/**
 * Calculadora que hace el cómputo de las funciones.
 *
 * @author Jefer Alexis Gonzalez Romero
 * @version 1.0 (20/02/2024)
 */
public class ReflexCalculator {

    public static void main(String[] args) throws IOException, URISyntaxException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(36001);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 36001.");
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
            String outputLine = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: application/json\r\n"
                    + "\r\n" + "{\"result\":";
            inputLine = in.readLine();
            if (inputLine != null) {
                URI uri = new URI(inputLine.split(" ")[1]);
                String path = uri.getPath();
                String query = uri.getQuery().replace("comando=", "");
                if (path.startsWith("/compreflex")) {
                    String[] commandAndParams = query.split("\\(");
                    String command = commandAndParams[0];
                    double[] array = arrayParams(commandAndParams[1]);
                    if (command.equals("qck")) quickSort(array, 0, array.length - 1);
                    else  mathMethods(array, command);
                    outputLine += Arrays.toString(array) + "}";
                    out.println(outputLine);
                }
            }
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }

    /**
     * Hace el cálculo con Math de java para cada uno de los parámetros dados.
     *
     * @param array Arreglo con los parámetros que se quieren calcular
     * @param command Comando con la operación que se quiere hacer.
     * @throws NoSuchMethodException Thrown when a particular method cannot be found.
     * @throws InvocationTargetException Exception thrown by an invoked method or constructor
     * @throws IllegalAccessException Application tries to reflectively create an instance
     */
    public static void mathMethods(double[] array, String command) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (int i = 0; i < array.length; i++) {
            Method method = Math.class.getMethod(command, double.class);
            array[i] = (double) method.invoke(null, array[i]);
        }
    }

    /**
     * Pasa un string con los parámetros divididos por comas a un arreglo de doubles.
     *
     * @param paramsString String con los parámetros separados por comas.
     * @return Arreglo de doubles con los parámetros a clacular.
     */
    public static double[] arrayParams(String paramsString) {
        String params = paramsString.replace(")", "");
        String[] arrayStrings = params.split(",");
        double[] array = new double[arrayStrings.length];
        for (int i = 0; i < arrayStrings.length; i++) array[i] = Double.parseDouble(arrayStrings[i]);
        return array;
    }

    /**
     * Implementa el algoritmo de quicksort.
     *
     * @param array Arreglo que se quiere ordenar.
     * @param inicio Inicio del arreglo desde donde se quiere comenzar a ordenar.
     * @param fin Fin del arreglo donde se debe parar el ordenamiento.
     */
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
