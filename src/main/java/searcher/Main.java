package searcher;

import java.io.IOException;

/**
 * Klasa zawierająca metodę main.
 * 
 * @author Karol Soczewica
 * @version 2019.06.21
 */
public class Main {
	/**
	 * Metoda main.
	 * 
	 * @param args tablica argumentów podanych przy uruchamianiu programu
	 */
	public static void main(String[] args) {
		try {
			Searcher searcher = new Searcher();
			searcher.searchAndPrintResults();
			searcher.closeReaders();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
