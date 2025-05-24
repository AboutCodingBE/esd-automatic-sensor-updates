package be.aboutcoding.esd.ex1.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
class IdParser {

    public List<Long> parse(MultipartFile idsFile) {
        List<Long> sensorIds = new ArrayList<>();

        try (Reader reader = new InputStreamReader(idsFile.getInputStream());
             var csvParser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim()
                     .parse(reader)) {

            for (CSVRecord record : csvParser) {
                String id = record.get("id");
                if (id != null && !id.isEmpty()) {
                    try {
                        sensorIds.add(Long.parseLong(id));
                    }
                    catch(NumberFormatException numberFormat) {
                        log.error("File contains id that is not a nubmer: {}", id);
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading sensor IDs from CSV", e);
        }
        return sensorIds;
    }
}
