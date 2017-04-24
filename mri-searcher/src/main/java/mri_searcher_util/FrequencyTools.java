package mri_searcher_util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;

public class FrequencyTools {

	private FrequencyTools() {
	}

	/**
	 * Este método devuelve los top mejores términos de acuerdo con su idf de
	 * aquellos presentes en querycontent para un índice.
	 * 
	 * @param reader
	 *            - IndexReader para el índice
	 * @param queryContent
	 *            - Lista de String con los términos de la query a evaluar
	 * @param field
	 *            - Campo sobre el que se realiza el proceso
	 * @param top
	 *            - Número de términos que queremos obtener
	 * @return - Un array de String con los términos deseados
	 * @throws IOException
	 */
	public static double obteneridf(TermsEnum termsEnum, int N) throws IOException {

		int df_T = termsEnum.docFreq();
		return Math.log(N / df_T);
	}

	public static String[] getBestTermsByIdf(IndexReader reader, String queryContent, String field, int top)
			throws IOException {
		int N = reader.numDocs();
		// Creando la lista de terminos y el iterador
		Fields fields = MultiFields.getFields(reader);
		Terms terms = fields.terms(field);
		List<String> queryContentsplit = Arrays.asList(queryContent.split(" "));
		TermsEnum termsEnum = terms.iterator();
		String[] topterms = new String[top];
		double[] topidfs = new double[top];

		while (termsEnum.next() != null) {
			String nombrestring = termsEnum.term().utf8ToString();
			if (queryContentsplit.contains(nombrestring)) {
				// OBTENER IDF
				int df_T = termsEnum.docFreq();
				double idf = Math.log(N / df_T);

				// buscamos el mínimo del array de idfs
				int minindex = 0;
				double min = topidfs[minindex];
				for (int i = 1; i < top; i++) {
					if (topidfs[i] < min) {
						min = topidfs[i];
						minindex = i;
					}
				}

				// si el nuevo es mayor que el mínimo, lo sustituímos
				if (min < idf) {
					topidfs[minindex] = idf;
					topterms[minindex] = nombrestring;
				}
			}
		}
		// Ordenamos la lista por idf
		return topterms;
	}

	public static List<String> calcularRelevantes(TopDocs topDocs, IndexReader reader, int ndr) throws IOException {
		int i = 0;
		List<String> relevantes = new ArrayList<String>();

		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			i++;
			Document doc = reader.document(scoreDoc.doc);
			String campoI = doc.get("I");
			relevantes.add(campoI);
			if (i == ndr) {
				return relevantes;
			}
		}

		return relevantes;
	}

	public static List<String> getBestTermsByTfIdf(IndexReader reader, String field,
			TopDocs topDocs, int top, int ndr) throws IOException {

		final int N = reader.numDocs();
		Fields fields = MultiFields.getFields(reader);
		Terms terms = fields.terms(field);
		TermsEnum termsEnum = terms.iterator();
		List<String> terminosTfIdf = new ArrayList<String>();
		List<String> topTerminos = new ArrayList<String>();
		List<String> relevantes = new ArrayList<String>();

		while (termsEnum.next() != null) {

			// RELEVANTES
			relevantes = calcularRelevantes(topDocs, reader, ndr);

			// OBTENER IDF
			double idf = obteneridf(termsEnum, N);

			// OBTENER string campo
			BytesRef nombre = termsEnum.term();
			String nombrestring = nombre.utf8ToString();
			String termino;

			// OBTENER POSTINNGS
			PostingsEnum lista;
			lista = termsEnum.postings(null, PostingsEnum.FREQS);

			if (lista != null) {
				int docx;
				while ((docx = lista.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
					Document doc = reader.document(docx);
					String campoI = doc.get("I"); // Numero de documento
					if (relevantes.contains(campoI)) {
						// double tf_docOrigen; // Tf en el documento
						double tf_doc; // Calculo de tf con log
						// OBTENEMOS EL TF
						tf_doc = lista.freq();
						// tf_docOrigen = tf_doc;
						if (tf_doc > 0) {
							tf_doc = 1 + Math.log(tf_doc);

						} else
							tf_doc = 0;

						// Calculamos TFIDF
						double tfidf = tf_doc * idf;
						// Añadimos los valores a la lista.
						/*
						 * termino = "TF*IDF: " + tfidf + " ( docid: " + docx +
						 * ", termino: " + nombrestring + " )" + " tf: " +
						 * tf_docOrigen + " idf: " + idf;
						 */
						termino = tfidf + "," + nombrestring;
						terminosTfIdf.add(termino);
					}
				}
			}
		}
		Collections.sort(terminosTfIdf);
		Collections.reverse(terminosTfIdf);
		for (int j = 0; j < top; j++) {
			String[] division = terminosTfIdf.get(j).split(",");
			topTerminos.add(division[1]);

		}
		return topTerminos;
	}
}
