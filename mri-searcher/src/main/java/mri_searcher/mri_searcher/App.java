package mri_searcher.mri_searcher;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.IndexWriterConfig.OpenMode;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
      Indexer indexer = new Indexer(Paths.get("index"),Paths.get("Collection"), OpenMode.CREATE_OR_APPEND);
      try {
			indexer.index();
		} catch (IOException e) {
			System.err.println("Falló la indexación :^(");
		}
    }
}
