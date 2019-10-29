package indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Klasa zawierająca metodę main.
 * 
 * @author Karol Soczewica
 * @version 2019.06.21
 */
public class Main {
	/**
	 * Ścieżka do pliku zawierającego nazwy dodanych katalogów.
	 */
	private static final String infoFile = "index/info.txt";

	/**
	 * Metoda uruchamia indekser z podanym argumentem add.
	 * 
	 * @param directory          napis zawierający nazwę dodawanego katalogu
	 * @param availableLanguages obiekt klasy Languages z dostępnymi językami
	 */
	private static void runIndexerWithCommandAdd(String directory, Languages availableLanguages) {
		Path directoryPath = Paths.get(directory);
		if (!Files.isReadable(directoryPath)) {
			System.err.println("Files are not readable.");
			System.exit(1);
		} else {
			try {
				Indexer indexer = new Indexer();
				indexer.indexDocuments(directoryPath, availableLanguages);
				indexer.closeIndexWriters();
			} catch (IOException e) {
				System.err.println("Indexer cannot add directory " + directory);
				System.exit(1);
			}

			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(infoFile, true));
				writer.append(directoryPath.toString() + "\n");
				writer.close();
			} catch (IOException e) {
				System.err.println("Could not write to " + infoFile);
				System.exit(1);
			}
		}
	}

	/**
	 * Uruchamia indekser z podanym argumentem purge.
	 */
	private static void runIndexerWithCommandPurge() {
		Indexer indexer;
		try {
			indexer = new Indexer();
			indexer.deleteAllIndexedFiles();
			indexer.closeIndexWriters();
		} catch (IOException e) {
			System.err.println("Indexer cannot remove files");
			System.exit(1);
		}
	}

	/**
	 * Uruchamia indekser z podanym argumentem rm.
	 * 
	 * @param directoryToRemove napis reprezentujący nazwę katalogu do usunięcia
	 */
	private static void runIndexerWithCommandRm(String directoryToRemove) {
		try {
			Indexer indexer = new Indexer();
			indexer.removeDocuments(directoryToRemove);
			indexer.closeIndexWriters();
		} catch (IOException e) {
			System.err.println("Indexer cannot remove " + directoryToRemove);
			System.exit(1);
		}
	}

	/**
	 * Uruchamia indekser z podanym argumentem list.
	 */
	private static void runIndexerWithCommandList() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(infoFile)));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(Paths.get(line).toFile().getCanonicalPath());
			}
			reader.close();
		} catch (IOException e) {
			System.err.println("Could not read the file " + infoFile);
			System.exit(1);
		}
	}

	/**
	 * Uruchamia indekser z podanym argumentem reindex.
	 * 
	 * @param availableLanguages obiekt klasy Languages z dostępnymi językami
	 */
	private static void runIndexerWithCommandReindex(Languages availableLanguages) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(infoFile)));
			String line;
			Indexer indexer = new Indexer();
			indexer.deleteAllIndexedFiles();

			while ((line = reader.readLine()) != null) {
				indexer.indexDocuments(Paths.get(line), availableLanguages);
			}

			indexer.closeIndexWriters();
			reader.close();
		} catch (IOException e) {

		}
	}

	/**
	 * Metoda main.
	 * 
	 * @param args tablica argumentów podanych przy uruchamianiu programu
	 */
	public static void main(String[] args) {
		Languages availableLanguages = new Languages();
		try {
			switch (args.length) {
			case 0:
				try {
					DirectoryWatcher directoryWatcher = new DirectoryWatcher(Paths.get(infoFile),
							true);
					directoryWatcher.processEvents(availableLanguages);
				} catch (IOException e) {
					System.err.println("Could not run DirectoryWatcher");
				}
				while (true) {
				}
			case 1:
				if (args[0].equals("--purge")) {
					runIndexerWithCommandPurge();
				} else if (args[0].equals("--list")) {
					runIndexerWithCommandList();
				} else if (args[0].equals("--reindex")) {
					runIndexerWithCommandReindex(availableLanguages);
				} else {
					throw new IncorrectIndexerArguments();
				}
				break;
			case 2:
				if (args[0].equals("--add")) {
					runIndexerWithCommandAdd(args[1], availableLanguages);
				} else if (args[0].equals("--rm")) {
					runIndexerWithCommandRm(args[1]);
				} else {
					throw new IncorrectIndexerArguments();
				}
				break;

			default:
				throw new IncorrectIndexerArguments();
			}
		} catch (IncorrectIndexerArguments e) {
			System.err.println("Incorrect indexer arguments.");
		}
	}
}
