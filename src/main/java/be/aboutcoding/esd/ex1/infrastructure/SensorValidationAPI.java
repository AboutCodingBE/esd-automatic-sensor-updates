package be.aboutcoding.esd.ex1.infrastructure;

import be.aboutcoding.esd.ex1.process.SensorValidationProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sensors")
public class SensorValidationAPI {

    private static final Logger logger = LoggerFactory.getLogger(SensorValidationAPI.class);

    private final SensorValidationProcess validationProcess;
    private final IdParser idParser;

    public SensorValidationAPI(SensorValidationProcess validationProcess, IdParser idParser) {
        this.validationProcess = validationProcess;
        this.idParser = idParser;
    }

    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> validateSensors(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            logger.warn("Empty file uploaded");
            return ResponseEntity.badRequest().body("Please upload a non-empty CSV file");
        }

        try {
            var sensorIds = idParser.parse(file.getInputStream());
            var validatedSensors = validationProcess.validateSensors(sensorIds);

            // Transform to simple response format
            List<SensorValidationResponse> response = validatedSensors.stream()
                    .map(sensor -> new SensorValidationResponse(sensor.getId(), sensor.getStatus()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            logger.error("Error reading uploaded file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to read the uploaded file");
        }
    }

    /**
     * Simple DTO for sensor validation response.
     */
    private static record SensorValidationResponse(Long id, String status) {
    }
}
