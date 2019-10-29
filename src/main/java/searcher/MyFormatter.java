package searcher;

import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.TokenGroup;

/**
 * Klasa odpowiadająca za formatowanie wypisywanych wyników wyszukiwania.
 * 
 * @author Karol Soczewica
 * @version 2019.06.21
 */
public class MyFormatter implements Formatter {
	/**
	 * Atrybut przechowuje prefiks potrzebny do wypisywania słowa w kolorze
	 * czerwonym.
	 */
	private static final String DEFAULT_PRE_TAG = "\u001b[31m";

	/**
	 * Atrybut przechowuje sufiks potrzebny do wypisywania słowa w kolorze
	 * czerwonym.
	 */
	private static final String DEFAULT_POST_TAG = "\u001b[0m";

	/**
	 * Atrybut przechowuje prefiks używany przy wypisywania słowa.
	 */
	private String preTag;

	/**
	 * Atrybut przechowuje sufiks używany przy wypisywania słowa.
	 */
	private String postTag;

	/**
	 * Konstruktor klasy MyFormatter.
	 * 
	 * @param color informacja o tym, czy do wypisywania ma zostać użyty kolor
	 */
	public MyFormatter(boolean color) {
		if (color) {
			this.preTag = DEFAULT_PRE_TAG;
			this.postTag = DEFAULT_POST_TAG;
		} else {
			this.preTag = "";
			this.postTag = "";
		}
	}

	/**
	 * Nadpisana metoda odpowiadająca za podświetlanie podanego tekstu.
	 * 
	 * @param originalText tekst, który ma zostać podświetlony
	 * @param tokenGroup   obiekt klasy TokenGroup
	 */
	@Override
	public String highlightTerm(String originalText, TokenGroup tokenGroup) {
		if (tokenGroup.getTotalScore() <= 0) {
			return originalText;
		} else {
			int capacity = preTag.length() + postTag.length() + originalText.length();

			StringBuilder builder = new StringBuilder(capacity);
			builder.append(preTag);
			builder.append(originalText);
			builder.append(postTag);

			return builder.toString();
		}
	}

}
