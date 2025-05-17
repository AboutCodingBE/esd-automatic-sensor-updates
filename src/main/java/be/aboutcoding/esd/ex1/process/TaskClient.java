package be.aboutcoding.esd.ex1.process;

import be.aboutcoding.esd.ex1.model.Sensor;
import be.aboutcoding.esd.ex1.infrastructure.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class TaskClient {

    private static final Logger logger = LoggerFactory.getLogger(TaskClient.class);
    public static final String TASK_URI = "/tasks";

    private final ApiProperties properties;
    private final RestTemplate restTemplate;

    public TaskClient(RestTemplate restTemplate,
                      ApiProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public Sensor scheduleFirmwareUpdate(Sensor sensor) {
        String taskId = scheduleTask(Task.createFirmwareUpdateTaskFor(sensor.getId()));
        logger.info("Scheduled firmware update task with ID: {} for sensor: {}", taskId, sensor.getId());

        sensor.setStatus("updating_firmware");
        return sensor;
    }

    public Sensor scheduleConfigurationUpdate(Sensor sensor) {
        String taskId = scheduleTask(Task.createConfigUpdateTaskFor(sensor.getId()));
        logger.info("Scheduled configuration update task with ID: {} for sensor: {}", taskId, sensor.getId());

        sensor.setStatus("updating_configuration");
        return sensor;
    }

    private String scheduleTask(Task requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-auth-id", properties.authKey());
        var completeUrl = properties.url() + TASK_URI;

        HttpEntity<Task> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                completeUrl,
                HttpMethod.PUT,
                requestEntity,
                Map.class
        );

        if (response.getStatusCodeValue() != 201) {
            throw new RuntimeException("Failed to schedule task. Received status: " + response.getStatusCodeValue());
        }

        Map responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("id")) {
            throw new RuntimeException("Invalid response: missing task ID");
        }

        return responseBody.get("id").toString();
    }
}