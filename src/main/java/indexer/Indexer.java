package indexer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

/**
 * Klasa odpowiadająca za działanie indeksera.
 * 
 * @author Karol Soczewica
 * @version 2019.06.21
 */
public class Indexer {
	/**
	 * Ścieżka do katalogu zawierającego indeks plików w języku polskim.
	 */
	private static final String polishIndexPath = "index/indexPL";

	/**
	 * Ścieżka do katalogu zawierającego indeks plików w języku angielskim.
	 */
	private static final String englishIndexPath = "index/indexEN";

	/**
	 * Obiekt klasy IndexWriter dla plików w języku polskim.
	 */
	private IndexWriter polishWriter;

	/**
	 * Obiekt klasy IndexWriter dla plików w języku angielskim.
	 */
	private IndexWriter englishWriter;

	/**
	 * Konstruktor klasy Indexer.
	 * 
	 * @throws IOException
	 */
	public Indexer() throws IOException {
		Directory directoryPL = FSDirectory.open(Paths.get(polishIndexPath));
		Directory directoryEN = FSDirectory.open(Paths.get(englishIndexPath));
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwcPL = new IndexWriterConfig(analyzer);
		IndexWriterConfig iwcEN = new IndexWriterConfig(analyzer);

		iwcPL.setOpenMode(OpenMode.CREATE_OR_APPEND);
		iwcEN.setOpenMode(OpenMode.CREATE_OR_APPEND);

		polishWriter = new IndexWriter(directoryPL, iwcPL);
		englishWriter = new IndexWriter(directoryEN, iwcEN);
	}

	/**
	 * Metoda indeksująca dokumenty z katalogu o podanej jako parametr ścieżce.
	 * 
	 * @param file               ścieżka do katalogu
	 * @param availableLanguages obiekt klasy Languages
	 * @throws IOException
	 */
	public void indexDocuments(Path file, Languages availableLanguages) throws IOException {
		if (Files.isDirectory(file)) {
			Files.walkFileTree(file, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
						throws IOException {
					if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
						indexDocument(file, availableLanguages);
					} else {
						System.err.println("Not regular file " + file.getFileName().toString());
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc)
						throws IOException {
					if (exc instanceof AccessDeniedException) {
						return FileVisitResult.SKIP_SUBTREE;
					}
					return super.visitFileFailed(file, exc);
				}
			});
		} else {
			indexDocument(file, availableLanguages);
		}
	}

	/**
	 * Metoda indeksująca pojedynczy dokument.
	 * 
	 * @param file               ścieżka do dokumentu
	 * @param availableLanguages obiekt klasy Languages
	 */
	private void indexDocument(Path file, Languages availableLanguages) {
		try (InputStream stream = Files.newInputStream(file)) {
			Document document = new Document();
			Extractor extractor = new Extractor(stream, availableLanguages);

			Field pathField = new StringField("path", file.toString(), Field.Store.YES);
			Field textField = new TextField("contents", extractor.text(), Field.Store.YES);

			document.add(pathField);
			document.add(textField);

			if (extractor.language().equals("pl")) {
				polishWriter.addDocument(document);
			} else {
				englishWriter.addDocument(document);
			}
		} catch (IOException | SAXException | TikaException e) {
			System.err.println("Parsing file problem in file " + file.getFileName().toString());
		}
	}

	/**
	 * Metoda usuwająca dokumenty z podanego jako parametr katalogu.
	 * 
	 * @param path napis reprezentujący nazwę katalogu
	 * @throws IOException
	 */
	public void removeDocuments(String path) throws IOException {
		Term term = new Term("path", path + "*");
		Query wildcard = new WildcardQuery(term);
		polishWriter.deleteDocuments(wildcard);
		englishWriter.deleteDocuments(wildcard);
	}

	/**
	 * Metoda zamykająca obiekty klasy IndexWriter.
	 * 
	 * @throws IOException
	 */
	public void closeIndexWriters() throws IOException {
		polishWriter.close();
		englishWriter.close();
	}

	public void deleteAllIndexedFiles() throws IOException {
		polishWriter.deleteAll();
		englishWriter.deleteAll();
	}
}
