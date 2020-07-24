import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 * @author Jackson Raffety
 */
public class Driver {

	/**
	 * The driver for indexing and searching a set of files.
	 * @param args The arguments from which to build the Inverted Index.
	 */
	public static void main(String[] args) {
		
		//The optional arguments we wish to check for and their defaults if absent.
		String ctsFlag        = "-counts";
		String indFlag        = "-index";
		String thrdFlag       = "-threads";
		String limitFlag      = "-limit";
		String urlFlag        = "-url";
		String defaultCounts  = "counts.json";
		String defaultIndex   = "index.json";
		String defaultThreads = "5";
		String defaultURL     = "default";
		String defaultLimit   = "50";
		int defaultRedirect   = 3;
		int limit;
		int threads;
		
		// Store initial start time.
		Instant start = Instant.now();
		
		/**
		 * Declare the InvertedIndex data structure and create the ArgumentParser.
		 * Declare the SearchBuilder as well as the InvertedIndexBuilder.
		 */
		ArgumentParser parser = new ArgumentParser();
		InvertedIndex index;
		InvertedIndexBuilder indexBuilder;
		SearchBuilderInterface searchBuilder;
		
		/** Parse Args. **/
		parser.parse(args);
		
		/** Preprocess Multithreading/URL Parsing Behavior **/
		String threadNo = parser.hasFlag(urlFlag) ? "5" : parser.getString(thrdFlag, defaultThreads);
		String limitNo  = parser.getString(limitFlag, defaultLimit);
		
		/** Validate threadNo **/
		try {
			threads = Integer.parseInt(threadNo);
			if (threads < 1) {
				threads = 5;
			}
		} catch (NumberFormatException e) {
			threads = 5;
		}
		
		/** Validate limitNo **/
		try {
			limit = Integer.parseInt(limitNo);
			if (limit < 1) {
				limit = 50;
			}
		} catch (NumberFormatException e) {
			limit = 50;
		}
		
		/** Determine Multithreaded Behavior **/
		if (parser.hasFlag(thrdFlag) || parser.hasFlag(urlFlag)) {
			index         = new ThreadSafeInvertedIndex();
			indexBuilder  = new ConcurrentInvertedIndexBuilder((ThreadSafeInvertedIndex) index, threads);
			searchBuilder = new ConcurrentSearchBuilder((ThreadSafeInvertedIndex) index, threads);
		}
		
		/** Determine Single Threaded Behavior **/
		else {
			index         = new InvertedIndex();
			indexBuilder  = new InvertedIndexBuilder(index);
			searchBuilder = new SearchBuilder(index);
		}

		/** Build the list of paths to construct the inverted index. **/
		try {
			if (parser.hasFlag(urlFlag)) {
				Crawler crawler = new Crawler(index, limit, threads);
				crawler.initCrawl(parser.getString(urlFlag, defaultURL), defaultRedirect);
			}
			else {
				indexBuilder.buildInvertedIndex(parser.getPath("-path"));
			}
		} catch (IOException e) {
			System.err.println("Given path from which to build the index is invalid.");
		} catch (NullPointerException np) {
			System.err.println("Given path from which to build the index is absent.");
		} catch (InterruptedException e) {
			System.err.println("Thread interrupted while building the index.");
		}

		/** Check for optional -index flag. **/
		try {
			if (parser.hasFlag(indFlag)) {
				Path printpath = parser.getPath(indFlag, defaultIndex);
				index.writeIndex(printpath);
			}
		} catch (IOException e) {
			System.err.println("Given path at which to write -index is invalid.");
		}
		
		/** Check for optional -counts flag. **/
		try {
			if (parser.hasFlag(ctsFlag)) {
				Path printpath = parser.getPath(ctsFlag, defaultCounts);
				SimpleJsonWriter.asObject(index.getCount(), printpath);
			}
		} catch (IOException e) {
			System.err.println("Given path at which to write -counts is invalid.");
		}
		
		/** Check for optional -query flag. **/
		try {
			if (parser.hasFlag("-query")) {
				searchBuilder.parseQuery(parser.getPath("-query"), parser.hasFlag("-exact"));
			}
		} catch (IOException e) {
			System.err.println("Given path at which to write -query is invalid.");
		} catch (NullPointerException np) {
			System.err.println("Given path at which to write -query is absent.");
		} catch (InterruptedException e) {
			System.err.println("Thread interrupted while building the search.");
		}
		
		/** Construct the JSON output. **/
		if (parser.hasFlag("-results")) {
			try {
				searchBuilder.printResults(parser.getPath("-results", "results.json"));
			} catch (IOException e) {
				System.err.println("Given path at which to write -results is invalid.");
			}
		}
		
		/** Calculate time elapsed and output. **/
		Duration elapsed = Duration.between(start, Instant.now());
		double seconds   = (double) elapsed.toMillis() / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}
}
