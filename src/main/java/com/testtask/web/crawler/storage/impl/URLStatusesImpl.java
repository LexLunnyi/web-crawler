package com.testtask.web.crawler.storage.impl;

import com.testtask.web.crawler.storage.URLStatuses;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author aberdnikov
 */
public class URLStatusesImpl implements URLStatuses {
    final String path;
    final String urlPath;
    final String ongoinPath;
    final String fullPath;
    
    final static String ONGOING_NAME = "ongoing.txt";
    final static String FULL_NAME = "full.txt";

    public URLStatusesImpl(String path, String urlPath) {
        this.path = path;
        this.urlPath = urlPath.replace("/", "_");
        
        ongoinPath = path + "/" + this.urlPath + "/" + ONGOING_NAME;
        fullPath = path + "/" + this.urlPath + "/" + FULL_NAME;
    }

    @Override
    public void loadOngoing(Queue<String> urls) {
        if (!exist()) return;
        try {
            urls.addAll(Files.readAllLines(Paths.get(ongoinPath), 
                        Charset.defaultCharset()));
        } catch (IOException ex) {
            System.err.println("Error load ongoing URLs from storage -> " + ex);
        }
    }

    @Override
    public void storeOngoing(Queue<String> urls) {
        save(ongoinPath, urls);
    }

    @Override
    public void loadFull(Set<String> urls) {
        if (!exist()) return;
        try {
            urls.addAll(Files.readAllLines(Paths.get(fullPath), 
                        Charset.defaultCharset()));
        } catch (IOException ex) {
            System.err.println("Error load full URLs from storage -> " + ex);
        }
    }

    @Override
    public void storeFull(Set<String> urls) {
        save(fullPath, urls);
    }

    @Override
    public boolean exist() {
        if (!Files.exists(Paths.get(path + "/" + urlPath))) return false;
        if (!Files.exists(Paths.get(ongoinPath))) return false;
        return Files.exists(Paths.get(fullPath));
    }
    
    private void save(String filePath, Collection<String> urls) {
        File theDir = new File(path + "/" + urlPath);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String url : urls) {
                writer.write(url + '\n');
            }
        } catch (IOException ex) {
            System.err.println("Error saving file " + filePath + " -> " + ex);
        }
    }
}