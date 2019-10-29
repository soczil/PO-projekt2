package searcher;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.FSDirectory;

/**
 * Klasa odpowiadająca za działanie wyszukiwarki.
 * 
 * @author Karol Soczewica
 * @version 2019.06.21
 */
public class Searcher {
	/**
	 * Ścieżka do katalogu zawierającego indeks plików w języku polskim.
	 */
	private static final String polishIndexPath = "index/indexPL";

	/**
	 * Ścieżka do katalogu zawierającego indeks plików w języku angielskim.
	 */
	private static final String englishIndexPath = "index/indexEN";

	/**
	 * Atrybut przechowujący obiekt klasy IndexReader dla plików w języku polskim.
	 */
	private IndexReader readerPL;

	/**
	 * Atrybut przechowujący obiekt klasy IndexReader dla plików w języku
	 * angielskim.
	 */
	private IndexReader readerEN;

	/**
	 * Atrybut przechowujący informację o obecnie wybranym języku wyszukiwania.
	 */
	private String language = "en";

	/**
	 * Atrybut przechowujący informację o wyświetlaniu kontekstów dla znalezionych
	 * wyników wyszukiwania.
	 */
	private boolean details = false;

	/**
	 * Atrybut przechowujący informację o limicie wyszukiwań.
	 */
	private int limit = 0;

	/**
	 * Atrybut przechowujący informację o tym, czy podświetlać wyniki wyszukwiania.
	 */
	private boolean color = false;

	/**
	 * Atrybut przechowujący informację o tym czy używać wyszukiwania w postaci
	 * TERM.
	 */
	private boolean termSearch = true;

	/**
	 * Atrybut przechowujący informację o tym czy używać wyszukiwania w postaci
	 * PHRASE.
	 */
	private boolean phraseSearch = false;

	/**
	 * Atrybut przechowujący informację o tym czy używać wyszukiwania w postaci
	 * FUZZY.
	 */
	private boolean fuzzySearch = false;

	/**
	 * Konstruktor klasy Searcher.
	 * 
	 * @throws IOException
	 */
	public Searcher() throws IOException {
		readerPL = DirectoryReader.open(FSDirectory.open(Paths.get(polishIndexPath)));
		readerEN = DirectoryReader.open(FSDirectory.open(Paths.get(englishIndexPath)));
	}

	/**
	 * Metoda zamykająca obiekty klasy IndexReader.
	 * 
	 * @throws IOException
	 */
	public void closeReaders() throws IOException {
		readerPL.close();
		readerEN.close();
	}

	/**
	 * Metoda odpowiadająca za tworzenie obiektu klasy Query do wyszukiwania w opcji
	 * TERM.
	 * 
	 * @param field pole, w którym odbywać się będzie wyszukiwanie
	 * @param text  wyszukiwane słowo
	 * @return obiekt klasy Query
	 */
	private Query searchTerm(String field, String text) {
		Term term = new Term(field, text);
		return new TermQuery(term);
	}

	/**
	 * Metoda odpowiadająca za tworzenie obiektu klasy Query do wyszukiwania w opcji
	 * PHRASE.
	 * 
	 * @param field pole, w którym odbywać się będzie wyszukiwanie
	 * @param terms tablica słów, z których składa się wyszukiwana fraza
	 * @return obiekt klasy Query
	 */
	private Query searchPhrase(String field, String[] terms) {
		return new PhraseQuery(field, terms);
	}

	/**
	 * Metoda odpowiadająca za tworzenie obiektu klasy Query do wyszukiwania w opcji
	 * FUZZY.
	 * 
	 * @param field pole, w którym odbywać się będzie wyszukiwanie
	 * @param text  wyszukiwane słowo
	 * @return obiekt klasy Query
	 */
	private Query searchFuzzy(String field, String text) {
		Term term = new Term(field, text);
		return new FuzzyQuery(term);
	}

