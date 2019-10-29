package indexer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.tika.exception.TikaException;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

/**
 * Klasa odpowiadająca za działanie ekstraktora.
 * 
 * @author Karol Soczewica
 * @version 2019.06.21
 */
public class Extractor {
	/**
	 * Język wyekstraktowanego tekstu.
	 */
	private String language;

	/**
	 * Wyekstraktowany tekst.
	 */
	private String text;

	/**
	 * Konstruktor klasy Extractor.
	 * 
	 * @param stream             strumień, z którego ekstraktujemy tekst
	 * @param availableLanguages obiekt klasy Languages z dostępnymi językami
	 * @throws IOException
	 * @throws SAXException
	 * @throws TikaException
	 */
	public Extractor(InputStream stream, Languages availableLanguages)
			throws IOException, SAXException, TikaException {
		BodyContentHandler handler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		AutoDetectParser parser = new AutoDetectParser();
		LanguageDetector languageDetector = new OptimaizeLangDetector();

		parser.parse(stream, handler, metadata);
		String extraxtedText = handler.toString();

		languageDetector.loadModels(availableLanguages.languages());
		languageDetector.addText(extraxtedText);
		List<LanguageResult> languages = languageDetector.detectAll();

		language = languages.get(0).toString().substring(0, 2);
		text = extraxtedText;
	}

	/**
	 * Akcesor dający w wyniku wyekstraktowany tekst.
	 * 
	 * @return wyekstraktowany tekst
	 */
	public String text() {
		return text;
	}

	/**
	 * Akcesor dający w wyniku język wyekstraktowanego tekstu.
	 * 
	 * @return język wyekstraktowanego tekstu
	 */
	public String language() {
		return language;
	}
}