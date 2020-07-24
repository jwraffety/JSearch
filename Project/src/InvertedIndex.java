import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class for our custom data type Inverted Index. Constructs a nested
 * TreeMap<String, TreeMap<String TreeSet<Integer>>> object which can be read as
 * TreeMap<String A stemmed word, TreeMap<String Paths to the stemmed word
 * in file, TreeSet<Integer> the count of how many times that stemmed
 * word appeared in the Path>>.
 * Also contains search functionality supported by the SearchResult and
 * SearchBuilder classes.
 * @author Jackson Raffety
 */
public class InvertedIndex {
	
	/**
	 * Stores the word stems and the paths/times found in file as described in
	 * the class description.
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;
	
	/**
	 * String path and how many words are at that path, stored
	 * in a TreeSet<String,Integer> to avoid duplicates.
	 */
	private final TreeMap<String, Integer> count;
	
	/**
	 * Default constructor to initialize our two maps.
	 */
	public InvertedIndex() {
		this.index = new TreeMap<>();
		this.count = new TreeMap<>();
	}
	
	/**
	 * Checks the Index for a particular word.
	 * @param word     The word to look for.
	 * @return boolean Whether the index contains the word.
	 */
	public boolean contains(String word) {
		return this.index.get(word) != null ? true : false;
	}
	
	/**
	 * Checks the Index for a word at a location.
	 * @param word     The word to look for.
	 * @param location The location to look for word.
	 * @return boolean Whether the index contains word at location.
	 */
	public boolean contains(String word, String location) {
		if (this.index.get(word) == null) {
			return false;
		}
		return this.index.get(word).get(location) != null ? true : false;
	}
	
	/**
	 * Checks the Index for a word at location with position.
	 * @param word     The word to look for.
	 * @param location The location to look for word.
	 * @param position THe position in location to look for word.
	 * @return boolean Whether the index contains the word at location with position.
	 */
	public boolean contains(String word, String location, int position) {
		if (this.index.get(word) == null || this.index.get(word).get(location) == null) {
			return false;
		}
		return this.index.get(word).get(location).contains(position);
	}
	
	/**
	 * Adds a <K,V> pair to the index as a <word, path> pair with an associated
	 * count.
	 * @param word  The word to use as a key to add.
	 * @param path  The path to associate with the key.
	 * @param count The count of how many times the word appeared
	 *     in the path, saved to the TreeSet<Integer> structure.
	 */
	public void add(String word, String path, int count) {
		this.index.putIfAbsent(word, new TreeMap<String, TreeSet<Integer>>());
		this.index.get(word).putIfAbsent(path, new TreeSet<Integer>());
		int current = this.count.getOrDefault(path, 0);
		if (current < count) {
			this.index.get(word).get(path).add(count);
			this.count.put(path, count);
		}
	}
	
	/**
	 * Takes an InvertedIndex and adds all of its elements to this.index.
	 * @param ind The data structure over which to iterate and add its elements to this.index.
	 */
	public void addAll(InvertedIndex ind) {
		for (String key1 : ind.getStemSet()) {
			this.index.putIfAbsent(key1, new TreeMap<String, TreeSet<Integer>>());
			for (String key2 : ind.getPathSet(key1)) {
				this.index.get(key1).putIfAbsent(key2, new TreeSet<Integer>(ind.getPathCountSet(key1, key2)));
			}
		}
		for (String path : ind.getCount().keySet()) {
			this.count.put(path, ind.getCount().get(path));
		}
	}
	
	/**
	 * A safe view of the keyset representing index.get(word).get(path).keySet().
	 * @param word        The word from which to find our path.
	 * @param path        The path from which to find the TreeSet of word counts.
	 * @return Collection The unmodifiable TreeSet.
	 */
	public Collection<Integer> getPathCountSet(String word, String path) {
		if (this.contains(word, path)) {
			return Collections.unmodifiableCollection(this.index.get(word).get(path));
		}
		return Collections.emptySet();
	}
	
	/**
	 * A safe view of the keyset representing index.get(word).keySet().
	 * @param word        The word from which to get our keyset.
	 * @return Collection The unmodifable set of keys.
	 */
	public Collection<String> getPathSet(String word) {
		if (this.contains(word)) {
			return Collections.unmodifiableCollection(this.index.get(word).keySet());
		}
		return Collections.emptySet();
	}
	
	/**
	 * A safe view of the keyset representing index.keySet().
	 * @return Collection The unmodifiable set of words.
	 */
	public Collection<String> getStemSet() {
		return Collections.unmodifiableCollection(this.index.keySet());
	}
	
	/**
	 * A safe view of index.count.
	 * @return Map The unmodifiable map representing a count of words in file.
	 */
	public Map<String, Integer> getCount() {
		return Collections.unmodifiableMap(this.count);
	}
		
	/**
	 * Searches the index for a single query.
	 * @param query The set of queries to be made.
	 * @param exact Whether the type of search is Exact or Partial.
	 * @return      The list of search results.
	 */
	public ArrayList<SearchResult> search(Set<String> query, boolean exact) {
		return exact ? exactSearch(query) : partialSearch(query);
	}
	
