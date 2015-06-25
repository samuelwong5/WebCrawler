package webcrawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebCrawler {
	private final Pattern _URLPATTERN;   
	private final String _INITIAL;
	private final String _BASE;
	private final String _PARSE;
	private List<String> nextUrl;
	private List<String> prevUrl; 
    private Map<Pattern, List<String>> parseData;
	private String currentUrl;          
	
    public WebCrawler(String initial, String base, String parse) {
    	this._INITIAL = initial;
    	this._BASE = base;
    	this._PARSE = parse;
    	this._URLPATTERN = Pattern.compile("href=\"" + _BASE +
    			"[^\"]*\"", Pattern.CASE_INSENSITIVE);
    	this.parseData = new HashMap<Pattern, List<String>>();
    }
    
    
    /**
     * Add a pattern to be matched during 
     * parsing the HTML source of each webpage.
     * @param p - pattern to be matched
     * @param pList - list the matches should be added to
     */
    public void addPattern(Pattern p, List<String> pList) {
    	parseData.put(p, pList);
    }
    
    
    /**
     * Starts the webcrawler using the given
     * initial URL and follows all the URLs next.
     */
    public void parseAll() {
    	prevUrl = new ArrayList<String>();
    	nextUrl = new ArrayList<String>();
    	nextUrl.add(_INITIAL);
    	currentUrl = _INITIAL;
        while (nextUrl.size() > 0) {
        	currentUrl = nextUrl.get(0);
        	parseCurrentUrl();
        }
    }

    /**
     * Parses the first URL in the nextUrl list.
     * @return - true if the list is not empty.
     */
    public boolean parseOne() {
    	if (nextUrl.size() > 0) {
    		currentUrl = nextUrl.get(0);
    		parseCurrentUrl();
    		return true;
    	}
    	return false;
    }
    
    
    /**
     * Parses the current URL.
     */
    private void parseCurrentUrl() {
    	for (Pattern p : parseData.keySet()) {
    		List<String> pList = parseData.get(p);
    		pList.clear();
    	}
    	prevUrl.add(currentUrl);
    	URL u = null;
		try {
			u = new URL(currentUrl);
		} catch (MalformedURLException e1) {
			//System.out.println("Malformed URL detected: " + currentUrl);
			return;
		}           
    	Scanner sc;
		try {
		    sc = new Scanner(u.openStream());
	        String buffer;            
			while (sc.hasNext()) {
				buffer = sc.next();
				getUrl(buffer);
				if (currentUrl.matches(_PARSE)) {
					parseBuffer(buffer);
				}
			}
			sc.close();
		} catch (IOException e1) {
			return;
		}
    }
    
    
    /**
     * Gets absolute and relative URLs by using 
     * regex to search for <a href> tags.
     * @param buffer - the HTML source to parse
     */
    private void getUrl(String buffer) {
    	Matcher urlMatcher = _URLPATTERN.matcher(buffer); 
	    while (urlMatcher.find()) {  
	    	String newUrl = urlMatcher.group();
	    	newUrl = newUrl.substring(6, newUrl.length() - 1);
        	newUrl = formatRelativeUrl(currentUrl, newUrl);
	        if (!prevUrl.contains(newUrl) && !nextUrl.contains(newUrl)) {
	        	if (newUrl.matches(_PARSE)) {
	        		nextUrl.add(0, newUrl);
	        	} else {
		        	nextUrl.add(newUrl);			        		
	        	}
	        }
	    }
    }
    
    
    /**
     * Parses the current buffer of HTML source
     * using the matchers provided.
     * @param buffer - the HTML source to parse
     */
    private void parseBuffer(String buffer) {
        for (Pattern p : parseData.keySet()) {
            Matcher m = p.matcher(buffer);
            List<String> pList = parseData.get(p);
            while (m.find()) {
                pList.add(m.group());
            }
        }
    }
    
    
    /**
     * Formats relative url to absolute url for webcrawler
     * to parse in the future.
     * @param currUrl - current url
     * @param foundUrl - found relative or absolute url
     * @return an absolute url representing the parsed url
     */
    private String formatRelativeUrl(String currUrl, String foundUrl) {
    	if (foundUrl.startsWith("/")) {                // Found URL of type "/FX/faces/home.html"
    		int i = currUrl.indexOf("com/");
    		int j = 0;
    		while (j < 3 && i < currUrl.length()) {
    			if (currUrl.charAt(i) == '/') {
    				j++;
    			}
    			if (j == 3) {
    				break;
    			}
    		}
    		String prevUrl = currUrl.substring(0, i);
    		foundUrl = prevUrl + foundUrl;
    	} else if (!foundUrl.startsWith("http")) {     // Found URL of type "resources/star.jpg"
    		int i = currUrl.lastIndexOf("/");
    		String prevUrl = currUrl.substring(0, i);
    		foundUrl = prevUrl + "/" + foundUrl;      		
    	}                                              // Found URL already absolute path
    	return foundUrl;
    }
}
