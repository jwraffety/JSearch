import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Utility class for parsing and stemming text and text files into sets of
 * stemmed words.
 * @author University of San Francisco
 * @author Jackson Raffety
 * @see TextParser
 */
public class TextFileStemmer {

	/** The default stemmer algorithm used by this class. */
	public static final SnowballStemmer.ALGORITHM DEFAULT = SnowballStemmer
		.ALGORITHM.ENGLISH;

	/**
	 * Returns a set of unique (no duplicates) cleaned and stemmed words parsed
	 * from the provided line.
	 * @param line             The line of words to clean, split, and stem.
	 * @return TreeSet<String> A sorted set of unique cleaned and stemmed words.
	 */
	public static TreeSet<String> uniqueStems(String line) {
		return uniqueStems(line, new SnowballStemmer(DEFAULT));
	}
	
	/**
	 * Returns a set of unique (no duplicates) cleaned and stemmed words parsed
	 * from the provided line.
	 * @param line    The line of words to clean, split, and stem.
	 * @param toStem  The TreeSet<String> in which to insert stemmed words.
	 * @return a sorted set of unique cleaned and stemmed words.
	 */
	public static TreeSet<String> uniqueStems(String line, TreeSet<String> toStem) {
		return uniqueStems(line, toStem, new SnowballStemmer(DEFAULT));
	}
	
	/**
	 * Returns a set of unique (no duplicates) cleaned and stemmed words parsed
	 * from the provided line.
	 * @param line    The line of words to clean, split, and stem.
	 * @param toStem  The TreeSet<String> to add the stemmed line in which to insert
	 * 		stemmed words.
	 * @param stemmer The stemmer to use.
	 * @return TreeSet<String> A sorted set of unique cleaned and stemmed words.
	 */
	public static TreeSet<String> uniqueStems(String line, TreeSet<String> toStem,
		Stemmer stemmer)
	{
		for (String word : TextParser.parse(line)) {
			toStem.add(stemmer.stem(word).toString());
		}
		return toStem;
	}
	
	/**
	 * Returns a set of unique (no duplicates) cleaned and stemmed words parsed
	 * from the provided line.
	 * @param line             The line of words to clean, split, and stem.
	 * @param stemmer          The stemmer to use.
	 * @return TreeSet<String> A sorted set of unique cleaned and stemmed words.
	 */
	public static TreeSet<String> uniqueStems(String line, Stemmer stemmer) {
		TreeSet<String> stemmed_strings = new TreeSet<String>();
		for (String word : TextParser.parse(line)) {
			stemmed_strings.add(stemmer.stem(word).toString());
		}
		return stemmed_strings;
	}
	
	/**
	 * Stems an individual word.
	 * @param word    The word to stem.
	 * @return String The stemmed word.
	 */
	public static String stemWord(String word) {
		return stemWord(word, new SnowballStemmer(DEFAULT));
	}
	
	/**
	 * Stems an individual word.
	 * @param word the word to stem.
	 * @param stemmer the stemmer algorithm to use.
	 * @return the stemmed word.
	 */
	public static String stemWord(String word, Stemmer stemmer) {
		return stemmer.stem(word).toString(); 
	}

	/**
	 * Reads a file line by line, parses each line into cleaned and stemmed words,
	 * and then adds those words to a set.
	 * @param inputFile        The input file to parse.
	 * @return TreeSet<String> A sorted set of stems from file.
	 * @throws IOException if unable to read or parse file.
	 */
	public static TreeSet<String> uniqueStems(Path inputFile) throws IOException {
		TreeSet<String> stemmed = new TreeSet<String>();
		try (BufferedReader reader = Files.newBufferedReader
			(inputFile, StandardCharsets.UTF_8);) {
			String line = reader.readLine();
			SnowballStemmer stemmer = new SnowballStemmer(TextFileStemmer.DEFAULT);
			while (line != null) {
				uniqueStems(line, stemmed, stemmer);
				line = reader.readLine();
			}
		}
		return stemmed.isEmpty() ? null : stemmed;
	}
}