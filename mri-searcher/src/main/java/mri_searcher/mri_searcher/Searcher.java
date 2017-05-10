package mri_searcher.mri_searcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
import mri_searcher_util.FrequencyTools;
import mri_searcher_util.Visualizar;

public class Searcher {

	private final static int MAXQUERY = 225;
	private final static String BODYFIELD = "W";
	private final static int DOCLIMIT = 120; // XXX: Valor inventado

	private final Path indexIn;
	private final int cut;
	private final int top;
	private final short rfMode;
	private final int ndr;
	private final int td;
	private final int tq;
	private final String queryRange;
	private final String[] fieldsproc;
	private final String[] fieldsvisual;
	private final Similarity suav;
	private final boolean explain;
	private final QueryParser queryParser;
	private final float paramSuavizado;

	public Searcher(Path indexIn, int cut, int top, short rfMode, int ndr, int td, int tq, String queryRange,
			String[] fieldsproc, String[] fieldvisual, Similarity suav, float paramSuavizado, boolean explain) {
		this.indexIn = indexIn;
		this.cut = cut;
		this.top = top;
		this.rfMode = rfMode;
		this.ndr = ndr;
		this.td = td;
		this.tq = tq;
		this.queryRange = queryRange;
		this.fieldsproc = fieldsproc;
		this.fieldsvisual = fieldvisual;
		this.suav = suav;
		this.paramSuavizado = paramSuavizado;
		this.queryParser = new MultiFieldQueryParser(this.fieldsproc, new StandardAnalyzer());
		this.explain = explain;
	}

	private int[] rangeParser(String range) {
		int[] array;
		int start;
		int end;
		if (range.equals("all")) {
			start = 1;
			end = MAXQUERY;
		} else {
			String[] s = range.split("-");
			if (s.length == 2) {
				start = Integer.parseInt(s[0]);
				end = Integer.parseInt(s[1]);
			} else {
				start = end = Integer.parseInt(s[0]);
			}
		}
		array = new int[end - start + 1];
		int j = 0;
		for (int i = start; i <= end; i++) {
			array[j++] = i;
		}
		return array;
	}

	public void search() {
		try (Directory dirIn = FSDirectory.open(indexIn); IndexReader reader = DirectoryReader.open(dirIn);) {
			IndexSearcher searcher = new IndexSearcher(reader);
			searcher.setSimilarity(suav);

			int[] queryNumbers = rangeParser(queryRange);
			TopDocs[] topDocs = new TopDocs[queryNumbers.length];
			TopDocs[] expDocs = new TopDocs[queryNumbers.length];
			String[] expQueries = new String[queryNumbers.length];


			for (int i = 0; i < queryNumbers.length; i++) {
				Query query;
				String queryContent = DiccionarioQueries.getContent(queryNumbers[i]);
				
				// para el explain
				List<String> termsTfidf = new ArrayList<>();
				List<String> tfsDoc = new ArrayList<>();
				List<String> idfsDoc = new ArrayList<>();
				double[] idfsQuery = new double[tq];
				String[] queryTerms = null;
				List<String> termsRM1 = null;
				List<String> valuesRM1 = null;
				
				try {
					query = queryParser.parse(queryContent);
					topDocs[i] = searcher.search(query, DOCLIMIT);
					String expQueryContent = null; 
					Query expQuery = null;

					if (rfMode == 1) {
						List<String> tfidf = FrequencyTools.getBestTermsByTfIdf(reader, BODYFIELD, topDocs[i], td, ndr);
						queryTerms = FrequencyTools.getBestTermsByIdf(reader, queryContent, BODYFIELD, tq, idfsQuery);
						for(int j = 0; j<td;j++) {
							String[] split = tfidf.get(j).split(",");
							termsTfidf.add(split[1]);
							tfsDoc.add(split[2]);
							idfsDoc.add(split[3]);
						}
						expQueryContent = queryExpandida("", termsTfidf, queryTerms);
					} else if (rfMode == 2) {
						List<String> titulos = FrequencyTools.obtenerTitulos(reader, topDocs[i], ndr);
						expQueryContent = queryExpandidatitulo(queryContent, titulos);
					} else if (rfMode == 3) {
						valuesRM1 = new ArrayList<String>();
						termsRM1 = FrequencyTools.obtenerRankingRM1(topDocs[i],reader,BODYFIELD,td,ndr,paramSuavizado,false,valuesRM1);
						expQueryContent = queryExpandidatitulo("", termsRM1);
					} else if (rfMode == 4) {
						valuesRM1 = new ArrayList<String>();
						termsRM1 = FrequencyTools.obtenerRankingRM1(topDocs[i],reader,BODYFIELD,td,ndr,paramSuavizado,true,valuesRM1);
						expQueryContent = queryExpandidatitulo("", termsRM1);
					}

					if (rfMode != 0) {
						expQueries[i] = expQueryContent;
						expQuery = queryParser.parse(expQueryContent);
						expDocs[i] = searcher.search(expQuery, DOCLIMIT);
					}

				} catch (ParseException e) {
					System.err.println("No se pudo parsear la query " + queryContent);
					e.printStackTrace();
				}
				
				if(explain) {
					if(rfMode == 1){
						System.out.println(Visualizar.explain_rf1(queryNumbers[i], termsTfidf, tfsDoc, idfsDoc, queryTerms, idfsQuery));
					}
					if(rfMode == 3 || rfMode == 4)
						System.out.println(Visualizar.explain_prf(queryNumbers[i], termsRM1, valuesRM1));
				}
			}
			System.out.println(Visualizar.visualizar(queryNumbers, expQueries, reader, topDocs, expDocs, fieldsvisual,
					top, cut, rfMode));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String queryExpandida(String queryContent, List<String> tfidf, String[] idf) throws ParseException {
		StringBuilder sb = new StringBuilder();
		// sb.append(queryContent);
		for (String s : idf)
			sb.append(s + " ");
		//sb.append("\n");
		for (String s : tfidf)
			sb.append(s + " ");
		return sb.toString();
	}

	private String queryExpandidatitulo(String queryContent, List<String> titulos) throws ParseException {
		StringBuilder sb = new StringBuilder();
		sb.append(queryContent);
		for (String s : titulos)
			sb.append(s + " ");
		return sb.toString();
	}
	
}
