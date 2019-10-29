package searcher;

/**
 * Klasa odpowiadająca za wyjątek niepoprawnego argumentu.
 * 
 * @author Karol Soczewica
 * @version 2019.06.21
 */
public class WrongArgument extends Exception {

	/**
	 * Atrybut przechowujący numer wersji.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Konstruktor przyjmujący napis jako parametr.
	 * 
	 * @param s obiekt klasy String
	 */
	public WrongArgument(String s) {
		super(s);
	}

}
