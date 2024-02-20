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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTP;

public class GestorFTP {

    private FTPClient clienteFTP;
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 21;
    private static final String USUARIO = "Angel";
    private static final String PASSWORD = "1234";

    public GestorFTP() {
        clienteFTP = new FTPClient();
    }

    private void conectar() throws SocketException, IOException {
        clienteFTP.connect(SERVIDOR, PUERTO);
        int respuesta = clienteFTP.getReplyCode();

        if (!FTPReply.isPositiveCompletion(respuesta)) {
            clienteFTP.disconnect();
            throw new IOException("Error al conectar con el servidor FTP");
        }

        boolean credencialesOK = clienteFTP.login(USUARIO, PASSWORD);

        if (!credencialesOK) {
            throw new IOException("Error al conectar con el servidor FTP. Credenciales incorrectas.");
        }

        clienteFTP.setFileType(FTP.BINARY_FILE_TYPE);
    }

    private void desconectar() throws IOException {
        clienteFTP.disconnect();
    }

    private boolean subirFichero(String path) throws IOException {
        File ficheroLocal = new File(path);
        FileInputStream fis = new FileInputStream(ficheroLocal);
        boolean enviado = clienteFTP.storeFile(ficheroLocal.getName(), fis);
        fis.close();
        return enviado;
    }

    private boolean descargarFichero(String ficheroRemoto, String pathLocal)
            throws IOException {
        OutputStream os = new BufferedOutputStream(new FileOutputStream(pathLocal));
        boolean recibido = clienteFTP.retrieveFile(ficheroRemoto, os);
        os.close();
        return recibido;
    }

    public static void main(String[] args) {

        String fichero_descarga = "hola.txt";

        GestorFTP gestorFTP = new GestorFTP();
        try {
            gestorFTP.conectar();
            System.out.println("Conectado");

            Compresion cd = new Compresion();
            String ruta = cd.comprimirDirectorio();
            System.out.println("Archivo ZIP creado en: " + ruta);

            Thread subirThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        boolean subido = gestorFTP.subirFichero(ruta);
                        if (subido) {
                            System.out.println("Fichero subido correctamente");
                        } else {
                            System.err.println("Ha ocurrido un error al intentar subir el fichero");
                        }
                    } catch (IOException e) {
                        System.err.println("Ha ocurrido un error:" + e.getMessage());
                    }
                }
            });

            Thread descargarThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        boolean descargado = gestorFTP.descargarFichero(fichero_descarga, "C:\\Users\\Angel\\Desktop\\Instituto\\Servicios y procesos\\FTPServer" + fichero_descarga);
                        if (descargado) {
                            System.out.println("Fichero descargado correctamente");
                        } else {
                            System.err.println("Ha ocurrido un error al intentar descargar el fichero.");
                        }
                    } catch (IOException e) {
                        System.err.println("Ha ocurrido un error:" + e.getMessage());
                    }
                }
            });

            subirThread.start();

            try {
                // Esperar a que el hilo subirThread termine su ejecución
                subirThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            descargarThread.start();

            try {
                // Esperar a que el hilo descargarThread termine su ejecución
                descargarThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            gestorFTP.desconectar();
            System.out.println("Desconectado");
        } catch (Exception e) {
            System.err.println("Ha ocurrido un error:" + e.getMessage());
        }
    }
}
