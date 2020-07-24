# JSearch #

JSearch is a single and multi-threaded web searched. Multiple classes described briefly below are responsible for builing a custom inverted index data structure which may be accessed to determine search results from queried/crawled web sites. Search functionality is passed by text file. Brief outlines of each java class are below:

## ArgumentParser ##

Parses and stores command-line arguments into simple key = value pairs.

## ConcurrentInvertedIndexBuilder ##

A concurrent version of the InvertedIndexBuilder class

## ConcurrentSearchBuilder ##

A concurrent version of the SearchBuilder class.

## Crawler ##

A web crawler which searches a seed URL for connecting URLs. These URLs are parsed for their HTML data and an inverted index may be built from this HTML data.

## Driver ##

Class responsible for running this project based on the provided command-line arguments.

## HtmlCleaner ##

Cleans simple, validated HTML into plain text.

## HtmlFetcher ##

A specialized version of HttpsFetcher that follows redirects and returns HTML content when possible.

## HttpsFetcher ##

An alternative to using Socket connections instead of a URLConnection to fetch the headers and content from a URL on the web.

## InvertedIndex ##

Class for our custom data type Inverted Index. Constructs a nested TreeMap<String, TreeMap<String TreeSet<Integer>>> object which can be read as TreeMap<String A stemmed word, TreeMap<String Paths to the stemmed word in file, TreeSet<Integer> the count of how many times that stemmed word appeared in the Path>>. Also contains search functionality supported by the SearchResult and SearchBuilder classes.

## InvertedIndexBuilder ##

 Builder class for our custom data type Inverted Index. Contains functions to parse and add elements to index.

## LinkParser ##

Parses URL links from the anchor tags within HTML text.

## SearchBuilder ##

This class provides search functionality to our InvertedIndex class. Constructs a TreeMap<String, ArrayList<SearchResult>> data structure which holds a query and the list of search results from querying that query.
  
## SearchBuilderInterface ##

An interface defining what attributes a SearchBuilder for an InvertedIndex should have, whether multi or single-threaded.

## SimpleJsonWriter ##

Outputs several data structures in "pretty" JSON format where newlines are used to separate elements and nested elements are indented. Warning: This class is not thread-safe. If multiple threads access this class concurrently, access must be synchronized externally.

## SimpleLock ##

A simple lock used for conditional synchronization as an alternative to using a synchronized block.

## SimpleReadWriteLock ##

Maintains a pair of associated locks, one for read-only operations and one for writing. The read lock may be held simultaneously by multiple reader threads, so long as there are no writers. The write lock is exclusive, but also tracks which thread holds the lock. If unlock is called by any other thread, a ConcurrentModificationException is thrown.

## TextFileStemmer ##

Utility class for parsing and stemming text and text files into sets of stemmed words.

## TextParser ##

Utility class for parsing text in a consistent manner.

## ThreadSafeInvertedIndex ##

A thread-safe version of the InvertedIndex class.

## WorkQueue ##

A simple work queue implementation based on the IBM Developer article by Brian Goetz. It is up to the user of this class to keep track of whether there is any pending work remaining.



