package com.testtask.web.crawler.storage.impl;

import com.testtask.web.crawler.storage.PageStorage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author aberdnikov
 */
public class PageStorageImpl implements PageStorage {

    final String path;
    final String urlPath;

    public PageStorageImpl(String path, String urlPath) {
        this.path = path;
        this.urlPath = urlPath.replace("/", "_");
    }

    @Override
    public void store(String url, String body) {
        System.out.println(url);
        File theDir = new File(path + "/" + urlPath);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
        String pagePath = path + "/" + urlPath + "/" + url.replace("/", "_");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(pagePath))) {
            writer.write(body);
        } catch (IOException ex) {
            System.err.println("Error saving file " + pagePath + " -> " + ex);
        }
    }
}