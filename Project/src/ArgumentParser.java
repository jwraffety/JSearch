import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses and stores command-line arguments into simple key = value pairs.
 * @author University of San Francisco
 * @author Jackson Raffety
 */
public class ArgumentParser {

	/**
	 * Stores command-line arguments in <k,v> pairs.
	 */
	private final Map<String, String> map;

	/**
	 * Initializes this argument map.
	 */
	public ArgumentParser() {
		this.map = new HashMap<>();
	}

	/**
	 * Parses the arguments into flag/value pairs where possible. Some flags may not
	 * have associated values. If a flag is repeated, its value is overwritten.
	 * @param args The command line arguments to parse.
	 */
	public void parse(String[] args) {
		boolean flagFound = false;
		if (args.length > 0) {
			for (int i = 0; i < args.length; ++i) {
				if (args[i].length() > 0) {
					if (isFlag(args[i])) {
						map.put(args[i], null);
						flagFound = true;
					}
					else if (flagFound && isFlag(args[i-1]) && isValue(args[i])) {
						map.put(args[i-1], args[i]);
						flagFound = false;
					}
				}
			}
		}
	}

	/**
	 * Determines whether the argument is a flag. Flags start with a dash "-"
	 * character, followed by at least one other character.
	 * @param arg      The argument to test if its a flag.
	 * @return boolean True if the argument is a flag.
	 */
	private static boolean isFlag(String arg) {
		if (arg == null) {
			return false;
		}
		else if (arg.length() > 1 && arg.charAt(0) == '-') {
			return true;
		}
		return false;
	}
	
	/**
	 * Determines whether the argument is a value. Values do not start with a dash
	 * "-" character, and must consist of at least one character.
	 * @param arg      The argument to test if its a value.
	 * @return boolean True if the argument is a value.
	 */
	private static boolean isValue(String arg) {
		return arg != null && !isFlag(arg) && !(arg.length() < 1 
			|| arg.charAt(0) == '-');
	}

	/**
	 * Returns the number of unique flags.
	 * @return int The number of unique flags.
	 */
	public int numFlags() {
		return map.keySet().size();
	}

	/**
	 * Determines whether the specified flag exists.
	 * @param flag     The flag to search for.
	 * @return boolean True if the flag exists
	 */
	public boolean hasFlag(String flag) {
		return map.containsKey(flag);
	}

	/**
	 * Determines whether the specified flag is mapped to a non-null value.
	 * @param flag     The flag to search for.
	 * @return boolean True if the flag is mapped to a non-null value.
	 */
	public boolean hasValue(String flag) {
		return map.get(flag) != null;
	}

	/**
	 * Returns The value to which the specified flag is mapped as a String,
	 * or null if there is no mapping for the flag.
	 * @param flag    The flag whose associated value is to be returned.
	 * @return String The value to which the specified flag is mapped, or null if
	 *         there is no mapping for the flag.
	 */
	private String getString(String flag) {
		return map.getOrDefault(flag, null);
	}

	/**
	 * Returns the value to which the specified flag is mapped as a String,
	 * or the default value if there is no mapping for the flag.
	 * @param flag         The flag whose associated value is to be returned.
	 * @param defaultValue The default value to return if there is no mapping for
	 *                     the flag.
	 * @return String      The value to which the specified flag is mapped, or the 
	 *     default value if there is no mapping for the flag.
	 */
	public String getString(String flag, String defaultValue) {
		String value = getString(flag);
		return value == null ? defaultValue : value;
	}

	/**
	 * Returns the value to which the specified flag is mapped as a Path, or
	 * null if the flag does not exist or has a null value.
	 * @param flag  The flag whose associated value is to be returned.
	 * @return Path The Path to which the specified flag is mapped, or {@code null} if
	 *     the flag does not exist or has a null value.
	 * @throws IOException
	 */
	public Path getPath(String flag) throws IOException {
		if (map.get(flag) == null) { return null; }
		Path p = Path.of(map.get(flag));
		return p;
	}

	/**
	 * Returns the value the specified flag is mapped as a Path, or the
	 * default value if the flag does not exist or has a null value.
	 * @param flag         The flag whose associated value will be returned.
	 * @param defaultValue The default value to return if there is no valid mapping
	 *                     for the flag.
	 * @return Path        The Path the specified flag is mapped as a {@link Path},
	 *     or the default value if there is no valid mapping for the flag.
	 * @throws IOException
	 */
	public Path getPath(String flag, String defaultValue) throws IOException {
		Path value = getPath(flag);
		return value == null ? Path.of(defaultValue) : value;
	}

	@Override
	public String toString() {
		return this.map.toString();
	}
}
