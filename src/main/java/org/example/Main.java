package org.example;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author Nicholas Curl
 */
public class Main {

    /**
     * The instance of the logger
     */
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final Path   output = Paths.get("./output");
    public static void main(String[] args) throws IOException {
        Files.createDirectories(output);
        File[] files = Paths.get("./contacts").toFile().listFiles();
        if (files != null && files.length > 0) {
            ConcurrentHashMap<Long, List<Long>> matched = new ConcurrentHashMap<>();
            CustomThreadPoolExecutor threadPoolExecutor = new CustomThreadPoolExecutor(30,
                                                                                       30,
                                                                                       0L,
                                                                                       TimeUnit.MILLISECONDS,
                                                                                       new LinkedBlockingQueue<>(),
                                                                                       new StoringRejectedExecutionHandler()
            );
            ProgressBar progressBar = new ProgressBarBuilder().setInitialMax(files.length)
                                                              .setMaxRenderedLength(120)
                                                              .setTaskName("Comparing")
                                                              .setUpdateIntervalMillis(1)
                                                              .setStyle(
                                                                      ProgressBarStyle.ASCII)
                                                              .build();
            List<Long> idOnly = Collections.synchronizedList(new ArrayList<>());
            List<Long> email = Collections.synchronizedList(new ArrayList<>());
            List<Long> firstOrLastNameOnly = Collections.synchronizedList(new ArrayList<>());
            List<Long> longName = Collections.synchronizedList(new ArrayList<>());
            for (File file : files) {
                threadPoolExecutor.submit(()-> {
                    String fileName = file.getName().replaceAll("\\.txt", "");
                    String[] fileNameSplit = fileName.split("_");
                    long id = Long.parseLong(fileNameSplit[fileNameSplit.length - 1]);
                    if(fileNameSplit.length == 1){
                        idOnly.add(id);
                    }
                    if(fileName.contains("@")){
                        email.add(id);
                    }
                    if(fileNameSplit.length==2 && !fileName.contains("@")){
                        firstOrLastNameOnly.add(id);
                    }
                    if(fileNameSplit.length>3){
                        longName.add(id);
                    }
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < (fileNameSplit.length - 1); i++) {
                        builder.append(fileNameSplit[i]).append("_");
                    }
                    if (builder.length() != 0) {
                        builder.deleteCharAt(builder.length() - 1);
                    }
                    fileName = builder.toString();
                    ArrayList<Long> ids = new ArrayList<>();
                    for (File file1 : files) {
                        if (!file.equals(file1)) {

                            String fileName1 = file1.getName().replaceAll("\\.txt", "");
                            String[] fileNameSplit1 = fileName1.split("_");
                            long id1 = Long.parseLong(fileNameSplit1[fileNameSplit1.length - 1]);
                            builder = new StringBuilder();
                            for (int i = 0; i < (fileNameSplit1.length - 1); i++) {
                                builder.append(fileNameSplit1[i]).append("_");
                            }
                            if (builder.length() != 0) {
                                builder.deleteCharAt(builder.length() - 1);
                            }
                            fileName1 = builder.toString();
                            if (fileName.isBlank() || fileName1.isBlank()) {
                                if (id == id1) {
                                    ids.add(id1);
                                }
                            }
                            else {
                                if (fileName.equalsIgnoreCase(fileName1)) {
                                    ids.add(id1);
                                }
                            }
                        }
                    }
                    if(!ids.isEmpty()) {
                        matched.put(id,ids);
                    }
                    progressBar.step();
                });
            }
            threadPoolExecutor.shutdown();
            try {
                if (!threadPoolExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                    logger.warn("Termination Timeout");
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            progressBar.close();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("duplicates", matched);
            jsonObject.put("idOnly", idOnly);
            jsonObject.put("email", email);
            jsonObject.put("firstOrLastNameOnly", firstOrLastNameOnly);
            jsonObject.put("longName", longName);
            FileWriter writer = new FileWriter(output.resolve("corrections.json").toFile());
            writer.write(jsonObject.toString(4));
            writer.close();
        }

    }
}
