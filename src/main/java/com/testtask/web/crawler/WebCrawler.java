package com.testtask.web.crawler;

import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.cli.*;

/**
 *
 * @author aberdnikov
 */
public class WebCrawler {

    public static void main(String[] args) {
        Options options = new Options();

        Option optInitURL = new Option("u", "url", true, "Init URL");
        optInitURL.setRequired(true);
        options.addOption(optInitURL);

        Option optOutput = new Option("o", "output", true, "Output path");
        optOutput.setRequired(true);
        options.addOption(optOutput);

        Option optThreadNumber = new Option("n", "threads-number", true, "Number of threads");
        optThreadNumber.setRequired(false);
        options.addOption(optThreadNumber);

        Option optHelp = new Option("h", "help", false, "Show help");
        optHelp.setRequired(false);
        options.addOption(optHelp);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("WebCrawler", options);
            System.exit(1);
        }

        if (cmd.hasOption("help")) {
            formatter.printHelp("WebCrawler", options);
            System.exit(0);
        }

        Integer threadsNumber = Integer.parseInt(cmd.getOptionValue("threads-number", "1"));
        String initURL = cmd.getOptionValue("url");
        String outputPath = cmd.getOptionValue("output");

        System.out.println("THREADS COUNT -> " + threadsNumber);
        System.out.println("INIT URL -> " + initURL);
        System.out.println("OUTPUT PATH -> " + outputPath);

        start(new CrawlerManager(threadsNumber, initURL, outputPath));
        System.exit(0);
    }

    public static void start(CrawlerManager cManager) {
        final AtomicBoolean running = new AtomicBoolean(true);
        
        final Thread mainThread = new Thread() {
            @Override
            public void run() {
                System.out.println("START CRAWLNG\n");
                cManager.start();
                while (running.get()) {
                    if (cManager.process()) {
                        break;
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
                cManager.stop();
                System.out.println("Main thread finished\n");
            }
        };

        final Thread sHook = new Thread() {
            @Override
            public void run() {
                try {
                    running.set(false);
                    mainThread.join();
                } catch (InterruptedException ex) {
                    System.err.println("ShutdownHook exeption: " + ex.getMessage());
                }
            }
        };

        Runtime.getRuntime().addShutdownHook(sHook);
        mainThread.start();
        try {
            mainThread.join();
        } catch (InterruptedException ex) {
            System.err.println("mainThread join exeption: " + ex.getMessage());
        }
    }
}
