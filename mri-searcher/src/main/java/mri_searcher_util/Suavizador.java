package mri_searcher_util;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;



public class Suavizador {
	
	final static String usage = "java es.udc.fic.mri_indexer.IndexFiles"
			+ " -index INDEX_PATH -coll DOC_PATH [-openmode CREATE|CREATE_OR_APPEND|APPEND]"
			+ "[-indexingmodel default|jm lambda| dir mu";

	public static Similarity seleccionarsuav(String[] suavizadores) {
		Similarity suav = null;
		
		// Default o número erroneo de parametros
		if (suavizadores.length < 2) {
			if (suavizadores[0].equals("default")) {
				suav = new BM25Similarity();
			} else {
				System.err.println("Not enough arguments");
				System.err.println(usage);
				System.exit(1);
			}
		} else {
			// Jelinek-Mercer
			if (suavizadores[0].equals("jm")) {
				float lambda = Float.parseFloat(suavizadores[1]);
				suav = new LMJelinekMercerSimilarity(lambda);
			} else {
				// Dirichlet
				if (suavizadores[0].equals("dir")) {
					float mu = Float.parseFloat(suavizadores[1]);
					suav = new LMDirichletSimilarity(mu);
				} else {
					// Argumento erroneo
					System.err.println("Not enough arguments");
					System.err.println(usage);
					System.exit(1);
				}

			}

		}
		return suav;
	}
}