package mri_searcher_util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class Visualizar {

	private static final int P10 = 0;
	private static final int R10 = 1;
	private static final int P20 = 2;
	private static final int R20 = 3;
	private static final int AP = 4;

	private static void presentarDocumentos(final StringBuilder sb, final IndexReader reader, final TopDocs topDocs,
			final String[] fields, final List<Integer> resultados, final List<Integer> relevantes, final int top)
			throws IOException {
		// Obtenemos los documentos
		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			Document doc = reader.document(scoreDoc.doc);
			String score = Float.toString(scoreDoc.score);
			// añadimos la ID propia del documento a la lista de resultados
			int campoI = Integer.parseInt(doc.getField("I").stringValue().trim());

			if (resultados.size() < top) {
				if (relevantes.contains(campoI)) {
					sb.append("Número de documento : " + doc.getField("I").stringValue() + "(RELEVANTE) \n");
				} else {
					sb.append("Número de documento : " + doc.getField("I").stringValue());
				}

				// Obtenemos los campos
				for (String f : fields) {
					sb.append(f + " : " + doc.getField(f).stringValue() + "\n");
				}

				sb.append("Score: " + score + "\n");
			}
			resultados.add(campoI);
		}
	}

	private static void presentarQuery(StringBuilder sb, int query, String expQuery, IndexReader reader, TopDocs topDocs,
			TopDocs expDocs, final String[] fields, float[] metricas, float[] expmetricas, int top, int cut, int rfMode) throws IOException {
		String queryName = DiccionarioQueries.getContent(query);
		List<Integer> resultados = new ArrayList<>();
		List<Integer> expresultados = new ArrayList<>();
		List<Integer> relevantes = DiccionarioQueries.getRelevants(query);

		// Auxiliares calculo de AP
		sb.append("******************************************************************\n");
		sb.append("Query " + query + ": " + queryName); // PRINT: LA QUERY

		presentarDocumentos(sb, reader, topDocs, fields, resultados, relevantes, top);

		// Obtenemos las métricas
		metricas[P10] = Metricas.Patn(10, resultados, relevantes);
		metricas[R10] = Metricas.Recallatn(10, resultados, relevantes);
		metricas[P20] = Metricas.Patn(20, resultados, relevantes);
		metricas[R20] = Metricas.Recallatn(20, resultados, relevantes);
		metricas[AP] = Metricas.AveragePrecision(resultados, relevantes, cut);

		sb.append("\nMétricas: P@10 " + metricas[P10] + " Recall@10 " + metricas[R10] + " P@20 " + metricas[P20]
				+ " Recall@20 " + metricas[R20] + " AveragePrecision " + metricas[AP] + "\n\n");

	
		if (rfMode !=0) {
			sb.append("Query " + query + " expandida : " + expQuery); // PRINT: LA QUERY
			presentarDocumentos(sb, reader, expDocs, fields, expresultados, relevantes, top);
			// Obtenemos las métricas de la query expandida
			expmetricas[P10] = Metricas.Patn(10, expresultados, relevantes);
			expmetricas[R10] = Metricas.Recallatn(10, expresultados, relevantes);
			expmetricas[P20] = Metricas.Patn(20, expresultados, relevantes);
			expmetricas[R20] = Metricas.Recallatn(20, expresultados, relevantes);
			expmetricas[AP] = Metricas.AveragePrecision(expresultados, relevantes, cut);

			sb.append("\nMétricas Q. Expandida: P@10 " + expmetricas[P10] + " Recall@10 " + expmetricas[R10] + " P@20 " + metricas[P20]
					+ " Recall@20 " + expmetricas[R20] + " AveragePrecision " + expmetricas[AP] + "\n\n");
		}
	}


	public static final String visualizar(int[] queries, String[] expQueries, IndexReader reader, TopDocs[] topDocs, TopDocs[] expDocs,
			final String[] fields, int top, int cut, int rfMode) throws IOException {
		float sumP10 = 0f, sumR10 = 0f, sumP20 = 0f, sumR20 = 0f, sumAP = 0f;
		float expSumP10 = 0f, expSumR10 = 0f, expSumP20 = 0f, expSumR20 = 0f, expSumAP = 0f;
		float[] metricas = new float[5], expmetricas = new float[5];
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < queries.length; i++) {
			presentarQuery(sb, queries[i], expQueries[i], reader, topDocs[i], expDocs[i], fields, metricas, expmetricas, top, cut,rfMode);
			sumP10 += metricas[P10];
			sumR10 += metricas[R10];
			sumP20 += metricas[P20];
			sumR20 += metricas[R20];
			sumAP += metricas[AP];
			
			if(rfMode != 0) {
				expSumP10 += expmetricas[P10];
				expSumR10 += expmetricas[R10];
				expSumP20 += expmetricas[P20];
				expSumR20 += expmetricas[R20];
				expSumAP += expmetricas[AP];
			}
		}

		sb.append("Promedio métricas: ");
		sb.append("P@10 " + sumP10 / queries.length + " ");
		sb.append("Recall@10 " + sumR10 / queries.length + " ");
		sb.append("P@20 " + sumP20 / queries.length + " ");
		sb.append("Recall@20 " + sumR20 / queries.length + " ");
		sb.append("MAP " + sumAP / queries.length + "\n");
		
		if(rfMode != 0) {
			sb.append("Promedio métricas expandida: ");
			sb.append("P@10 " + expSumP10 / queries.length + " ");
			sb.append("Recall@10 " + expSumR10 / queries.length + " ");
			sb.append("P@20 " + expSumP20 / queries.length + " ");
			sb.append("Recall@20 " + expSumR20 / queries.length + " ");
			sb.append("MAP " + expSumAP / queries.length + " ");
		}

		return sb.toString();
	}
	
	public static final String explain_rf1(int query, List<String> docTerms, List<String> idfsDoc, List<String> tfsDoc, String[] queryTerms, double[] idfsQuery) {
		StringBuilder sb = new StringBuilder();
		System.err.println("EXPLAIN1");
		System.err.println("LFNRKG: " + query);
		sb.append("Query " + query + ": " + DiccionarioQueries.getContent(query) + "\n");
		System.err.println("EXPLAIN2");
		sb.append("Se expande con:\n");
		
		sb.append("\nTérminos extraídos de la query\n");
		for(int i=0; i<queryTerms.length; i++) {
			System.err.println("EXPLAIN3");
			sb.append("Término: " + queryTerms[i] + " idf: " + idfsQuery[i] + "\n");
			System.err.println("EXPLAIN4");
		}
		
		sb.append("\nTérminos extraídos de documentos relevantes\n");
		for(int i=0; i<docTerms.size(); i++) {
			System.err.println("EXPLAIN5");
			sb.append("Término: " + docTerms.get(i) + " idf: " + idfsDoc.get(i) + " tf: " + tfsDoc.get(i) + "\n");
			System.err.println("EXPLAIN6");
		}
		
		return sb.toString();
	}
	
	public static final String explain_prf(int query, List<String> terms, List<String> valor) {
		StringBuilder sb = new StringBuilder();
		sb.append("Query " + query + ": " + DiccionarioQueries.getContent(query) + "\n");
		sb.append("Se expande con:\n");
		
		sb.append("\nTérminos obtenidos mediante RM1\n");
		for(int i=0; i<terms.size(); i++) {
			String[] split = valor.get(i).split(",");
			sb.append("Término: " + terms.get(i) + " P(D): " + split[0] + " P(w|D): " + split[1] + " Query likelihood: " + split[2] + "\n");
		}
		
		return sb.toString();
	}
}
