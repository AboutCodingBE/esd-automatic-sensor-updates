package be.aboutcoding.esd.ex1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for scheduling firmware and configuration update tasks.
 */
@Component
public class TaskClient {

    private static final Logger logger = LoggerFactory.getLogger(TaskClient.class);
    private static final String FIRMWARE_UPDATE_STATUS = "updating_firmware";
    private static final String CONFIGURATION_UPDATE_STATUS = "configuration_update";

    private final RestTemplate restTemplate;
    private final String taskApiUrl;
    private final SensorInformationClient sensorInformationClient;

    /**
     * Constructor for TaskClient.
     *
     * @param restTemplate The RestTemplate for making HTTP requests
     * @param taskApiUrl The base URL of the task API
     * @param sensorInformationClient Client for retrieving sensor information
     */
    public TaskClient(
            RestTemplate restTemplate,
            @Value("${task.api.url}") String taskApiUrl,
            SensorInformationClient sensorInformationClient) {
        this.restTemplate = restTemplate;
        this.taskApiUrl = taskApiUrl;
        this.sensorInformationClient = sensorInformationClient;
    }

    /**
     * Schedules a firmware update task for a sensor.
     *
     * @param sensorId The ID of the sensor that needs a firmware update
     * @return The updated Sensor object with the new status
     */
    public Sensor scheduleFirmwareUpdate(String sensorId) {
        logger.info("Scheduling firmware update task for sensor ID: {}", sensorId);

        try {
            String url = taskApiUrl + "/tasks/firmware-update";

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sensorId", sensorId);
            requestBody.put("priority", "high");
            requestBody.put("taskType", "firmware_update");

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request entity
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Make POST request
            ResponseEntity<TaskResponse> response = restTemplate.postForEntity(url, request, TaskResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Successfully scheduled firmware update task for sensor ID: {}", sensorId);
                return updateSensorWithStatus(sensorId, FIRMWARE_UPDATE_STATUS);
            } else {
                logger.warn("Failed to schedule firmware update task for sensor ID: {}", sensorId);
                return getSensorWithOriginalStatus(sensorId);
            }
        } catch (Exception e) {
            logger.error("Error scheduling firmware update task for sensor ID {}: {}", sensorId, e.getMessage());
            return getSensorWithOriginalStatus(sensorId);
        }
    }

    /**
     * Schedules a configuration update task for a sensor.
     *
     * @param sensorId The ID of the sensor that needs a configuration update
     * @return The updated Sensor object with the new status
     */
    public Sensor scheduleConfigurationUpdate(String sensorId) {
        logger.info("Scheduling configuration update task for sensor ID: {}", sensorId);

        try {
            String url = taskApiUrl + "/tasks/configuration-update";

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sensorId", sensorId);
            requestBody.put("priority", "medium");
            requestBody.put("taskType", "configuration_update");
            requestBody.put("configFile", "config123.cfg");

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request entity
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Make POST request
            ResponseEntity<TaskResponse> response = restTemplate.postForEntity(url, request, TaskResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Successfully scheduled configuration update task for sensor ID: {}", sensorId);
                return updateSensorWithStatus(sensorId, CONFIGURATION_UPDATE_STATUS);
            } else {
                logger.warn("Failed to schedule configuration update task for sensor ID: {}", sensorId);
                return getSensorWithOriginalStatus(sensorId);
            }
        } catch (Exception e) {
            logger.error("Error scheduling configuration update task for sensor ID {}: {}", sensorId, e.getMessage());
            return getSensorWithOriginalStatus(sensorId);
        }
    }

    /**
     * Updates a sensor with a new status.
     *
     * @param sensorId The ID of the sensor
     * @param status The new status to set
     * @return The updated Sensor object
     */
    private Sensor updateSensorWithStatus(String sensorId, String status) {
        Sensor sensor = sensorInformationClient.getSensorInformation(sensorId);
        sensor.setStatus(status);
        return sensor;
    }

    /**
     * Gets a sensor with its original status.
     * Used when task scheduling fails.
     *
     * @param sensorId The ID of the sensor
     * @return The original Sensor object
     */
    private Sensor getSensorWithOriginalStatus(String sensorId) {
        return sensorInformationClient.getSensorInformation(sensorId);
    }

    /**
     * Inner class representing the structure of the task API response.
     */
    private static class TaskResponse {
        private String taskId;
        private String status;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
