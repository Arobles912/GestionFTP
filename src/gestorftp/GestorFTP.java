/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package gestorftp;

/**
 *
 * @author Angel
 */
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTP;

public class GestorFTP {

    // Declaración de variables conexion
    private FTPClient clienteFTP;
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 21;
    private static final String USUARIO = "Angel";
    private static final String PASSWORD = "1234";

    // Constructor de la clase GestorFTP
    public GestorFTP() {
        clienteFTP = new FTPClient();
    }

    // Método privado para conectar al servidor FTP
    private void conectar() throws SocketException, IOException {
        clienteFTP.connect(SERVIDOR, PUERTO); // Conexión con el servidor
        int respuesta = clienteFTP.getReplyCode();

        // Comprobamos si la conexión fue exitosa
        if (!FTPReply.isPositiveCompletion(respuesta)) {
            clienteFTP.disconnect(); // Si no lo fue, nos desconectamos y lanza una excepción
            throw new IOException("Error al conectar con el servidor FTP");
        }

        // Comprobamos si las credenciales son correctas
        boolean credencialesOK = clienteFTP.login(USUARIO, PASSWORD);

        if (!credencialesOK) {
            throw new IOException("Error al conectar con el servidor FTP. Credenciales incorrectas.");
        } else {
            System.out.println("Conectado."); // Mensaje de confirmación
        }

        clienteFTP.setFileType(FTP.BINARY_FILE_TYPE); // Establecemos el tipo de archivo
    }

    // Método privado para desconectar del servidor FTP
    private void desconectar() throws IOException {
        clienteFTP.disconnect();
    }

    // Método privado para subir un archivo al servidor FTP
    private boolean subirFichero(String path) throws IOException {
        File ficheroLocal = new File(path);
        FileInputStream fis = new FileInputStream(ficheroLocal);
        // Guardamos el archivo
        boolean enviado = clienteFTP.storeFile(ficheroLocal.getName(), fis);
        fis.close();
        return enviado; // Devolvemos un booleano
    }

    // Método privado para descargar un archivo desde el servidor FTP
    private boolean descargarFichero(String ficheroRemoto, String pathLocal)
            throws IOException {
        OutputStream os = new BufferedOutputStream(new FileOutputStream(pathLocal));
        // Descargamos el archivo del servidor
        boolean recibido = clienteFTP.retrieveFile(ficheroRemoto, os);
        os.close();
        return recibido; // Devolvemos un booleano
    }


    public static void main(String[] args) {

        String fichero_descarga = "hola.txt"; // Variable con el nombre del fichero a descargar

        GestorFTP gestorFTP = new GestorFTP();
        try {
            gestorFTP.conectar(); // Conexión al servidor FTP
            System.out.println("Conectado");

            Compresion cd = new Compresion(); // Instanciamos la clase Compresion
            String ruta = cd.comprimirDirectorio(); // Comprimimos directorio y obtenemos la ruta del archivo ZIP
            System.out.println("Archivo ZIP creado en: " + ruta);

            // Hilo para subir el archivo al servidor FTP
            Thread subirThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        // Comprobamos si se ha subido correctamente
                        boolean subido = gestorFTP.subirFichero(ruta);
                        if (subido) {
                            System.out.println("Fichero subido correctamente"); // Mensaje de confirmación
                        } else {
                            System.err.println("Ha ocurrido un error al intentar subir el fichero"); // Mensaje de error
                        }
                    } catch (IOException e) {
                        System.err.println("Ha ocurrido un error:" + e.getMessage());
                    }
                }
            });

            // Hilo para descargar un archivo desde el servidor FTP
            Thread descargarThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        // Comprobamos si se ha descargado correctamente, además incluimos la ruta 
                        // de la carpeta donde queremos realizar la descarga, acompañado del nombre del archivo
                        boolean descargado = gestorFTP.descargarFichero(fichero_descarga, "C:\\Users\\Angel\\Desktop\\Instituto\\Servicios y procesos\\FTPServer" + fichero_descarga);
                        if (descargado) {
                            System.out.println("Fichero descargado correctamente"); // Mensaje de confirmación
                        } else {
                            System.err.println("Ha ocurrido un error al intentar descargar el fichero."); // Mensaje de error
                        }
                    } catch (IOException e) {
                        System.err.println("Ha ocurrido un error:" + e.getMessage());
                    }
                }
            });

            subirThread.start(); // Iniciamos el hilo de subida
            try {
                // Esperamos a que el hilo subirThread termine su ejecución
                subirThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            descargarThread.start(); // Iniciamos el hilo de descarga
            try {
                // Esperamos a que el hilo descargarThread termine su ejecución
                descargarThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            gestorFTP.desconectar(); // Desconexión del servidor FTP
            System.out.println("Desconectado");
        } catch (Exception e) {
            System.err.println("Ha ocurrido un error:" + e.getMessage());
        }
    }
}
