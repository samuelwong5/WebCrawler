package webcrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AmazonWebCrawler {
	public void start() {
		WebCrawler wc = new WebCrawler(
				"http://www.amazon.com/LEGO-Classic-Green-Baseplate-Supplement/dp/B00NHQF65S/",  // Initial URL
				"http://www.amazon.com/",                                                        // Base URL
				"http://www.amazon.com/.*dp/.*");	                                             // Parse URL
		Pattern prices = Pattern.compile("(\\$|£)([1-9]+(,\\d{3})*)(.\\d{2})?[^(0-9|.)]");
	    Pattern images = Pattern.compile("img src=\"[^\\s]*jpg");
	    List<String> pricesList = new ArrayList<String>();
	    List<String> imagesList = new ArrayList<String>();
	    wc.addPattern(prices, pricesList);
	    wc.addPattern(images, imagesList);
	    while (wc.parseOne()) {
	    	// Ex: Use pricesList and imagesList to store into database
	    }
	}

}
