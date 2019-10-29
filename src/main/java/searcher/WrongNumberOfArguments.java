package searcher;

/**
 * Klasa odpowiadająca za wyjątek niepoprawnej liczby argumentów.
 * 
 * @author Karol Soczewica
 * @version 2019.06.21
 */
public class WrongNumberOfArguments extends Exception {

	/**
	 * Atrybut przechowujący numer wersji.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Konstruktor przyjmujący napis jako parametr.
	 * 
	 * @param s obiekt klasy String
	 */
	public WrongNumberOfArguments(String s) {
		super(s);
	}
}
