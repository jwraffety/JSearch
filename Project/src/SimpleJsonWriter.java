import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Outputs several data structures in "pretty" JSON format where
 * newlines are used to separate elements and nested elements are indented.
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 * @author University of San Francisco
 * @author Jackson Raffety 
 */
public class SimpleJsonWriter {
	
	/** Object JSON methods **/
	
	/**
	 * Writes the elements as a pretty JSON array.
	 * @param elements The elements to write.
	 * @param writer   The writer to use.
	 * @param level    The initial indent level.
	 * @throws IOException
	 */
	public static void asArray(Collection<Integer> elements, Writer writer, int level)
		throws IOException
	{
		writer.write("[");
		Iterator<Integer> iter = elements.iterator();
		if (iter.hasNext()) {
			writer.write("\n");
			indent(iter.next(), writer, level + 1);
		}
		while (iter.hasNext()) {
			writer.write(",\n");
			indent(iter.next(), writer, level + 1);
		}
		writer.write("\n");
		indent(writer, level);
		writer.write("]");
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 * @param elements The elements to write.
	 * @param path     The file path to use.
	 * @throws IOException
	 */
	public static void asArray(Collection<Integer> elements, Path path)
		throws IOException
	{
		try (BufferedWriter writer = Files.newBufferedWriter(path,
			StandardCharsets.UTF_8))
		{
			asArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 * @param elements The elements to use.
	 * @return String  The String of written elements.
	 * @throws IOException
	 */
	public static String asArray(Collection<Integer> elements) throws IOException {
		StringWriter writer = new StringWriter();
		asArray(elements, writer, 0);
		return writer.toString();
	}
	
	/**
	 * Writes the elements as a pretty JSON object.
	 * @param elements The elements to write.
	 * @param writer   The Writer to use.
	 * @param level    The initial indent level.
	 * @throws IOException
	 */
	public static void asObject(Map<String, Integer> elements, Writer writer, int level)
		throws IOException
	{
		Iterator<String> iter = elements.keySet().iterator();
		writer.write("{");
		if (iter.hasNext()) {
			writeEntry(elements, iter.next(), writer, level + 1);
		}
		while (iter.hasNext()) {
			writer.write(",");
			writeEntry(elements, iter.next(), writer, level + 1);
		}
		writer.write("\n");
		indent("}", writer, level - 1);
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 * @param elements The elements to write.
	 * @param path     The file path to use.
	 * @throws IOException
	 */
	public static void asObject(Map<String, Integer> elements, Path path)
		throws IOException
	{
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 * @param elements The elements to use.
	 * @return a {@link String} containing the elements in pretty JSON format.
	 * @throws IOException 
	 * @see #asObject(Map, Writer, int)
	 */
	public static String asObject(Map<String, Integer> elements) throws IOException {
		StringWriter writer = new StringWriter();
		asObject(elements, writer, 0);
		return writer.toString();
	}

	/**
	 * Writes the elements as a nested pretty JSON object. The generic notation used
	 * allows this method to be used for any type of map with any type of nested
	 * collection of integer objects.
	 * @param elements The elements to write.
	 * @param writer   The writer to use.
	 * @param level    The initial indent level.
	 * @throws IOException
	 */
	public static void asNestedObject(Map<String, ? extends Collection<Integer>>
	    elements, Writer writer, int level) throws IOException
	{
		Iterator<String> iter = elements.keySet().iterator();
		writer.write("{");
		if (iter.hasNext()) {
			writeNestedEntry(elements, iter.next(), writer, level + 1);
		}
		while (iter.hasNext()) {
			writer.write(",");
			writeNestedEntry(elements, iter.next(), writer, level + 1);
		}
		writer.write("\n");
		indent("}", writer, level - 1);
	}

	/**
	 * Writes the elements as a nested pretty JSON object to file.
	 * @param elements The elements to write.
	 * @param path     The file path to use.
	 * @throws IOException
	 */
	public static void asNestedObject(Map<String, ? extends
		Collection<Integer>> elements, Path path) throws IOException
	{
		try (BufferedWriter writer = Files.newBufferedWriter(path,
			StandardCharsets.UTF_8))
		{
			asNestedObject(elements, writer, 0);
		}
	}

	/**
	 * Returns elements as a nested pretty JSON object.
	 * @param elements The elements to use.
	 * @return String  A String containing the elements in pretty JSON format.
	 * @throws IOException 
	 */
	public static String asNestedObject(Map<String, ? extends
		Collection<Integer>> elements) throws IOException
	{
		StringWriter writer = new StringWriter();
		asNestedObject(elements, writer, 0);
		return writer.toString();
	}
	
	/**
	 * Writes an entry for some nested object.
	 * @param elements The map from which to write.
	 * @param item     The item to write.
	 * @param writer   The Writer to write.
	 * @param level    The level at which to write.
	 * @throws IOException
	 */
	private static void writeNestedEntry(Map<String, ? extends
		Collection<Integer>> elements, String item, Writer writer, int level)
		throws IOException
	{
		writer.write("\n");
		quote(item, writer, level + 1);
		writer.write(": ");
		asArray(elements.get(item), writer, level + 1);
	}

	/**
	 * Writes an entry for some object.
	 * @param elements The map from which to write. 
	 * @param item     The item to write
	 * @param writer   The Writer to write.
	 * @param level    The level at which to write.
	 * @throws IOException
	 */
	private static void writeEntry(Map<String, Integer> elements, String item,
		Writer writer, int level) throws IOException
	{
		writer.write("\n");
		quote(item, writer, level + 1);
		writer.write(": ");
		writer.write(elements.get(item).toString());
	}
	
	/** Inverted Index JSON methods **/

	/**
	 * Writes the InvertedIndex as a pretty JSON object to file.
	 * @param index The InvertedIndex to write.
	 * @param path  The path to which to write the JSON output.
	 * @throws IOException
	 */
	public static void asInvertedIndex(TreeMap<String, TreeMap<String, TreeSet<Integer>>> index, Path path)
		throws IOException 
	{
		try (BufferedWriter writer = Files.newBufferedWriter(path,
			StandardCharsets.UTF_8))
		{
			asInvertedIndex(index, writer, 0);
		}
	}
	
	/**
	 * Default method to call asInvertedIndex(index).
	 * @param index   The index to write.
	 * @return String The String representing the InvertedIndex in JSON format.
	 * @throws IOException
	 */
	public static String asInvertedIndex(TreeMap<String, TreeMap<String, TreeSet<Integer>>> index) throws IOException {
		StringWriter writer = new StringWriter();
		asInvertedIndex(index, writer, 0);
		return writer.toString();
	}
	
	/**
	 * Writes index to JSON format.
	 * @param index  The InvertedIndex to write.
	 * @param writer The Writer to use.
	 * @param level  The initial indent level.
	 * @throws IOException
	 */	
	public static void asInvertedIndex(TreeMap<String, TreeMap<String, TreeSet<Integer>>> index, Writer writer, int level)
		throws IOException
	{
		Iterator<String> iter = index.keySet().iterator();
		writer.write("{");
		if (iter.hasNext()) {
			writeInvertedIndexEntry(index, iter.next(), writer, level + 1);
		}
		while (iter.hasNext()) {
			writer.write(",");
			writeInvertedIndexEntry(index, iter.next(), writer, level + 1);
		}
		writer.write("\n}");
	}
	
	/**
	 * Writes the protected views of the InvertedIndex index.
	 * @param index   The InvertedIndex from which to write.
	 * @param stem    The word stem.
	 * @param pathSet The protected view of a set of paths.
	 * @param writer  The Writer whith which to write.
	 * @param level   The level at which to indent.
	 * @throws IOException
	 */
	public static void asNestedIndex(TreeMap<String, TreeMap<String, TreeSet<Integer>>> index, String stem,
		Collection<String> pathSet, Writer writer, int level) throws IOException
	{
		Iterator<String> iter = pathSet.iterator();
		writer.write("{");
		if (iter.hasNext()) {
			String path = iter.next();
			writeNestedIndexEntry(index.get(stem).get(path), path, writer, level + 1);
		}
		while (iter.hasNext()) {
			writer.write(",");
			String path = iter.next();
			writeNestedIndexEntry(index.get(stem).get(path), path, writer, level + 1);
		}
		writer.write("\n");
		indent("}", writer, level - 1);
	}
	
	/**
	 * Writes the protected views of the InvertedIndex index as an array.
	 * @param countSet The protected view of the set of word counts.
	 * @param path     The path to quote.
	 * @param writer   The Writer with which to write.
	 * @param level	   The level at which to indent.
	 * @throws IOException
	 */
	private static void writeNestedIndexEntry(Collection<Integer> countSet,
		String path, Writer writer, int level) throws IOException
	{
		writer.write("\n");
		quote(path, writer, level + 1);
		writer.write(": ");
		asArray(countSet, writer, level + 1);
	}
	
	/**
	 * Writes an entry for an inverted index JSON output.
	 * @param index  The inverted index from which to print.
	 * @param stem   The element to be printed.
	 * @param writer The writer to use for writing.
	 * @param level  The level at which we are indenting.
	 * @throws IOException
	 */
	private static void writeInvertedIndexEntry(TreeMap<String, TreeMap<String, TreeSet<Integer>>> index, String stem,
		Writer writer, int level) throws IOException
	{
		writer.write("\n");
		quote(stem, writer, level + 1);
		writer.write(": ");
		Collection<String> pathSet = index.get(stem).keySet();
		asNestedIndex(index, stem, pathSet, writer, level + 1);
	}
	
	/** Query JSON methods **/
	
	/**
	 * Writes the InvertedIndex.SearchResult elements as a pretty JSON array.
	 * @param elements The elements to write
	 * @param writer   The writer to use
	 * @param level    The initial indent level
	 * @throws IOException
	 */
	public static void asQueryArray(ArrayList<InvertedIndex.SearchResult> elements, Writer writer, int level) throws IOException {
		writer.write("[");
		int count = 0;
		int size = elements.size();
		Iterator<InvertedIndex.SearchResult> iter = elements.iterator();
		while (iter.hasNext()){
			++count;
			InvertedIndex.SearchResult item = iter.next();
			writer.write("\n");
			indent(writer, level + 1);
			writer.write("{");
			asQueryObject(item, writer, level + 1);
			if (count < size) { writer.write(","); }
		}
		writer.write("\n");
		indent(writer, level);
		writer.write("]");
	}

	/**
	 * Writes the InvertedIndex.SearchResult elements as a pretty JSON array to file.
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException
	 *
	 * @see #asArray(Collection, Writer, int)
	 */
	public static void asQueryArray(ArrayList<InvertedIndex.SearchResult> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asQueryArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the InvertedIndex.SearchResult elements as a pretty JSON array.
	 * @param elements the elements to use.
	 * @return a {@link String} containing the query in pretty JSON format.
	 */
	public static String asQueryArray(ArrayList<InvertedIndex.SearchResult> elements) {
		try {
			StringWriter writer = new StringWriter();
			asQueryArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Default method to call asCompletedQuery(TreeMap<String, ArrayList<InvertedIndex.SearchResult>>).
	 * @param results The query to write
	 * @return a {@link String} representing the completed query
	 * @throws IOException
	 */
	public static String asCompletedQuery(TreeMap<String, ArrayList<InvertedIndex.SearchResult>> results) throws IOException {
		StringWriter writer = new StringWriter();
		asCompletedQuery(results, writer, 0);
		return writer.toString();
	}
	
	/**
	 * Writes the completed query as a pretty JSON object to file.
	 * @param results The TreeMap as a completed query to write.
	 * @param path    The path to which to write the JSON output.
	 * @throws IOException
	 */
	public static void asCompletedQuery(TreeMap<String, ArrayList<InvertedIndex.SearchResult>> results, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asCompletedQuery(results, writer, 0);
		}
	}
	
	/**
	 * Writes results as a pretty JSON array.
	 * @param results The completed query to write.
	 * @param writer  The writer to use.
	 * @param level   The initial indent level.
	 * @throws IOException
	 */
	public static void asCompletedQuery(TreeMap<String, ArrayList<InvertedIndex.SearchResult>> results, Writer writer, int level) throws IOException {
		Iterator<String> iter = results.keySet().iterator();
		writer.write("{");
		int size = results.keySet().size();
		int count = 0;
		while (iter.hasNext()) {
			++count;
			String item = iter.next();
			String formattedItem= item.replace(",", "").replace("[", "").replace("]", "").trim();
			writer.write("\n");
			quote(formattedItem, writer, level + 1);
			writer.write(": ");
			asQueryArray(results.get(item), writer, level + 1);
			if (count < size) { writer.write(","); }
		}
		writer.write("\n}");
		
	}
	
	/**
	 * Writes the InvertedIndex.SearchResult elements as a pretty JSON object.
	 * @param elements The elements to write.
	 * @param writer   The writer to use.
	 * @param level    The initial indent level.
	 * @throws IOException
	 */
	public static void asQueryObject(InvertedIndex.SearchResult elements, Writer writer, int level) throws IOException {
		writer.write("\n");
		quote("where", writer, level + 1);
		writer.write(": ");
		quote(elements.getLocation(), writer, 0);
		writer.write(",\n");
		quote("count", writer, level + 1);
		writer.write(": ");
		writer.write(Integer.toString(elements.getTimesAtLocation()));
		writer.write(",\n");
		quote("score", writer, level + 1);
		writer.write(": ");
		String formatted = String.format("%.8f", elements.getScore());
		writer.write(formatted);
		writer.write("\n");
		indent(writer, level);
		writer.write("}");
	}

	/**
	 * Writes the InvertedIndex.SearchResult as a pretty JSON object to file.
	 * @param element  The elements to write.
	 * @param path     The file path to use.
	 * @throws IOException
	 */
	public static void asQueryObject(InvertedIndex.SearchResult element, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			asQueryObject(element, writer, 0);
		}
	}

	/**
	 * Returns the SearchResults as a pretty JSON query object.
	 * @param element The element to write.
	 * @return a {@link String} containing the elements in pretty JSON format.
	 */
	public static String asQueryObject(InvertedIndex.SearchResult element) {
		try {
			StringWriter writer = new StringWriter();
			asQueryObject(element, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}
	
	/** General purpose formatting helper methods **/
	
	/**
	 * Writes the tab symbol by the number of times specified.
	 * @param writer The writer to use.
	 * @param times  The number of times to write a tab symbol.
	 * @throws IOException
	 */
	private static void indent(Writer writer, int times) throws IOException {
		for (int i = 0; i < times; i++) {
			writer.write('\t');
		}
	}

	/**
	 * Indents and then writes the element.
	 * @param element The element to write.
	 * @param writer  The writer to use.
	 * @param times   The number of times to indent.
	 * @throws IOException
	 */
	private static void indent(Integer element, Writer writer, int times)
		throws IOException
	{
		indent(element.toString(), writer, times);
	}

	/**
	 * Indents and then writes the element.
	 * @param element The element to write.
	 * @param writer  The writer to use.
	 * @param times   The number of times to indent.
	 * @throws IOException
	 */
	private static void indent(String element, Writer writer, int times)
		throws IOException
	{
		indent(writer, times);
		writer.write(element);
	}

	/**
	 * Writes the element surrounded by quotation marks.
	 * @param element The element to write.
	 * @param writer  The writer to use.
	 * @throws IOException
	 */
	private static void quote(String element, Writer writer) throws IOException {
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Indents and then writes the element surrounded by quotation marks.
	 * @param element The element to write.
	 * @param writer  The writer to use.
	 * @param times   The number of times to indent.
	 * @throws IOException
	 */
	private static void quote(String element, Writer writer, int times) 
		throws IOException
	{
		indent(writer, times);
		quote(element, writer);
	}
}
