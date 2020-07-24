import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses URL links from the anchor tags within HTML text.
 * @author University of San Francisco
 * @author Jackson Raffety
 */
public class LinkParser {

	/**
	 * Removes the fragment component of a URL (if present), and properly encodes
	 * the query string (if necessary).
	 * @param url The url to clean.
	 * @return cleaned url (or original url if any issues occurred)
	 */
	public static URL clean(URL url) {
		try {
			return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
					url.getPort(), url.getPath(), url.getQuery(), null).toURL();
		}
		catch (MalformedURLException | URISyntaxException e) {
			return url;
		}
	}

	/**
	 * Returns a list of all the HTTP(S) links found in the href attribute of the
	 * anchor tags in the provided HTML. The links will be converted to absolute
	 * using the base URL and cleaned (removing fragments and encoding special
	 * characters as necessary).
	 * @param base The base url used to convert relative links to absolute.
	 * @param html The raw html associated with the base url.
	 * @return cleaned list of all http(s) links in the order they were found.
	 */
	public static ArrayList<URL> listLinksURL(URL base, String html) {
		ArrayList<URL> links  = new ArrayList<>();
		String hrefRegex      = "<a[^>]+href\\s*=\\s*\"(.+?)\"";
		Pattern anchorPattern = Pattern.compile(hrefRegex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher hrefMatcher   = anchorPattern.matcher(html);
		while (hrefMatcher.find()) {
			try {
				URL absolute = clean(new URL(base, hrefMatcher.group(1)));
				links.add(absolute);
			}
			catch (MalformedURLException e) {
				System.err.println("Malformed URL");
			}
		}
		return links;
	}
	
	/**
	 * Returns a list of all the HTTP(S) links found in the href attribute of the
	 * anchor tags in the provided HTML. The links will be converted to absolute
	 * using the base URL and cleaned (removing fragments and encoding special
	 * characters as necessary).
	 * @param base The base url used to convert relative links to absolute.
	 * @param html The raw html associated with the base url.
	 * @return cleaned list of all http(s) links in the order they were found.
	 */
	public static ArrayList<String> listLinks(URL base, String html) {
		ArrayList<String> links = new ArrayList<>();
		String hrefRegex        = "<a[^>]+href\\s*=\\s*\"(.+?)\"";
		Pattern anchorPattern   = Pattern.compile(hrefRegex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher hrefMatcher     = anchorPattern.matcher(html);
		while (hrefMatcher.find()) {
			try {
				URL absolute = clean(new URL(base, hrefMatcher.group(1)));
				links.add(absolute.toString());
			}
			catch (MalformedURLException e) {
				System.err.println("Malformed URL");
			}
		}
		return links;
	}
}
