package mri_searcher_util;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;

public class FrequencyTools {
	
	private FrequencyTools() {}
	
	/**
	 * Este método devuelve los top mejores términos de acuerdo con su idf de aquellos
	 * presentes en querycontent para un índice.
	 * @param reader - IndexReader para el índice
	 * @param queryContent - Lista de String con los términos de la query a evaluar
	 * @param field - Campo sobre el que se realiza el proceso
	 * @param top - Número de términos que queremos obtener
	 * @return - Un array de String con los términos deseados
	 * @throws IOException
	 */
	public static String[] getBestTermsByIdf(IndexReader reader, List<String> queryContent, String field, int top) throws IOException {
		int N = reader.numDocs();
		// Creando la lista de terminos y el iterador
		Fields fields = MultiFields.getFields(reader);
		Terms terms = fields.terms(field);
		
		TermsEnum termsEnum = terms.iterator();
		String[] topterms = new String[top];
		double[] topidfs = new double[top];

		while (termsEnum.next() != null) {
			String nombrestring = termsEnum.term().utf8ToString();
			if (queryContent.contains(nombrestring)) {
				// OBTENER IDF
				int df_T = termsEnum.docFreq();
				double idf = Math.log(N / df_T);
				
				// buscamos el mínimo del array de idfs
				int minindex = 0;
				double min = topidfs[minindex];
				for(int i=1; i<top; i++) {
					if(topidfs[i] < min) {
						min = topidfs[i];
						minindex = i;
					}
				}
				
				// si el nuevo es mayor que el mínimo, lo sustituímos
				if(min < idf) {
					topidfs[minindex] = idf;
					topterms[minindex] = nombrestring;
				}
			}
		}
		// Ordenamos la lista por idf
		return topterms;
	}
}
