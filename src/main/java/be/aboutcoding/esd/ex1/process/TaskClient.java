package be.aboutcoding.esd.ex1.process;

import be.aboutcoding.esd.ex1.infrastructure.Task;
import be.aboutcoding.esd.ex1.model.TS50X;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public TS50X scheduleFirmwareUpdate(TS50X TS50X) {
        var taskId = scheduleTask(Task.createFirmwareUpdateTaskFor(TS50X.getId()));
        logger.info("Scheduled firmware update task with ID: {} for sensor: {}", taskId, TS50X.getId());

        TS50X.setStatus("updating_firmware");
        return TS50X;
    }

    public TS50X scheduleConfigurationUpdate(TS50X TS50X) {
        var taskId = scheduleTask(Task.createConfigUpdateTaskFor(TS50X.getId()));
        logger.info("Scheduled configuration update task with ID: {} for sensor: {}", taskId, TS50X.getId());

        TS50X.setStatus("updating_configuration");
        return TS50X;
    }

    private String scheduleTask(Task requestBody) {
        var headers = new HttpHeaders();
        headers.set("x-auth-id", properties.authKey());
        var completeUrl = properties.url() + TASK_URI;

        var requestEntity = new HttpEntity<>(requestBody, headers);
        var response = restTemplate.exchange(
                completeUrl,
                HttpMethod.PUT,
                requestEntity,
                Map.class
        );

        if (response.getStatusCodeValue() != 201) {
            throw new RuntimeException("Failed to schedule task. Received status: " + response.getStatusCodeValue());
        }

        var responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("id")) {
            throw new RuntimeException("Invalid response: missing task ID");
        }

        return responseBody.get("id").toString();
    }
}