	/**
	 * Metoda sprawdzająca liczbę argumentów w poleceniu.
	 * 
	 * @param splittedLine tablica zawierająca podzieloną linię tekstu
	 * @param argsNumber   poprawna liczba argumentów
	 * @throws WrongNumberOfArguments wyjątek złej liczby argumentów
	 */
	private void checkArgumentsNumber(String[] splittedLine, int argsNumber)
			throws WrongNumberOfArguments {
		if (splittedLine.length != argsNumber) {
			throw new WrongNumberOfArguments(splittedLine[0]);
		}
	}

	/**
	 * Metoda zmieniająca sposób wyszukiwania.
	 * 
	 * @param term   informacja, czy używać wyszukiwania w postaci TERM
	 * @param phrase informacja, czy używać wyszukiwania w postaci PHRASE
	 * @param fuzzy  informacja, czy używać wyszukiwania w postaci FUZZY
	 */
	private void searchChange(boolean term, boolean phrase, boolean fuzzy) {
		termSearch = term;
		phraseSearch = phrase;
		fuzzySearch = fuzzy;
	}

	/**
	 * Metoda odpowiedzialna za obsługę polecenia lang.
	 * 
	 * @param command  polecenie
	 * @param argument argument dla polecenia
	 * @throws WrongArgument wyjątek niepoprawnego argumentu
	 */
	private void langCommand(String command, String argument) throws WrongArgument {
		if (argument.equals("pl")) {
			language = "pl";
		} else if (argument.equals("en")) {
			language = "en";
		} else {
			throw new WrongArgument(command);
		}
	}

	/**
	 * Metoda odpowiedzialna za obsługę polecenia details.
	 * 
	 * @param command  polecenie
	 * @param argument argument dla polecenia
	 * @throws WrongArgument wyjątek niepoprawnego argumentu
	 */
	private void detailsCommand(String command, String argument) throws WrongArgument {
		if (argument.equals("on")) {
			details = true;
		} else if (argument.equals("off")) {
			details = false;
		} else {
			throw new WrongArgument(command);
		}
	}

	/**
	 * Metoda odpowiadająca za obsługę polecenia limit.
	 * 
	 * @param argument argument dla polecenia
	 * @throws NumberFormatException
	 */
	private void limitCommand(String argument) throws NumberFormatException {
		limit = Integer.parseInt(argument);
	}

	/**
	 * Metoda odpowiadająca za obsługę polecenia color.
	 * 
	 * @param command  polecenie
	 * @param argument argument dla polecenia
	 * @throws WrongArgument wyjątek niepoprawnego argumentu
	 */
	private void colorCommand(String command, String argument) throws WrongArgument {
		if (argument.equals("on")) {
			color = true;
		} else if (argument.equals("off")) {
			color = false;
		} else {
			throw new WrongArgument(command);
		}
	}

	/**
	 * Metoda obsługująca polecenia sterujące.
	 * 
	 * @param splittedLine tablica zwierająca podzieloną linię tekstu
	 */
	private void controlCommand(String[] splittedLine) {
		int i = 0;
		String command = splittedLine[i];
		i++;

		try {
			switch (command) {
			case "%lang":
				checkArgumentsNumber(splittedLine, 2);
				langCommand(command, splittedLine[i]);
				break;

			case "%details":
				checkArgumentsNumber(splittedLine, 2);
				detailsCommand(command, splittedLine[i]);
				break;

			case "%limit":
				checkArgumentsNumber(splittedLine, 2);
				limitCommand(splittedLine[i]);
				break;

			case "%color":
				checkArgumentsNumber(splittedLine, 2);
				colorCommand(command, splittedLine[i]);
				break;

			case "%term":
				checkArgumentsNumber(splittedLine, 1);
				searchChange(true, false, false);
				break;

			case "%phrase":
				checkArgumentsNumber(splittedLine, 1);
				searchChange(false, true, false);
				break;

			case "%fuzzy":
				checkArgumentsNumber(splittedLine, 1);
				searchChange(false, false, true);
				break;

			default:
				throw new WrongCommand(command);
			}
		} catch (WrongNumberOfArguments e) {
			System.err.println("Wrong number of arguments for command " + e.getMessage());
		} catch (WrongArgument e) {
			System.err.println("Wrong argument in command " + e.getMessage());
		} catch (NumberFormatException e) {
			System.err.println("Wrong number in command %limit");
		} catch (WrongCommand e) {
			System.err.println("Wrong command " + e.getMessage());
		}
	}

