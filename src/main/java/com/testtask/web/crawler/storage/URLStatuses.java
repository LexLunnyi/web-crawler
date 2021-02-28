package com.testtask.web.crawler.storage;

import java.util.Queue;
import java.util.Set;

/**
 *
 * @author aberdnikov
 */
public interface URLStatuses {
    /**
     * Loads ongoing (from previous running) URLs from local storage
     * @param urls - list of the URLs
     */
    void loadOngoing(Queue<String> urls);
    
    /**
     * Stores ongoing (from current running) URLs to local storage
     * @param urls - list of the URLs
     */
    void storeOngoing(Queue<String> urls);
    
    /**
     * Loads full processed (from previous running) URLs from local storage
     * @param urls - set of the URLs
     */
    void loadFull(Set<String> urls);
    
    /**
     * Loads full processed (from current running) URLs to local storage
     * @param urls - set of the URLs
     */
    void storeFull(Set<String> urls);
    
    /**
     * Check existing of the previous running
     * @return true if the crawler was run before
     */
    boolean exist();
}
