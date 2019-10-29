package indexer;

import java.util.HashSet;
import java.util.Set;

/**
 * Klasa przechowująca dostępne języki.
 * 
 * @author Karol Soczewica
 * @version 2019.06.21
 */
public class Languages {
	/**
	 * Identyfikator języka polskiego.
	 */
	private static final String polish = "pl";

	/**
	 * Identyfikator języka angielskiego.
	 */
	private static final String english = "en";

	/**
	 * Set przechowujący dostępne języki.
	 */
	private Set<String> languages;

	/**
	 * Konstruktor klasy Languages.
	 */
	public Languages() {
		languages = new HashSet<String>();
		languages.add(polish);
		languages.add(english);
	}

	/**
	 * Akcesor dający w wyniku set dostepnych języków.
	 * 
	 * @return obiekt Set
	 */
	public Set<String> languages() {
		return languages;
	}
}
