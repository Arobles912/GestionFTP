/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestorftp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Angel
 */
import java.io.*;
import java.util.zip.*;

import java.io.*;
import java.time.LocalDateTime;
import java.util.zip.*;

public class Compresion {

    public static String comprimirDirectorio() throws IOException {
        System.out.print("Introduce la ruta del archivo: ");
        Scanner sc = new Scanner(System.in);
        String directorioOrigen = sc.nextLine();
        LocalDateTime date = LocalDateTime.now();
        String archivoDestino = date.getDayOfMonth() + "_" + date.getMonthValue() +"_" + date.getYear() +"_" + date.getHour() +"_" + date.getMinute() + ".zip";
        FileOutputStream fos = new FileOutputStream(archivoDestino);
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        File directorio = new File(directorioOrigen);
        zipDirectorio(directorio, directorio.getName(), zipOut);

        zipOut.close();
        fos.close();
        
        System.out.println("Arcxhivo comprimido.");
        return archivoDestino;
    }

    private static void zipDirectorio(File directorio, String nombreDirectorio, ZipOutputStream zipOut) throws IOException {
        if (directorio.isHidden()) {
            return;
        }
        if (directorio.isDirectory()) {
            if (nombreDirectorio.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(nombreDirectorio));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(nombreDirectorio + "/"));
                zipOut.closeEntry();
            }
            File[] archivos = directorio.listFiles();
            for (File archivo : archivos) {
                zipDirectorio(archivo, nombreDirectorio + "/" + archivo.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(directorio);
        ZipEntry zipEntry = new ZipEntry(nombreDirectorio);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int longitud;
        while ((longitud = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, longitud);
        }
        fis.close();
    }
}

