package mri_searcher.mri_searcher;

import java.util.HashMap;
import java.util.Map;

class CommandLine {
	
	private final Map<String,String> opts = new HashMap<>();

	public void triturar(String[] args) {
		opts.clear();

		String current = "";
		StringBuilder sb = new StringBuilder();
		
		for(int i=0; i<args.length; i++) {
			if(args[i].startsWith("-")) {
				if(sb.length() != 0) {
					sb.deleteCharAt(sb.length()-1); // remove trailing whitespace
					opts.put(current, sb.toString()); // add to dict
					sb.setLength(0); // reset length
				}
				current = args[i];
			} else {
				sb.append(args[i] + " ");
			}
		}
		
		// Add remaining options
		if(sb.length() != 0) {
			sb.deleteCharAt(sb.length()-1); // remove trailing whitespace
			opts.put(current, sb.toString()); // add to dict
			sb.setLength(0); // reset length
		}
	}

	/**
	 * Obtiene un argumento de línea de comandos por su nombre.
	 * @param name - nombre del argumento
	 * @return El argumento de línea de comandos especificado por name
	 */
	public String getOpt(String name) {
		return opts.get(name);
	}

	/**
	 * Como getOpt pero tira una excepción si no hay.
	 * @param name
	 * @return El argumento de línea de comandos especificado por name
	 * @throws MissingArgumentException
	 */
	public String checkOpt(String name) throws MissingArgumentException {
		if(!opts.containsKey(name)) throw new MissingArgumentException();
		return getOpt(name);
	}
	
	public boolean hasOpt(String name) {
		return opts.containsKey(name);
	}
	
	/**
	 * Comprueba si está presente alguno de los conjuntos de opciones especificados
	 * en opts.
	 * @param opts Array de arrays de opciones. Cada elemento de este array es un array
	 * de opciones que de estar presentes en el diccionario hacen que el valor devuelto
	 * sea verdadero.
	 */
	private boolean checkPresent(String[][] opts) {
		boolean result = false;
		for(int i=0; i<opts.length; i++) {
			boolean partial = true;
			for(int j=0; j<opts[i].length; j++) {
				partial &= hasOpt(opts[i][j]); // elementos del mismo array son necesarios (AND)
			}
			result |= partial; // cada uno de los arrays es suficiente (OR)
		}
		return result;
	}
	
	/**
	 * Establece si los argumentos entregados por línea de comandos corresponden
	 * a una operación de creación de índices (parte 1 de la práctica)
	 */
	public boolean isIndexing() {
		String[][] indexingOpts = {{"-index","-indexingmodel","-coll"}};
		return checkPresent(indexingOpts);
	}
	
	/**
	 * Establece si los argumentos entregados por línea de comandos corresponden
	 * a una operación de procesamiento de índice (parte 2 de la práctica)
	 */
	public boolean isSearching() {
		String[][] queringOpts = {{"-search","-indexin","-cut","-top","-queries","-fieldsproc","-fieldsvisual"}};
		return checkPresent(queringOpts);
	}

	class MissingArgumentException extends Exception {
		private static final long serialVersionUID = 7146926153071567017L;
	}
}
