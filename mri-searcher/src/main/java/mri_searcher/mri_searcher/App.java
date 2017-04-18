package mri_searcher.mri_searcher;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import mri_searcher.mri_searcher.CommandLine.MissingArgumentException;

/**
 * Hello world!
 *
 */
public class App {
	/* Sin hacer */

	final static String usage = "java es.udc.fic.mri_indexer.IndexFiles"
			+ " -index INDEX_PATH -coll DOC_PATH [-openmode CREATE|CREATE_OR_APPEND|APPEND]"
			+ "[-indexingmodel default|jm lambda| dir mu";

	/*
	 * public static void main( String[] args ) { Indexer indexer = new
	 * Indexer(Paths.get("index"),Paths.get("Collection"),
	 * OpenMode.CREATE_OR_APPEND); try { indexer.index(); } catch (IOException
	 * e) { System.err.println("Falló la indexación :^("); } }
	 */
	public static void main(String[] args) {
		CommandLine cl = new CommandLine();
		cl.triturar(args);

		if (cl.isIndexing()) {
			System.out.println("Starting indexing");
			indexing(cl); // primera parte de la práctica
		} else if (cl.isSearching()) {
			System.out.println("Starting search");
			searching(cl); // segunda parte de la práctica
		}
	}
	
	public static void searching(CommandLine cl) {
		Path indexin = Paths.get(cl.getOpt("-indexin"));
		Integer cut = Integer.parseInt(cl.getOpt("-cut"));
		Integer top = Integer.parseInt(cl.getOpt("-top"));
		String queries = cl.getOpt("-queries");
		String[] fieldsproc = cl.getOpt("-fieldsproc").split(",");
		String[] fieldsvisual = cl.getOpt("-fieldsvisual").split(",");
		
		
	}

	public static void indexing(CommandLine cl) {
		Path index = null;

		// OPENMODE
		OpenMode openMode = OpenMode.CREATE_OR_APPEND;

		try {
			openMode = OpenMode.valueOf(cl.checkOpt("-openmode"));
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid open mode specified");
			System.err.println(usage);
			System.exit(1);
		} catch (MissingArgumentException e) {
			System.out.println("No open mode specified, asumming CREATE_OR_APPEND");
		}

		// INDEXINGMODEL
		String[] suavizadores = null;
		Similarity suav = null;
		suavizadores = cl.getOpt("-indexingmodel").split(" ");
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
					// Argumetno erroneo
					System.err.println("Not enough arguments");
					System.err.println(usage);
					System.exit(1);
				}

			}

		}

		if (cl.hasOpt("-index")) {
			// single thread
			Path coll = null;
			coll = Paths.get(cl.getOpt("-coll"));
			index = Paths.get(cl.getOpt("-index"));
			Indexer indXr = new Indexer(index, coll, openMode, suav);
			try {
				indXr.index();
			} catch (IOException e) {
				System.err.println("Falló la indexación :^(");

			}
		}
	}
}
