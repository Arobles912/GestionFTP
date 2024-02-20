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

public class SincronizacionCarpetas {

    private static FTPClient clienteFTP = new FTPClient();
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 21;
    private static final String USUARIO = "Mango";
    private static final String PASSWORD = "1234";
    private static final String LOCAL_FOLDER_PATH = "C:/Users/Angel/Desktop/Instituto/Servicios y procesos/LocalSync";
    private static final String REMOTE_FOLDER_PATH = "/FTPSync";

    public SincronizacionCarpetas() {
        clienteFTP = new FTPClient();
    }  

    public static void main(String[] args) throws IOException {
        new SincronizacionCarpetas();
        conectar();
        // Iniciar la sincronización
        TimerTask task = new TimerTask() {
            public void run() {
                try {
                    sincronizarCarpetas();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Timer timer = new Timer();
        long interval = 2000; // 1 segundos
        timer.scheduleAtFixedRate(task, 0, interval);
    }

    private static void conectar() throws SocketException, IOException {
        clienteFTP.connect(SERVIDOR, PUERTO);
        int respuesta = clienteFTP.getReplyCode();

        if (!FTPReply.isPositiveCompletion(respuesta)) {
            clienteFTP.disconnect();
            throw new IOException("Error al conectar con el servidor FTP");
        }

        boolean credencialesOK = clienteFTP.login(USUARIO, PASSWORD);

        if (!credencialesOK) {
            throw new IOException("Error al conectar con el servidor FTP. Credenciales incorrectas.");
        } else {
            System.out.println("Conectado.");
        }

        clienteFTP.setFileType(FTP.BINARY_FILE_TYPE);
    }

    private static void sincronizarCarpetas() throws IOException {
        File localFolder = new File(LOCAL_FOLDER_PATH);

        // Obtener lista de archivos locales y remotos
        File[] localFiles = localFolder.listFiles();
        FTPFile[] remoteFiles = clienteFTP.listFiles(REMOTE_FOLDER_PATH);

        // Sincronizar archivos
        for (File localFile : localFiles) {

                String remoteFilePath = REMOTE_FOLDER_PATH + "/" + localFile.getName();
                FTPFile remoteFile = encontrarArchivoRemoto(remoteFiles, localFile.getName());
                if (remoteFile == null || localFile.lastModified() > remoteFile.getTimestamp().getTimeInMillis()) {
                    // Si el archivo local es más reciente o no existe remotamente, cargarlo al servidor FTP
                    subirArchivo(localFile, clienteFTP, remoteFilePath);
                } else if (!remoteFile.getName().equals(localFile.getName())) {
                    // Si el archivo remoto tiene un nombre diferente al local, renombrarlo localmente
                    localFile.renameTo(new File(LOCAL_FOLDER_PATH + "/" + remoteFile.getName()));
                }

        }

        // Eliminar archivos remotos que no existen localmente
        for (FTPFile remoteFile : remoteFiles) {
            File localFile = new File(LOCAL_FOLDER_PATH + "/" + remoteFile.getName());
            if (!localFile.exists()) {
                    borrarArchivo(clienteFTP, REMOTE_FOLDER_PATH + "/" + remoteFile.getName());
            }
        }

    }

    private static FTPFile encontrarArchivoRemoto(FTPFile[] files, String fileName) {
        for (FTPFile file : files) {
            if (file.getName().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    private static void subirArchivo(File file, FTPClient ftpClient, String remoteFilePath) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            boolean subido = ftpClient.storeFile(remoteFilePath, inputStream);
            inputStream.close();
            if (subido) {
                System.out.println("Archivo subido: " + file.getName());
            } else {
                System.out.println("Error al subir el archivo: " + file.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private static void borrarArchivo(FTPClient ftpClient, String remoteFilePath) {
        try {
            boolean borrado = ftpClient.deleteFile(remoteFilePath);
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
