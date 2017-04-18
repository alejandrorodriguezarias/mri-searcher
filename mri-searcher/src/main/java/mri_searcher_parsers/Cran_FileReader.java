package mri_searcher_parsers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Cran_FileReader {

	//Recibe por parametros el path de un fichero y devuelve la lista de querys del mismo.
	public static final ArrayList readQuery(String path) throws IOException {

		byte[] encoded = Files.readAllBytes(Paths.get(path));
		String temporal = new String(encoded, StandardCharsets.UTF_8);
		String[] temporal2 = temporal.split(".I");

		ArrayList<String> listaquery = new ArrayList<String>();

		// Elimina el numero en la query
		for (int i = 1;i<3; i++) {
			listaquery.add(temporal2[i].split(".W")[1]);
		}
		return listaquery;
	}
	
	public static void main( String[] args){
		
		try {
			readQuery("/home/alejandro/git/mri-searcher/cran/cran.qry");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("No se pudo leer el fichero");
		}
		
	}

}
/*
 * static String readFile(String path, Charset encoding) throws IOException {
 * byte[] encoded = Files.readAllBytes(Paths.get(path)); return new
 * String(encoded, encoding); }
 */