package com.testtask.web.crawler.storage;

/**
 *
 * @author aberdnikov
 */
public interface PageStorage {
    /**
     * Stores a page
     * @param url - URL of the page
     * @param body - body of the page to be stored
     */
    void store(String url, String body);
}
