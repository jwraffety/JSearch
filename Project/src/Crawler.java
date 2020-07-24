import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * A web crawler which searches a seed URL for connecting URLs.
 * These URLs are parsed for their HTML data and an inverted index may
 * be built from this HTML data.
 * @author Jackson Raffety
 */
public class Crawler {
	/**
	 * The InvertedIndex to build.
	 */
	private final ThreadSafeInvertedIndex index;
	
	/**
	 * The total number of links to crawl.
	 */
	private volatile int limit;
	
	/**
	 * The amount of threads to run for the WorkQueue.
	 */
	private final int threads;
	
	/**
	 * The set of unique, crawled URLs saved as Strings for easy comparison.
	 */
	private HashSet<String> unique;
	
	/**
	 * The WorkQueue to execute Tasks.
	 */
	private WorkQueue queue;
	
	/**
	 * A class-level lock to control access to limit.
	 */
	private static Object lock;
	
	/**
	 * Constructs the Crawler class.
	 * @param index   The inverted index to build.
	 * @param limit   The number of unique URLs to crawl.
	 * @param threads The number of threads for the WorkQueue. 
	 */
	public Crawler(InvertedIndex index, int limit, int threads) {
		this.index   = (ThreadSafeInvertedIndex) index;
		this.limit   = limit;
		this.threads = threads;
		this.unique = new HashSet<>();
		lock = new Object();
	}
	
	/**
	 * Crawls from a seed URL and finds other URLs within that seed URL before parsing
	 * the HTML content of the seed URL.
	 * @param seed      The seed URL.
	 * @param redirects How many redirects to take, if necessary.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void initCrawl(String seed, int redirects) throws IOException, InterruptedException {
		URL seedUrl        = LinkParser.clean(new URL(seed));
		String dirtyHtml   = HtmlFetcher.fetch(seed, redirects);
		String cleanedHtml = HtmlCleaner.stripBlockElements(dirtyHtml);
		ArrayList<String> discovered = new ArrayList<>();
		
		/** Get an in sequence, unique list of URLs **/
		discovered.add(seedUrl.toString());
		discovered.addAll(LinkParser.listLinks(seedUrl, cleanedHtml));
		discovered = (ArrayList<String>) discovered.stream().distinct().collect(Collectors.toList());
		this.unique.addAll(discovered);
		
		/** Start the queue and iterate over unique**/
		this.queue = new WorkQueue(this.threads);
		Iterator<String> iter = discovered.iterator();
		
		while (iter.hasNext() && limit > 0) {
			limit--;
			String found = iter.next();
			this.queue.execute(new Task(found, redirects));
		}
		this.queue.finish();
		this.queue.shutdown();
		cleanedHtml = HtmlCleaner.stripTags(cleanedHtml);
		cleanedHtml = HtmlCleaner.stripEntities(cleanedHtml);
		parseLine(seedUrl.toString(), cleanedHtml, this.index);
	}
	
	/**
	 * Crawls from a seed URL and finds other URLs within that seed URL before parsing
	 * the HTML content of the seed URL.
	 * @param seed      The seed URL.
	 * @param redirects How many redirects to take, if necessary.
	 * @return The cleaned html.
	 * @throws IOException
	 */
	public String crawl(String seed, int redirects) throws IOException {
		URL url            = LinkParser.clean(new URL(seed));
		String dirtyHtml   = HtmlFetcher.fetch(url, redirects);
		String cleanedHtml = HtmlCleaner.stripBlockElements(dirtyHtml);
		ArrayList<String> discovered = LinkParser.listLinks(url, cleanedHtml);
		discovered = (ArrayList<String>) discovered.stream().distinct().collect(Collectors.toList());//
		HashSet<String> local = new HashSet<>(discovered);
		if (limit > 0) {
			for (String foundUrl : local) {
				synchronized(unique) {
					if (unique.contains(foundUrl)) {
						local.remove(foundUrl);
						continue;
					}
				}
			}
			Iterator<String> iter = local.iterator();
			synchronized(lock) {
				while (iter.hasNext() && limit > 0) {
					queue.execute(new Task(iter.next(), redirects));
					limit--;
				}
			}
		}
		cleanedHtml = HtmlCleaner.stripTags(cleanedHtml);
		cleanedHtml = HtmlCleaner.stripEntities(cleanedHtml);
		return cleanedHtml;
	}
	
	/**
	 * A static helper method to parse a line dervied from a path and add its contents
	 * to index.
	 * @param line  The line to parse.
	 * @param url   The path from which the line was derived.
	 * @param index The InvertedIndex to build.
	 * @throws IOException
	 */
	public static void parseLine(String url, String line, InvertedIndex index) throws IOException {
		int counter = 1;
		SnowballStemmer stemmer = new SnowballStemmer(TextFileStemmer.DEFAULT);
		for (String word : TextParser.parse(line)) {
			index.add(TextFileStemmer.stemWord(word, stemmer), url, counter++);
		}
	}
	
	/**
	 * The task class to parse HTML from a single URL.
	 * @author Jackson Raffety
	 *
	 */
	private class Task implements Runnable {
		
		/**
		 * The URL from which to build.
		 */
		private String url;

		/**
		 * The acceptable number of redirects to be followed.
		 */
		private int redirects;
		
		/**
		 * The task subclass which assigns a job to a thread.
		 * @param url       The url at which to begin building the InvertedIndex.
		 * @param redirects The number of acceptable redirects to follow.
		 * @throws MalformedURLException 
		 */
		public Task(String url, int redirects) throws MalformedURLException {
			this.url = url;
			this.redirects = redirects;
		}

		@Override
		public void run() {
			try {
				InvertedIndex local = new InvertedIndex();
				String html = crawl(url, redirects);
				if (html != null) {
					parseLine(url, html, local);
				}
				index.addAll(local);
			}
			catch (IOException e) {
				System.err.println("Thread failure: ConcurrentInvertedIndexBuilder");
			}
		}
	}
}