	/**
	 * Performs a partial search.
	 * @param toSearchFor The query to make.
	 * @return            The list of SearchResults 
	 */
	public ArrayList<SearchResult> partialSearch(Set<String> toSearchFor) {
		ArrayList<SearchResult> searchResults      = new ArrayList<>();
		HashMap<String, SearchResult> fileTracker  = new HashMap<>();
		
		for (String stem : toSearchFor) {
			for (String indexStem : this.index.tailMap(stem).keySet()) {
				if (!indexStem.startsWith(stem)) {
					break;
				}
				else {
					this.indexSearchHelper(indexStem, fileTracker, searchResults);
				}
			}
		}
		Collections.sort(searchResults);
		return searchResults;
	}
	
	/**
	 * Performs an exact search.
	 * @param toSearchFor The query to make.
	 * @return            The list of SearchResults.
	 */
	public ArrayList<SearchResult> exactSearch(Set<String> toSearchFor) {
		ArrayList<SearchResult> searchResults     = new ArrayList<>();
		HashMap<String, SearchResult> fileTracker = new HashMap<>();
		for (String stem : toSearchFor) {
			if (this.contains(stem)) {
				this.indexSearchHelper(stem, fileTracker, searchResults);
			}
		}
		Collections.sort(searchResults);
		return searchResults;
	}
	
	/**
	 * Iterates over a set of paths and calls buildMaps on the below data structures.
	 * @param stem    The word stem.
	 * @param tracker A way to track what path files we've already encountered.
	 * @param searchResults 
	 */
	private void indexSearchHelper(String stem,
		HashMap<String, SearchResult> tracker,
		ArrayList<SearchResult> searchResults)
	{
		for (String sPath : this.getPathSet(stem)) {
			if (tracker.get(sPath) != null) {
				tracker.get(sPath).updateScore(stem);
			}
			else {
				SearchResult newResult = new SearchResult(sPath, stem);
				searchResults.add(newResult);
				tracker.put(sPath, newResult);
			}
		}
	}
	
	/**
	 * Prints the Inverted Index to a pretty JSON output.
	 * @param printPath The path art which to write the Inverted Index.
	 * @throws IOException 
	 */
	public void writeIndex(Path printPath) throws IOException {
		SimpleJsonWriter.asInvertedIndex(this.index, printPath);
	}
	
	@Override
	public String toString() {
		return this.getStemSet().toString();
	}
	
	/** The SearchResult inner class **/

	/**
	 * Stores a single search result and implements the comparable interface.
	 * @author Jackson Raffety
	 */
	public class SearchResult implements Comparable<SearchResult> {
		
		/**
		 * The location in file of the query.
		 */
		private final String location;

		/**
		 * The number of times a word was found at the location.
		 */
		private int timesAtLocation;
		
		/**
		 * The calculated score of a given search. Generated by the number of times
		 * a query was found at the file location divided by the word count of the
		 * file in which it was located.
		 */
		private double score;

		/**
		 * This constructs a single search result.
		 * @param location The location in file of the search.
		 * @param query    The query string.
		 */
		public SearchResult(String location, String query) {
			this.location = location;
			this.updateScore(query);
		}
		
		/**
		 * Returns the location of the search.
		 * @return The file location.
		 */
		public String getLocation() {
			return this.location;
		}
		
		/**
		 * Returns the number of matches of the search.
		 * @return The number of matches of the search.
		 */
		public int getTimesAtLocation() {
			return this.timesAtLocation;
		}
		
		/**
		 * Returns the calulated score of a search.
		 * @return The double value of a search's score.
		 */
		public double getScore() {
			return this.score;
		}
		
		/**
		 * Updates the score and amount of times a query is valid for some location.
		 * @param query The query whose score we need to update.
		 */
		private void updateScore(String query) {
			this.timesAtLocation += index.get(query).get(location).size();
			this.score = (double) this.timesAtLocation / count.get(location);
		}
		
		@Override
		public String toString() {
			StringBuilder temp = new StringBuilder();
			temp.append(this.location);
			temp.append(" ");
			temp.append(this.timesAtLocation);
			temp.append(" ");
			temp.append(this.score);
			return temp.toString();
		}
		
		/**
		 *  The overridden compareTo method in order to compare SearchResults.
		 *  @param other The other SearchResult with which to compare.
		 *  @return      The outcome of the comparison. Equals = 0,
		 *               greater = 1, lesser = -1.
		 */
		@Override
		public int compareTo(SearchResult other) {
			if (this.score == other.getScore()) {
				if (this.timesAtLocation == other.getTimesAtLocation()) {
					return this.location.compareToIgnoreCase(other.getLocation());
				}
				else if (this.timesAtLocation > other.getTimesAtLocation()) {
					return -1;
				}
				else {
					return 1;
				}
			}
			else if (this.score > other.getScore()) {
				return -1;
			}
			else {
				return 1;
			}
		}
	}

}
