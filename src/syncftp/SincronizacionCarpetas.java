/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package syncftp;

/**
 *
 * @author Angel
 */

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.net.ftp.*;

import java.io.*;
import java.net.SocketException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SincronizacionCarpetas {

    // Declaración de variables conexion y carpetas
    private static FTPClient clienteFTP = new FTPClient();
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 21;
    private static final String USUARIO = "Mango";
    private static final String PASSWORD = "1234";
    private static final String LOCAL_FOLDER_PATH = "C:/Users/Angel/Desktop/Instituto/Servicios y procesos/LocalSync";
    private static final String REMOTE_FOLDER_PATH = "/FTPSync";

    // Constructor de la clase SincronizacionCarpetas
    public SincronizacionCarpetas() {
        clienteFTP = new FTPClient();
    }  

    public static void main(String[] args) throws IOException {
        new SincronizacionCarpetas(); // Creamos una instancia de la clase para inicializar la conexión
        conectar(); // Conexión al servidor FTP

        // Iniciamos la sincronización a intervalos regulares
        TimerTask task = new TimerTask() {
            public void run() {
                try {
                    sincronizarCarpetas(); // Método para sincronizar las carpetas
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Timer timer = new Timer();
        long interval = 2000; // Intervalo de sincronización cada 2 segundos
        timer.scheduleAtFixedRate(task, 0, interval);
    }

    // Método para establecer conexión con el servidor FTP
    private static void conectar() throws SocketException, IOException {
        clienteFTP.connect(SERVIDOR, PUERTO); // Conexion al servidor FTP
        int respuesta = clienteFTP.getReplyCode(); // Obtenemos la respuesta del servidor

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

    // Método para sincronizar las carpetas local y remota
    private static void sincronizarCarpetas() throws IOException {
        File localFolder = new File(LOCAL_FOLDER_PATH); // Carpeta local

        // Obtenemos lista de archivos locales y remotos
        File[] localFiles = localFolder.listFiles();
        FTPFile[] remoteFiles = clienteFTP.listFiles(REMOTE_FOLDER_PATH);

        // Sincronizamos los archivos con un bucle for que recorre cada uno de ellos
        for (File localFile : localFiles) {
                String remoteFilePath = REMOTE_FOLDER_PATH + "/" + localFile.getName();
                FTPFile remoteFile = encontrarArchivoRemoto(remoteFiles, localFile.getName());

                // Si el archivo local es más reciente o no existe remotamente, lo subimos al servidor FTP
                if (remoteFile == null || localFile.lastModified() > remoteFile.getTimestamp().getTimeInMillis()) {
                    subirArchivo(localFile, clienteFTP, remoteFilePath);
                } else if (!remoteFile.getName().equals(localFile.getName())) {
                    // Si el archivo remoto tiene un nombre diferente al local, renombrarlo localmente
                    localFile.renameTo(new File(LOCAL_FOLDER_PATH + "/" + remoteFile.getName()));
                }
        }

        // Eliminamos archivos remotos que no existen localmente con un bucle for que recorre cada uno de ellos
        for (FTPFile remoteFile : remoteFiles) {
            File localFile = new File(LOCAL_FOLDER_PATH + "/" + remoteFile.getName());
            if (!localFile.exists()) {
                    borrarArchivo(clienteFTP, REMOTE_FOLDER_PATH + "/" + remoteFile.getName());
            }
        }
    }

    // Método para encontrar un archivo remoto por nombre
    private static FTPFile encontrarArchivoRemoto(FTPFile[] files, String fileName) {
        for (FTPFile file : files) {
            // Recorremos los archivos y comprobamos si tienen el mismo nombre
            if (file.getName().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    // Método para subir un archivo al servidor FTP
    private static void subirArchivo(File file, FTPClient ftpClient, String remoteFilePath) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            // Guardamos el archivo
            boolean subido = ftpClient.storeFile(remoteFilePath, inputStream);
            inputStream.close();
            // Mostramos por pantalla el resultado 
            if (subido) {
                System.out.println("Archivo subido: " + file.getName());
            } else {
                System.out.println("Error al subir el archivo: " + file.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para eliminar un archivo del servidor FTP
    private static void borrarArchivo(FTPClient ftpClient, String remoteFilePath) {
        try {
            // Borramos el archivo
            boolean borrado = ftpClient.deleteFile(remoteFilePath);
            // Mostramos por pantalla el resultado
            if (borrado) {
                System.out.println("Archivo eliminado: " + remoteFilePath);
            } else {
                System.out.println("No se pudo eliminar el archivo: " + remoteFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
