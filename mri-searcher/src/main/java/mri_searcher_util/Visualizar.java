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
	
	private static String presentarQuery(int query, IndexReader reader, TopDocs topDocs, final String[] fields, float[] metricas) throws IOException {
		StringBuilder sb = new StringBuilder();
		String queryName = DiccionarioQueries.getContent(query);
		List<Integer> resultados = new ArrayList<>();
		List<Integer> relevantes = DiccionarioQueries.getRelevants(query);
		
		// Obtenemos los documentos
		for(ScoreDoc scoreDoc : topDocs.scoreDocs) {
			Document doc = reader.document(scoreDoc.doc);
			String score = Float.toString(scoreDoc.score);
			
			// añadimos la ID propia del documento a la lista de resultados
			resultados.add(Integer.parseInt(doc.getField("I").stringValue()));
			
			// Obtenemos los campos
			for(String f : fields) {
				sb.append(f + " : " + doc.getField(f).stringValue() );
			}
		}
		
		// Obtenemos las métricas
		metricas[P10] = Metricas.Patn(10, resultados, relevantes);
		metricas[R10] = Metricas.Recallatn(10, resultados, relevantes);
		metricas[P20] = Metricas.Patn(20, resultados, relevantes);
		metricas[R20] = Metricas.Recallatn(20, resultados, relevantes);
		
		sb.append("Métricas: P@10 " + metricas[P10] + " Recall@10 " + metricas[R10] + " P@20 " + metricas[P20] + " Recall@20 " + metricas[R20]);
		
		return null;
		
	}
	
	public static final String visualizar() {
		return null;
	}
}
