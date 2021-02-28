package com.testtask.web.crawler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author aberdnikov
 */
public class URLCrawler {
    String url;
    String page = "";
    List<String> links = new LinkedList<>();

    /**
     * Constructs URL crawler for loading web-page and parsing new links
     * @param url - URL of the page
     */
    public URLCrawler(String url) {
        this.url = url;
    }

    /**
     * Gets the URL of the loaded page
     * @return URL of page
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Gets links which were found in the page
     * @return list of links
     */
    public List<String> getLinks() {
        return links;
    }

    /**
     * Gets body of the page
     * @return string with page's body
     */
    public String getPage() {
        return page;
    }
    
    /**
     * Executes the loading of the page and parsing new links
     */
    public void load() {
        try {
            Document doc = Jsoup.connect(url).get();
            parseLinks(doc);
            //Check for new liks
            page = doc.outerHtml();
        } catch (IOException ex) {
            System.err.println("Error in load the page " + url + " -> " + ex);
        }
    }
    
    /**
     * Parses the page and finds new links
     * @param doc contains a page
     */
    private void parseLinks(Document doc) {
        Elements hrefs = doc.select("a[href]");
        for (Element href : hrefs) {
            links.add(href.attr("abs:href"));
        }
        hrefs = doc.select("link[href]");
        for (Element href : hrefs) {
            links.add(href.attr("abs:href"));
        }
        Elements srcs = doc.select("[src]");
        srcs.stream().filter(src -> (!src.normalName().equals("img"))).forEachOrdered(src -> {
            links.add(src.attr("abs:src"));
        });
    }
}
