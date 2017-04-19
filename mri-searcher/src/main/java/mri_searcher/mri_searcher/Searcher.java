package mri_searcher.mri_searcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import mri_searcher_util.DiccionarioQueries;
import mri_searcher_util.Visualizar;

public class Searcher {

	private static int MAXQUERY = 225;
	
	private Path indexIn;
	private int cut;
	private int top;
	private String queryRange;
	private String[] fieldsproc;
	private String[] fieldsvisual;
	private Similarity suav;
	
	public Searcher(Path indexIn, int cut, int top, String queryRange, String[] fieldsproc, String[] fieldvisual, Similarity suav) {
		this.indexIn = indexIn;
		this.cut = cut;
		this.top = top;
		this.queryRange = queryRange;
		this.fieldsproc = fieldsproc;
		this.fieldsvisual = fieldvisual;
		this.suav = suav;
	}

	private int[] rangeParser(String range) {
		int[] array;
		int start;
		int end;
		if(range.equals("all")) {
			start = 1;
			end = MAXQUERY;
		} else {
			String[] s = range.split("-");
			if(s.length == 2) {
				start = Integer.parseInt(s[0]);
				end = Integer.parseInt(s[1]);
			} else {
				start = end = Integer.parseInt(s[0]);
			}
		}
		array = new int[end-start+1];
		int j = 0;
		for(int i=start; i<=end; i++) {
			array[j++] = i;
		}
		return array;
	}

	public void search() {
		try (
				Directory dirIn = FSDirectory.open(indexIn);
				IndexReader reader = DirectoryReader.open(dirIn);
				) {
			IndexSearcher searcher = new IndexSearcher(reader);
			searcher.setSimilarity(suav);
			QueryParser queryParser = new MultiFieldQueryParser(fieldsproc, new StandardAnalyzer());
			
			int[] queryNumbers = rangeParser(queryRange);
			TopDocs[] topDocs = new TopDocs[queryNumbers.length];
			
			for(int i=0; i<queryNumbers.length; i++) {
				Query query;
				String queryContent = DiccionarioQueries.getContent(queryNumbers[i]);
				try {
					query = queryParser.parse(queryContent);
					//20 NUM DOCS NECESARIO PARA RECALL20 Y P20
					
					topDocs[i] = searcher.search(query, cut > 20 ? cut : 20);
				} catch (ParseException e) {
					System.err.println("No se pudo parsear la query " + queryContent);
					e.printStackTrace();
				}
			}
			System.out.println(Visualizar.visualizar(queryNumbers, reader, topDocs, fieldsvisual,top,cut));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
