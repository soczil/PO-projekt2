package searcher;

/**
 * Klasa odpowiadająca za wyjątek niepoprawnej komendy.
 * 
 * @author Karol Soczewica
 * @version 2019.06.21
 */
public class WrongCommand extends Exception {

	/**
	 * Atrybut przechowujący numer wersji.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Konstruktor przyjmujący napis jako parametr.
	 * 
	 * @param s obiekt klasy String
	 */
	public WrongCommand(String s) {
		super(s);
	}

}
