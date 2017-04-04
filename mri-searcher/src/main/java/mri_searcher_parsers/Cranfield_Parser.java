package mri_searcher_parsers;

import java.util.LinkedList;
import java.util.List;

public class Cranfield_Parser {


	/*private static final String END_BOILERPLATE_1 = "Reuter&#3;";
	private static final String END_BOILERPLATE_2 = "REUTER&#3;";*/

	

	public static List<List<String>> parseString(StringBuffer fileContent) {
		/* First the contents are converted to a string */
		String text = fileContent.toString();

		/*
		 * The method split of the String class splits the strings using the
		 * delimiter which was passed as argument Therefor lines is an array of
		 * strings, one string for each line
		 */
		String[] lines = text.split("\n");


		List<List<String>> documents = new LinkedList<List<String>>();

		/* The tag REUTERS identifies the beginning and end of each article */

		for (int i = 0; i < lines.length; ++i) {
			if (!lines[i].startsWith(".I"))
				continue;
			StringBuilder sb = new StringBuilder();
			while (((i+1)<lines.length) && (!lines[i+1].startsWith(".I"))) {
				sb.append(lines[i++]);
				sb.append("\n");
			}
			
			//Añade las ultimas lineas
			sb.append(lines[i]);
			sb.append("\n");
			sb.append(".I");
			sb.append("\n");
			
			/*
			 * Here the sb object of the StringBuilder class contains the
			 * Reuters article which is converted to text and passed to the
			 * handle document method that will return the document in the form
			 * of a list of fields
			 */
			documents.add(handleDocument(sb.toString()));
		}
		return documents;
	}
	
	public static List<String> handleDocument(String text) {

		/*
		 * This method returns the Reuters article that is passed as text as a
		 * list of fields
		 */

		/* The fields TOPICS, TITLE, DATELINE and BODY are extracted */
		/* Each topic inside TOPICS is identified with a tag D */
		/* If the BODY ends with boiler plate text, this text is removed */

		String I = extract("I","T", text, true);
		String T = extract("T","A", text, true);
		String A = extract("A","B", text, true);
		String B = extract("B","W", text, true);
		String W = extract("W","I", text, true);
		/*if (body.endsWith(END_BOILERPLATE_1)
				|| body.endsWith(END_BOILERPLATE_2))
			body = body
					.substring(0, body.length() - END_BOILERPLATE_1.length());*/
		List<String> document = new LinkedList<String>();
		document.add(I);
		document.add(T);
		document.add(A);
		document.add(B);
		document.add(W);
		return document;
	}
	
	
	private static String extract(String campoAIndexar,String terminoFinIndexar, String text, boolean allowEmpty) {

		/*
		 * This method find the tags for the field elt in the String text and
		 * extracts and returns the content
		 */
		/*
		 * If the tag does not exists and the allowEmpty argument is true, the
		 * method returns the null string, if allowEmpty is false it returns a
		 * IllegalArgumentException
		 */

		String startElt = "." + campoAIndexar;
		String endElt = "." + terminoFinIndexar;
		int startEltIndex = text.indexOf(startElt);
		if (startEltIndex < 0) {
			if (allowEmpty)
				return "";
			throw new IllegalArgumentException("no start, elt=" + campoAIndexar
					+ " text=" + text);
		}
		int start = startEltIndex + startElt.length();
		int end = text.indexOf(endElt, start);
		if (end < 0)
			throw new IllegalArgumentException("no end, elt=" + campoAIndexar + " text="
					+ text);
		return text.substring(start, end);
	}
}
