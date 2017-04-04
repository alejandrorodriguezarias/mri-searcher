package mri_searcher.mri_searcher;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import mri_searcher.mri_searcher.CommandLine.MissingArgumentException;



/**
 * Hello world!
 *
 */
public class App 
{
	/* Sin hacer */

	final static String usage = "java es.udc.fic.mri_indexer.IndexFiles"
			+ " -index INDEX_PATH -coll DOC_PATH|-colls DOC_PATH1 ... DOC_PATHN [-openmode CREATE|CREATE_OR_APPEND|APPEND]";
	
    /*public static void main( String[] args )
    {
      Indexer indexer = new Indexer(Paths.get("index"),Paths.get("Collection"), OpenMode.CREATE_OR_APPEND);
      try {
			indexer.index();
		} catch (IOException e) {
			System.err.println("Falló la indexación :^(");
		}
    }*/
	public static void main(String[] args) {
		CommandLine cl = new CommandLine();
		cl.triturar(args);
		
		

		if (cl.isIndexing()) {
			System.out.println("Starting indexing");
			indexing(cl); // primera parte de la práctica
		}
	}
	
		public static void indexing(CommandLine cl) {
			Path index = null;
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

			if (cl.hasOpt("-index")) {
				// single thread
				Path coll = null;
				coll = Paths.get(cl.getOpt("-coll"));
				index = Paths.get(cl.getOpt("-index"));

				Indexer indXr = new Indexer(index, coll, openMode);
				try {
					indXr.index();
				} catch (IOException e) {
					System.err.println("Falló la indexación :^(");
			
				}
		    }
		}
}
