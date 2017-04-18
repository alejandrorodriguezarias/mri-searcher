package mri_searcher_util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mri_searcher_parsers.Cran_FileReader;

public class DiccionarioQueries {
	
	static ArrayList<String> lista = Cran_FileReader.readQuery("cran/cran.qry");
	static ArrayList<String> relev = Cran_FileReader.readRelevance("cran/cranqrel");

	public static final String getContent(int query) {
		return lista.get(query-1);
	}
	
	public static final List<Integer> getRelevants(int query) {
		String[] rels = relev.get(query-1).split(",");
		Integer[] irels = new Integer[rels.length];
		for(int i=0; i<rels.length; i++) { // Parseamos todos los enteros
			irels[i] = Integer.parseInt(rels[i]);
		}
		return Arrays.asList(irels);
		
	}
}