	/**
	 * Metoda odpowiedzialna za drukowanie wyników wyszukiwania.
	 * 
	 * @param reader   obiekt klasy IndexReader
	 * @param searcher obiekt klasy IndexSearcher
	 * @param query    obiekt klasy Query
	 * @param analyzer obiekt klasy Analyzer
	 * @throws IOException
	 * @throws InvalidTokenOffsetsException
	 */
	private void printResults(IndexReader reader, IndexSearcher searcher, Query query,
			Analyzer analyzer) throws IOException, InvalidTokenOffsetsException {
		TopDocs results;
		if (limit == 0) {
			results = searcher.search(query, Integer.MAX_VALUE);
		} else {
			results = searcher.search(query, limit);
		}
		ScoreDoc[] hits = results.scoreDocs;
		int numberOfHits = Math.toIntExact(results.totalHits.value);

		System.out.println("Files count: " + numberOfHits);

		Formatter formatter = new MyFormatter(color);
		QueryScorer scorer = new QueryScorer(query);
		Highlighter highlighter = new Highlighter(formatter, scorer);
		Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 50);
		highlighter.setTextFragmenter(fragmenter);

		for (int i = 0; i < hits.length; i++) {
			int docId = hits[i].doc;
			Document document = searcher.doc(hits[i].doc);
			String path = document.get("path");
			System.out.println(path);
			if (details) {
				@SuppressWarnings("deprecation")
				TokenStream stream = TokenSources.getAnyTokenStream(reader, docId, "contents",
						analyzer);
				String[] fragments = highlighter.getBestFragments(stream, document.get("contents"),
						10);
				for (String f : fragments) {
					System.out.println("... " + f + " ...");
				}
			}
		}
	}

	/**
	 * Metoda odpowiedzialna za interakcję z użytkownikiem.
	 * 
	 * @throws IOException
	 */
	public void searchAndPrintResults() throws IOException {
		IndexSearcher searcherPL = new IndexSearcher(readerPL);
		IndexSearcher searcherEN = new IndexSearcher(readerEN);
		Analyzer analyzer = new StandardAnalyzer();
		String line;
		String[] splittedLine;
		Query query;

		IndexReader mainReader = readerEN;
		IndexSearcher mainSearcher = searcherEN;

		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {
				try {
					System.out.print("> ");
					line = scanner.nextLine();

					if (line.length() > 0) {
						splittedLine = line.split(" ");
						if (line.charAt(0) == '%') {
							controlCommand(splittedLine);
							if (language.equals("pl")) {
								mainSearcher = searcherPL;
								mainReader = readerPL;
							} else {
								mainSearcher = searcherEN;
								mainReader = readerEN;
							}
						} else {
							if (termSearch) {
								if (splittedLine.length > 1) {
									throw new IncorrectSearchingArgument();
								} else {
									query = searchTerm("contents", line);
									printResults(mainReader, mainSearcher, query, analyzer);
								}
							} else if (phraseSearch) {
								query = searchPhrase("contents", splittedLine);
								printResults(mainReader, mainSearcher, query, analyzer);
							} else if (fuzzySearch) {
								if ((line.length() < 4) && details) {
									System.err.println(
											"Could not write context for phrase shorter than 4 characters");
									details = false;
									query = searchFuzzy("contents", line);
									printResults(mainReader, mainSearcher, query, analyzer);
									details = true;
								} else {
									query = searchFuzzy("contents", line);
									printResults(mainReader, mainSearcher, query, analyzer);
								}
							}
						}
					}
				} catch (IncorrectSearchingArgument e) {
					System.err.println("Incorrect searching argument");
				}
			}
		} catch (InvalidTokenOffsetsException e) {
			System.err.println("Invalid token off sets");
		}
	}

}
