import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/** 
 * Builder class for our custom data type Inverted Index. Contains functions
 * to parse and add elements to index.
 * @author Jackson Raffety
 */
public class InvertedIndexBuilder {
	
	/**
	 * The InvertedIndex to build.
	 */
	private final InvertedIndex index;
	
	/**
	 * IS_TEXT is a lambda to aid our Files stream in parsing.
	 * Finds each regular file and determines their type by
	 * taking the file extension toLowerCase() and scanning for 'text' or 'txt'.
	 */
	public static final Predicate<Path> IS_TEXT = path ->
		(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
		&& (checkExtension(path, ".text", ".txt"));
	
	/**
	 * Constructs the InvertedIndexBuilder
	 * @param index The InvertedIndex to build.
	 */
	public InvertedIndexBuilder(InvertedIndex index) {
		this.index = index;
	}
	
	/**
	 * Builds and stems the given InvertedIndex of index.
	 * @param startPath The starting path of our pathwalk.
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void buildInvertedIndex(Path startPath)
		throws IOException, InterruptedException
	{
		for (Path path : getTextFiles(startPath)) {
			parseFile(path);
		}
	}
	
	/** 
	 * buildPathList takes startPath and walks it to extract all following files.
	 * These files are walked and identified with IS_TEXT, collected and
	 * saved to pathwalk as an ArrayList<Path> object and returned.
	 * @param startPath  	   The path at which to begin the path walk.
	 * @return ArrayList<Path> A list of walked paths.
	 * @throws IOException
	 */
	public static ArrayList<Path> getTextFiles(Path startPath) throws IOException {
	    ArrayList<Path> pathList = new ArrayList<>();
	    try (Stream<Path> pathwalk = Files.walk(startPath,
	    	FileVisitOption.FOLLOW_LINKS))
	    {
	    	pathList.addAll(pathwalk.filter(p -> IS_TEXT.test(p))
	    			.collect(Collectors.toList()));
	    }
		return pathList;
	}
	
	/**
	 * A helper method to parse an entire file line by line and add its contents
	 * to index.
	 * @param path The path to parse.
	 * @throws IOException
	 */
	public void parseFile(Path path) throws IOException {
		parseFile(path, this.index);
	}
	
	/**
	 * A static helper method to parse an entire file line by line and add its contents
	 * to index.
	 * @param path  The path to parse.
	 * @param index The InvertedIndex to build.
	 * @throws IOException
	 */
	public static void parseFile(Path path, InvertedIndex index) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path,
			StandardCharsets.UTF_8)) 
		{
			String line = reader.readLine();
			int counter = 1;
			SnowballStemmer stemmer = new SnowballStemmer(TextFileStemmer.DEFAULT);
			String pathStr = path.toString();
			while (line != null) {
				for (String word : TextParser.parse(line)) {
					index.add(TextFileStemmer.stemWord(word, stemmer),
						pathStr, counter++);
				}
				line = reader.readLine();
			}
		}
	}
	
	/**
	 * A helper method to verify a file's extension, useful for the instance predicate.
	 * @param path       The path to read.
	 * @param extension1 The first extension to check for.
	 * @param extension2 The second extension to check for.
	 * @return boolean   Whether the path ends with the given extensions.
	 */
	public static boolean checkExtension(Path path, String extension1,
		String extension2)
	{
		String pathStr = path.toString().toLowerCase();
		return pathStr.endsWith(extension1) || pathStr.endsWith(extension2);
	}
}
