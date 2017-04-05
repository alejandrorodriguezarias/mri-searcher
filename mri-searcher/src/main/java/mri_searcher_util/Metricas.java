package mri_searcher_util;

import java.util.List;
import java.util.stream.Collectors;

public class Metricas {

	/**
	 * Métrica de precisión P@n: Coge los primeros n resultados, obtiene cuantos de estos son relevantes y
	 * lo divide por el parámetro n
	 * @param n - parámetro de P@n
	 * @param docs - lista de resultados de la búsqueda
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
	 * @param docs - lista de resultados de la búsqueda
	 * @param rels - lista de documentos relevantes conocidos previamente
	 * @return el valor de precisión según esta métrica
	 */
	static final float Recallatn(int n, List<Integer> docs, List<Integer> rels) {
		List<Integer> ndocs = docs.subList(0, n);
		int intersec = ndocs.stream().filter(rels::contains).collect(Collectors.toSet()).size();
		return intersec / (float) rels.size();
	}
}
