package com.tianle_cinemaScrapper.cinemaScrapper.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CsvUtils {
    //fallback function
    public static void writeToCSV(String filePrefix, String elCinemaId, String url, String errorMessage) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String fileName = filePrefix + "_" + today + ".csv";
        Path path = null;
        try {
            path = Paths.get(fileName);

            boolean isNewFile = !Files.exists(path);
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                 CSVPrinter csvPrinter = new CSVPrinter(bufferedWriter, CSVFormat.DEFAULT)) {
                if (isNewFile) {
                    csvPrinter.printRecord("elCinemaId", "URL", "ErrorMessage");
                }

                csvPrinter.printRecord(elCinemaId, url, errorMessage);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
