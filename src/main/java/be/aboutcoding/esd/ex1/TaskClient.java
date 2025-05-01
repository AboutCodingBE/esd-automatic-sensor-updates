package be.aboutcoding.esd.ex1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for scheduling firmware and configuration update tasks via the 3rd party API.
 */
@Component
public class TaskClient {

    private static final Logger logger = LoggerFactory.getLogger(TaskClient.class);
    private static final String API_URL = "www.mysensor.io/api/tasks";
    private static final String AUTH_HEADER = "x-auth-id";
    private static final String AUTH_VALUE = "CL019567d9-6e3b-738b-9b6d-32496110bd35==";

    // Task types
    private static final String UPDATE_FIRMWARE_TYPE = "update_firmware";
    private static final String UPDATE_CONFIGURATION_TYPE = "update_configuration";

    // Status values
    private static final String UPDATING_FIRMWARE_STATUS = "updating_firmware";
    private static final String UPDATING_CONFIGURATION_STATUS = "updating_configuration";

    private final RestTemplate restTemplate;

    /**
     * Constructor for TaskClient.
     *
     * @param restTemplate The RestTemplate for making HTTP requests
     */
    public TaskClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Schedules a firmware update task for a sensor.
     *
     * @param sensor The sensor that needs a firmware update
     * @return The updated Sensor object with the new status
     */
    public Sensor scheduleFirmwareUpdate(Sensor sensor) {
        logger.info("Scheduling firmware update task for sensor ID: {}", sensor.getId());

        try {
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sensor_serial", Long.parseLong(sensor.getId()));
            requestBody.put("type", UPDATE_FIRMWARE_TYPE);
            // Note: file_id is optional for firmware updates as the latest will be installed

            // Make the API call
            String taskId = sendTaskRequest(requestBody);
            logger.info("Successfully scheduled firmware update task with ID: {} for sensor ID: {}", taskId, sensor.getId());

            // Create and return updated sensor with new status
            Sensor updatedSensor = new Sensor(
                    sensor.getId(),
                    sensor.getFirmwareVersion(),
                    sensor.getConfiguration(),
                    UPDATING_FIRMWARE_STATUS
            );
            return updatedSensor;

        } catch (Exception e) {
            logger.error("Error scheduling firmware update task for sensor ID {}: {}", sensor.getId(), e.getMessage());
            throw new RuntimeException("Failed to schedule firmware update task", e);
        }
    }

    /**
     * Schedules a configuration update task for a sensor.
     *
     * @param sensor The sensor that needs a configuration update
     * @return The updated Sensor object with the new status
     */
    public Sensor scheduleConfigurationUpdate(Sensor sensor) {
        logger.info("Scheduling configuration update task for sensor ID: {}", sensor.getId());

        try {
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sensor_serial", Long.parseLong(sensor.getId()));
            requestBody.put("type", UPDATE_CONFIGURATION_TYPE);
            requestBody.put("file_id", "a3e4aed2-b091-41a6-8265-2185040e2c32"); // Example file ID for config123.cfg

            // Make the API call
            String taskId = sendTaskRequest(requestBody);
            logger.info("Successfully scheduled configuration update task with ID: {} for sensor ID: {}", taskId, sensor.getId());

            // Create and return updated sensor with new status
            Sensor updatedSensor = new Sensor(
                    sensor.getId(),
                    sensor.getFirmwareVersion(),
                    sensor.getConfiguration(),
                    UPDATING_CONFIGURATION_STATUS
            );
            return updatedSensor;

        } catch (Exception e) {
            logger.error("Error scheduling configuration update task for sensor ID {}: {}", sensor.getId(), e.getMessage());
            throw new RuntimeException("Failed to schedule configuration update task", e);
        }
    }

    /**
     * Sends a task request to the API.
     *
     * @param requestBody The request body to send
     * @return The task ID from the response
     */
    private String sendTaskRequest(Map<String, Object> requestBody) {
        // Prepare headers with authentication
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTH_HEADER, AUTH_VALUE);

        // Create request entity with body and headers
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Send PUT request
        ResponseEntity<Map> response = restTemplate.exchange(
                API_URL,
                HttpMethod.PUT,
                requestEntity,
                Map.class
        );

        // Check response status
        if (response.getStatusCodeValue() != 201) {
            throw new RuntimeException("Unexpected response status: " + response.getStatusCodeValue());
        }

        // Extract task ID from response
        if (response.getBody() != null && response.getBody().containsKey("id")) {
            return response.getBody().get("id").toString();
        } else {
            throw new RuntimeException("Task ID not found in response");
        }
    }
}