package capitalism.utils;

import java.net.URL;
import java.net.URLClassLoader;

public class StringStuff {

	/**
	 * Database stores in UTF8 but Java uses UTF16.
	 * Occasionally (eg currency symbols) this causes problems.
	 * Hence this procedure, from https://fabioangelini.wordpress.com/2011/08/04/converting-java-string-fromto-utf-8/
	 * 
	 * @param inString
	 *            the UtF8 string to be converted
	 * @return the UTF16 string results from converting the input
	 */
	public static String convertFromUTF8(String inString) {
		String out = null;
		try {
			out = new String(inString.getBytes("ISO-8859-1"), "UTF-8");
		} catch (java.io.UnsupportedEncodingException e) {
			return null;
		}
		return out;
	}
	/**
	 * debug method, sometimes used to check what's going on with the runnable jar
	 */
	public void printClassPath() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		URL[] urls = ((URLClassLoader) cl).getURLs();
		for (URL url : urls) {
			System.out.println(url.getFile());
		}
	}

}
