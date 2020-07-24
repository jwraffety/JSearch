import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class provides search functionality to our InvertedIndex class.
 * Constructs a TreeMap<String, ArrayList<SearchResult>> data structure which
 * holds a query and the list of search results from querying that query.
 * @author Jackson Raffety
 */
public class SearchBuilder implements SearchBuilderInterface {
	
	/**
	 * The InvertedIndex to search.
	 */
	private final InvertedIndex index;

	/**
	 * The completed search results.
	 */
	private final TreeMap<String, ArrayList<InvertedIndex.SearchResult>> results;
	
	/**
	 * Constructs the SearchBuilder.
	 * @param index The InvertedIndex from which to build a search.
	 */
	public SearchBuilder(InvertedIndex index) {
		this.index = index;
		this.results = new TreeMap<>();
	}
	
	/**
	 * Iterates over the file located at path and parses each line as a query.
	 * @param path  The path at which to search.
	 * @param exact Whether to perform partial or exact search.
	 * @throws IOException
	 */
	public void parseQuery(Path path, boolean exact) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path,
			StandardCharsets.UTF_8))
		{
			String line;
			while ((line = reader.readLine()) != null) {
				parseQuery(line, exact);
			}
		}
	}
	
	/**
	 * Parses a single line and searches the index for the parsed line.
	 * @param line  The line to clean and search.
	 * @param exact Whether to perform partial or exact search.
	 */
	public void parseQuery(String line, boolean exact) {
		TreeSet<String> stems = TextFileStemmer.uniqueStems(line);
		if (stems.isEmpty()) {
			return;
		}
		String joined = String.join(" ", stems);
		if (results.containsKey(joined)) {
			return;
		}
		ArrayList<InvertedIndex.SearchResult> found = this.index.search(stems, exact);
		results.put(joined, found);
	}
	
	/**
	 * The prints a pretty JSON view of the completed query.
	 * @param printPath  The path at which to print.
	 * @throws IOException 
	 */
	public void printResults(Path printPath) throws IOException {
		SimpleJsonWriter.asCompletedQuery(this.results, printPath);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (String key : this.results.keySet()) {
			builder.append(key);
			for (InvertedIndex.SearchResult searchResult : this.results.get(key)) {
				builder.append(": ");
				builder.append(searchResult.toString());
				builder.append("\n");
			}
		}
		return builder.toString();
	}
}


