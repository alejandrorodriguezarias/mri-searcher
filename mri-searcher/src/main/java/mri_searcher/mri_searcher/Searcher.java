package mri_searcher.mri_searcher;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import mri_searcher_util.DiccionarioQueries;
import mri_searcher_util.Visualizar;

public class Searcher {

	Path indexIn;
	int cut;
	int top;
	String queryRange;
	String[] fieldsproc;
	String[] fieldsvisual;
	
	public Searcher(Path indexIn, int cut, int top, String queryRange, String[] fieldsproc, String[] fieldvisual) {
		this.indexIn = indexIn;
		this.cut = cut;
		this.top = top;
		this.queryRange = queryRange;
		this.fieldsproc = fieldsproc;
		this.fieldsvisual = fieldvisual;
	}

	private int[] rangeParser(String range) {
		return null;
	}
	
	public void search() {
		try (
				Directory dirIn = FSDirectory.open(indexIn);
				IndexReader reader = DirectoryReader.open(dirIn);
				) {
			IndexSearcher searcher = new IndexSearcher(reader);
			QueryParser queryParser = new MultiFieldQueryParser(fieldsproc, new StandardAnalyzer());
			
			int[] queryNumbers = rangeParser(queryRange);
			TopDocs[] topDocs = new TopDocs[queryNumbers.length];
			
			for(int i=0; i<queryNumbers.length; i++) {
				Query query;
				String queryContent = DiccionarioQueries.getContent(queryNumbers[i]);
				try {
					query = queryParser.parse(queryContent);
					topDocs[i] = searcher.search(query, top);
				} catch (ParseException e) {
					System.err.println("No se pudo parsear la query " + queryContent);
					e.printStackTrace();
				}
			}
		
			Visualizar.visualizar(queryNumbers, reader, topDocs, fieldsvisual);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
