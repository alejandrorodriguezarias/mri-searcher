package mri_searcher_parsers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Cran_FileReader {

	// Recibe por parametros el path de un fichero y devuelve la lista de querys
	// del mismo.
	public static final ArrayList<String> readQuery(String path) {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(path));
			String temporal = new String(encoded, StandardCharsets.UTF_8);
			String[] temporal2 = temporal.split(".I");

			ArrayList<String> listaquery = new ArrayList<String>();

			// Elimina el numero en la query
			for (int i = 1; i < temporal2.length; i++) {
				
				listaquery.add(temporal2[i].split(".W")[1]);
			}
			return listaquery;
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}

	public static final ArrayList<String> readRelevance(String path) {
		try {
			String line;

			InputStream fis = new FileInputStream(path);
			InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
			BufferedReader br = new BufferedReader(isr);
			ArrayList<String> listarelev = new ArrayList<String>();

			while ((line = br.readLine()) != null) {
				String[] temporal = line.split(" ");
				int indice = Integer.parseInt(temporal[0]);
				if (listarelev.size() < indice) {
					listarelev.add(temporal[1]);
				} else {
					String tmp = listarelev.get(indice - 1);
					tmp = tmp + "," + temporal[1];
					listarelev.set(indice - 1, tmp);
				}
			}
			
			return listarelev;
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
			System.exit(1);
			return null;
		}
	}
}
/*
 * public static void main( String[] args){
 * 
 * try { readRelevance("/home/alejandro/git/mri-searcher/cran/cranqrel"); }
 * catch (IOException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); System.err.println("No se pudo leer el fichero"); }
 * 
 * }
 * 
 * }
 */