/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestorftp;

import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Angel
 */
import java.io.*;
import java.util.zip.*;
import java.time.LocalDateTime;

public class Compresion {

    // Método para comprimir un directorio y devolver la ruta del archivo ZIP resultante
    public static String comprimirDirectorio() throws IOException {
        System.out.print("Introduce la ruta del archivo: ");
        Scanner sc = new Scanner(System.in);
        String directorioOrigen = sc.nextLine(); // Obtenemos la ruta del directorio a comprimir
        LocalDateTime date = LocalDateTime.now(); // Obtenemos la fecha y hora actual
        String archivoDestino = date.getDayOfMonth() + "_" + date.getMonthValue() + "_" + date.getYear() + "_" + date.getHour() + "_" + date.getMinute() + ".zip"; // Nombre del archivo ZIP resultante
        FileOutputStream fos = new FileOutputStream(archivoDestino);
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        File directorio = new File(directorioOrigen);

        //Llamamos al método zipDirectorio para la compresión, le pasamos el directorio a comprimir y el zipOutoutStream
        zipDirectorio(directorio, directorio.getName(), zipOut);

        zipOut.close();
        fos.close();

        System.out.println("Arcxhivo comprimido."); // Mensaje de confirmación
        return archivoDestino;
    }

    // Método privado para comprimir un directorio
    private static void zipDirectorio(File directorio, String nombreDirectorio, ZipOutputStream zipOut) throws IOException {
        // Verificamos si el directorio es oculto
        if (directorio.isHidden()) {
            return; // Si es oculto, salimos del método
        }

        // Comprobamos si es un directorio
        if (directorio.isDirectory()) {
            // Si el nombre del directorio termina con "/", le indicamos que es un directorio
            if (nombreDirectorio.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(nombreDirectorio)); // Creamos una entrada ZIP para el directorio
                zipOut.closeEntry(); // Cerramos la entrada ZIP
            } else {
                zipOut.putNextEntry(new ZipEntry(nombreDirectorio + "/")); // Creamos una entrada ZIP para el directorio con "/"
                zipOut.closeEntry(); // Cerramos la entrada ZIP
            }

            // Obtenemos la lista de archivos en el directorio
            File[] archivos = directorio.listFiles();
            // Recorremos los archivos y subdirectorios del directorio actual con un bucle for
            for (File archivo : archivos) {
                // Por cada recorrido, llamamos al método zipDirectorio para comprimir cada archivo/subdirectorio dentro del directorio actual
                zipDirectorio(archivo, nombreDirectorio + "/" + archivo.getName(), zipOut);
            }
            return; 
        }

        // Si es un archivo, se procede a comprimirlo
        FileInputStream fis = new FileInputStream(directorio); 
        ZipEntry zipEntry = new ZipEntry(nombreDirectorio); // Creamos una entrada ZIP para el archivo con su nombre completo
        zipOut.putNextEntry(zipEntry); // Agregar la entrada ZIP al archivo ZIP
        byte[] bytes = new byte[1024]; // Creamos un búfer de bytes para la lectura del archivo
        int longitud;
        // Leeemos el archivo y lo escribimos en el archivo ZIP
        while ((longitud = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, longitud); // Escribimos los bytes leídos en el archivo ZIP
        }
        fis.close(); 
    }
}
