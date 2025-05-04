package be.aboutcoding.esd.ex1.infrastructure;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
class IdParser {

    /**
     * Parses sensor IDs from a CSV input stream using Apache Commons CSV.
     *
     * @param inputStream The CSV input stream containing sensor IDs and types
     * @return A list of sensor IDs extracted from the CSV
     */
    public List<Long> parse(InputStream inputStream) {
        List<Long> sensorIds = new ArrayList<>();

        try (Reader reader = new InputStreamReader(inputStream);
             CSVParser csvParser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .parse(reader)) {

            for (CSVRecord record : csvParser) {
                String id = record.get("id");
                if (id != null && !id.isEmpty()) {
                    sensorIds.add(Long.parseLong(id));
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error parsing sensor IDs from CSV", e);
        }
        return sensorIds;
    }
}
