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
	private static final int AP  = 4;
	
	private static void presentarQuery(StringBuilder sb, int query, IndexReader reader, TopDocs topDocs, final String[] fields, float[] metricas, int top, int cut) throws IOException {
		String queryName = DiccionarioQueries.getContent(query);
		List<Integer> resultados = new ArrayList<>();
		List<Integer> relevantes = DiccionarioQueries.getRelevants(query);
		
		//Auxiliares calculo de AP
		List<Integer> totales = new ArrayList<>();
		List<Integer> APRelevantes = new ArrayList<>();
		sb.append("Query " + query + ": " + queryName); // 1 PRINT: LA QUERY
		
		// Obtenemos los documentos
		for(ScoreDoc scoreDoc : topDocs.scoreDocs) {
			Document doc = reader.document(scoreDoc.doc);
			String score = Float.toString(scoreDoc.score);
			// añadimos la ID propia del documento a la lista de resultados
			int campoI = Integer.parseInt(doc.getField("I").stringValue().trim());
			
			if (resultados.size()<top) {
				if (relevantes.contains(campoI)) {
					// PRINT 2 3: 2DOCID Y MARCA
					sb.append("Número de documento : " + doc.getField("I").stringValue() + "(RELEVANTE) \n");
				
				} else {
					sb.append("Número de documento : " + doc.getField("I").stringValue()); // 2 PRINT: EL DOCID (CAMPO I)
				}
			
			
				// Obtenemos los campos
				for(String f : fields) {		
					sb.append(f + " : " + doc.getField(f).stringValue() + "\n"); // 4 PRINT: LOS CAMPOS REQUERIDOS
				}
			
				sb.append("Score: " + score + "\n"); // 5 PRINT: EL SCORE
			}
			resultados.add(campoI);
		}
		
		// Obtenemos las métricas
		metricas[P10] = Metricas.Patn(10, resultados, relevantes);
		metricas[R10] = Metricas.Recallatn(10, resultados, relevantes);
		metricas[P20] = Metricas.Patn(20, resultados, relevantes);
		metricas[R20] = Metricas.Recallatn(20, resultados, relevantes);
		metricas[AP]  = Metricas.AveragePrecision(resultados,relevantes,cut);
		
		sb.append("Métricas: P@10 " + metricas[P10] + " Recall@10 " + metricas[R10] + " P@20 " + metricas[P20] + " Recall@20 " + metricas[R20] + " AveragePrecision " + metricas[AP] + "\n\n");		
	}
	
	public static final String visualizar(int[] queries, IndexReader reader, TopDocs[] topDocs, final String[] fields, int top, int cut) throws IOException {
		float sumP10 = 0f, sumR10 = 0f, sumP20 = 0f, sumR20 = 0f, sumAP = 0f;
		float[] metricas = new float[5];
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<queries.length; i++) {		
			presentarQuery(sb, queries[i], reader, topDocs[i], fields, metricas, top, cut);
			sumP10 += metricas[P10];
			sumR10 += metricas[R10];
			sumP20 += metricas[P20];
			sumR20 += metricas[R20];
			sumAP  += metricas[AP];
			
		}
		
		sb.append("Promedio métricas: ");
		sb.append("P@10 " + sumP10/queries.length + " ");
		sb.append("Recall@10 " + sumR10/queries.length + " ");
		sb.append("P@20 " + sumP20/queries.length + " ");
		sb.append("Recall@20 " + sumR20/queries.length + " ");
		sb.append("MAP " + sumAP/queries.length + " ");
		
		return sb.toString();
	}
}
