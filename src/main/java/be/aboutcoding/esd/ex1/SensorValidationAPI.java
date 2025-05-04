package be.aboutcoding.esd.ex1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API for sensor validation operations.
 * Provides endpoints for validating sensor firmware and configuration.
 */
@RestController
@RequestMapping("/api/sensors")
public class SensorValidationAPI {

    private static final Logger logger = LoggerFactory.getLogger(SensorValidationAPI.class);

    private final SensorValidationProcess validationProcess;

    public SensorValidationAPI(SensorValidationProcess validationProcess) {
        this.validationProcess = validationProcess;
    }

    /**
     * Endpoint for validating a list of sensors from a CSV file.
     *
     * @param file The CSV file containing sensor IDs and types
     * @return A list of validation results for each sensor
     */
    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> validateSensors(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            logger.warn("Empty file uploaded");
            return ResponseEntity.badRequest().body("Please upload a non-empty CSV file");
        }

        try {
            // Process the CSV file
            List<Sensor> validatedSensors = validationProcess.validateSensors(file.getInputStream());

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
