import java.io.IOException;
import java.nio.file.Path;

/**
 * An interface defining what attributes a SearchBuilder for an InvertedIndex
 * should have, whether multi or single-threaded.
 * @author Jackson Raffety
 */
public interface SearchBuilderInterface {

	/**
	 * Will open a path and call parseQuery on each line of the file at the path.
	 * @param path  The path to open.
	 * @param exact Whether an exact or partial search.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void parseQuery(Path path, boolean exact) throws IOException, InterruptedException;
	
	/**
	 * Will parse a single line and execute either exact or partial serch on the
	 * queries within the line.
	 * @param line  The line to parse.
	 * @param exact Whether an exact or partial search.
	 */
	public void parseQuery(String line, boolean exact);
	
	/**
	 * Will print the search results in a pretty JSON format at printPath.
	 * @param printPath The path at which to print the search results.
	 * @throws IOException
	 */
	public void printResults(Path printPath) throws IOException;
	
}
