package com.AI.AIEXAMPLE.Service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataService {

    public static List<Double> readDoublesFromCSV(String filePath) {
        List<Double> doubleList = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> lines = reader.readAll();

            for (String[] line : lines) {
                for (String value : line) {
                    try {
                        doubleList.add(Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid double value: " + value);
                    }
                }
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }

        return doubleList;
    }
}
