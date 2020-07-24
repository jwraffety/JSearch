import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A concurrently-building version of the SearchBuilder class.
 * @author Jackson Raffety
 *
 */
public class ConcurrentSearchBuilder implements SearchBuilderInterface {
	
	/**
	 * The ThreadSafeInvertedIndex to search.
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * The completed search results.
	 */
	private final TreeMap<String, ArrayList<InvertedIndex.SearchResult>> results;
	
	/**
	 * The work queue.
	 */
	private int threads;
	
	/**
	 * Constructs the SearchBuilder.
	 * @param index   The InvertedIndex from which to build a search.
	 * @param threads Number of threads.
	 */
	public ConcurrentSearchBuilder(ThreadSafeInvertedIndex index, int threads) {
		this.index = index;
		this.results = new TreeMap<>();
		this.threads = threads;
	}
	
	/**
	 * Iterates over the file located at path and parses each line as a query.
	 * @param path  The path at which to search.
	 * @param exact Whether to perform partial or exact search.
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void parseQuery(Path path, boolean exact) throws IOException, InterruptedException {
		try (BufferedReader reader = Files.newBufferedReader(path,
			StandardCharsets.UTF_8))
		{
			WorkQueue queue = new WorkQueue(this.threads);
			String line;
			while ((line = reader.readLine()) != null) {
				queue.execute(new Task(line, exact));
			}
			queue.finish();
			queue.shutdown();
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
		synchronized(this.results) {
			if (results.containsKey(joined)) {
				return;
			}
		}
		ArrayList<InvertedIndex.SearchResult> found = this.index.search(stems, exact);
		synchronized(this.results) {
			results.put(joined, found);
		}
	}
	
	/**
	 * The prints a pretty JSON view of the completed query.
	 * @param printPath The path at which to print.
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
	
	/**
	 * The task class.
	 * @author Jackson Raffety
	 *
	 */
	private class Task implements Runnable {
		
		/**
		 * The line to query.
		 */
		private String line;
		
		/**
		 * Whether exact or partial search.
		 */
		private boolean exact;

		/**
		 * The task class which performs a single search.
		 * @param line  The line to search.
		 * @param exact Whether an exact or partial search.
		 */
		public Task(String line, boolean exact) {
			this.line  = line;
			this.exact = exact;
		}

		@Override
		public void run() {
			parseQuery(this.line, this.exact);
		}
	}
}
