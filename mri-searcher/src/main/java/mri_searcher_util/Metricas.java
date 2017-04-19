package mri_searcher_util;

import java.util.List;
import java.util.stream.Collectors;

public class Metricas {

	/**
	 * Métrica de precisión P@n: Coge los primeros n resultados, obtiene cuantos de estos son relevantes y
	 * lo divide por el parámetro n
	 * @param n - parámetro de P@n
	 * @param docs - lista de ¡¡ CAMPOS i !! de los resultados de la búsqueda
	 * @param rels - lista de documentos relevantes conocidos previamente
	 * @return el valor de precisión según esta métrica
	 */
	static final float Patn(int n, List<Integer> docs, List<Integer> rels) {
		List<Integer> ndocs = docs.subList(0, n);
		int intersec = ndocs.stream().filter(rels::contains).collect(Collectors.toSet()).size();
		return intersec / (float) n;
	}
	
	/**
	 * Métrica de precisión Recall@n: Coge los primeros n resultados, obtiene cuantos de estos son relevantes
	 * y divide por el número total de relevantes posibles.
	 * @param n - parámetro de Recall@n
	 * @param docs - lista de ¡¡ CAMPOS i !! de los resultados de la búsqueda
	 * @param rels - lista de documentos relevantes conocidos previamente
	 * @return el valor de precisión según esta métrica
	 */
	static final float Recallatn(int n, List<Integer> docs, List<Integer> rels) {
		List<Integer> ndocs = docs.subList(0, n);
		int intersec = ndocs.stream().filter(rels::contains).collect(Collectors.toSet()).size();
		return intersec / (float) rels.size();
	}
	
	/*AP CALCULA LA PRECISION POR APARICION ES DECIR
	  no relevante
	  relevante  1/2
	  no relevante
	  relevante   2/4
	  no relevante
	  no relevante
	  relevante 3/7
	  HASTA VALOR CUT
	  
	  AP = 1/2 + 1/4 + 1/7
	  		--------------
	  		total queriesrelevantes
	  		
	*/
	static final float AveragePrecision(List<Integer> totales, List<Integer> relevantes, int cut) {
		float sumAP = 0;
		int numRel = 0;
		for (int i = 0;i< cut;i++)  {
			if (relevantes.contains(totales.get(i))){
				numRel++;
				sumAP += (numRel/(i+1));
			}
			
		}
		return sumAP/relevantes.size();
		
	}
}
