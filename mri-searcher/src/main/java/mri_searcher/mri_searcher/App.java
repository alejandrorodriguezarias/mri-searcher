package mri_searcher.mri_searcher;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.Similarity;

import mri_searcher.mri_searcher.CommandLine.MissingArgumentException;
import mri_searcher_util.Suavizador;

public class App {
	

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
		}
		if (cl.isSearching()) {
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
		// INDEXINGMODEL
		String[] suavizadores = null;
		Similarity suav = null;
		suavizadores = cl.getOpt("-search").split(" ");
		// Default o número erroneo de parametros
		suav = Suavizador.seleccionarsuav(suavizadores);
		Searcher searcher = new Searcher(indexin, cut, top, queries, fieldsproc, fieldsvisual,suav);
		searcher.search();
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
		suav = Suavizador.seleccionarsuav(suavizadores);

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
