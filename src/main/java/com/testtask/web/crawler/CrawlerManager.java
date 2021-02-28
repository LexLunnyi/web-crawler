package com.testtask.web.crawler;

import com.testtask.web.crawler.storage.PageStorage;
import com.testtask.web.crawler.storage.URLStatuses;
import com.testtask.web.crawler.storage.impl.PageStorageImpl;
import com.testtask.web.crawler.storage.impl.URLStatusesImpl;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 *
 * @author aberdnikov
 */
public class CrawlerManager {
    Integer threadsNumber;
    String initURL;
    String outputPath;
    
    final Queue<String> ongoing = new LinkedList<>();
    final Set<String> full = new HashSet<>();
    final List<Future<URLCrawler>> futured = new LinkedList<>();
    
    final ExecutorService executorService;
    final URLStatuses statuses;
    final PageStorage pageStorage; 

    /**
     * Constructs a crawler manager that will control parallel pages loading process.
     * 
     * @param threadsNumber - number of parallel threads which will load the pages
     * @param initURL - initial URL
     * @param outputPath - path where the pages will be stored
     */
    public CrawlerManager(Integer threadsNumber, String initURL, String outputPath) {
        this.threadsNumber = threadsNumber;
        this.initURL = initURL;
        this.outputPath = outputPath;
        
        executorService = Executors.newFixedThreadPool(threadsNumber);
        pageStorage = new PageStorageImpl(outputPath, initURL);
        statuses = new URLStatusesImpl(outputPath, initURL);
    }

    /**
     * Starts the loading process. If the WebCrawler have been executed for initURL 
     * before then already loaded pages will be ignored. Otherwise the loading
     * will start from initial URL.
     */
    public void start() {
        //If the crawler was run before load the urls which were processed
        if (statuses.exist()) {
            statuses.loadOngoing(ongoing);
            statuses.loadFull(full);
        } else {
            ongoing.add(initURL);
            full.add(initURL);
        }
        
        queueNewPages(threadsNumber);
    }

    /**
     * Stops the loading process. All already loaded URLs will be stored for
     * ignoring at the next execution.
     */
    public void stop() {
        //Stop loading threads
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            System.out.println("executorService InterruptedException " + ex);
        }
        //Store last loaded pages
        storeNewPages();
        //Store loaded and found URLs
        statuses.storeOngoing(ongoing);
        statuses.storeFull(full);
    }
    
    /**
     * Checks the loaded pages and store if they were found.
     * @return true if there aren't newfound URLs and the all previous were loaded
     */
    public boolean process() {
        //Check for loaded pages
        storeNewPages();
        //Queue new pages to be load
        int queued = queueNewPages(threadsNumber - futured.size());
        return (0 == queued) && (futured.isEmpty());
    }
    
    /**
     * Checks already loaded pages, store them and get new URLs from them.
     */
    private void storeNewPages() {
        //Looking for new loaded pages and storing them
        List<Future<URLCrawler>> finished = futured.stream().filter(item->item.isDone()).collect(Collectors.toList());
        finished.forEach(item -> {
            try {
                URLCrawler crawler = item.get();
                //Store the page
                pageStorage.store(crawler.getUrl(), crawler.getPage());
                //Queue found links
                addNewURLs(crawler.getLinks());
            } catch (InterruptedException | ExecutionException ex) {
                System.err.println("Error to store page -> " + ex);
            }
        });
        //Remove stored elements
        futured.removeAll(finished);
    }

    /**
     * Adds the new URLs to the ongoing list.
     * @param urls - list of the new URLs
     */
    private void addNewURLs(List<String> urls) {
        for (String url : urls) {
            //Check that it doesn't outside url
            if (!url.startsWith(initURL)) continue;
            //Check that it wasn't parsed before
            if (full.contains(url)) continue;
            
            ongoing.add(url);
            full.add(url);
        }
    }
    
    /**
     * Takes the URLs from the ongoing list and starts to load them
     * @param count - number of the URLs to take
     * @return actual number of the URLs which were took
     */
    private int queueNewPages(int count) {
        int cnt = 0;
        for (int i = 0; i < count; i++) {
            //Looking for new URLs from queue
            String url = ongoing.poll();
            if (null == url) {
                break;
            }
            //Create new task for load a page
            Callable<URLCrawler> task = () -> {
                URLCrawler res = new URLCrawler(url);
                res.load();
                return res;
            };
            //Submit the task
            futured.add(executorService.submit(task));
            cnt++;

        }
        return cnt;
    }
}
