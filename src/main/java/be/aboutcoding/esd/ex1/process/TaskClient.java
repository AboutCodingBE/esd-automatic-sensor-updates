package be.aboutcoding.esd.ex1.process;

import be.aboutcoding.esd.ex1.infrastructure.Task;
import be.aboutcoding.esd.ex1.model.TS50X;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
public class TaskClient {

    public static final String TASK_URI = "/tasks";

    private final ApiProperties properties;
    private final RestTemplate restTemplate;

    public TaskClient(RestTemplate restTemplate,
                      ApiProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public TS50X scheduleFirmwareUpdate(TS50X sensor) {
        try {
            var taskId = scheduleTask(Task.createFirmwareUpdateTaskFor(sensor.getId()));
            log.info("Scheduled firmware update task with ID: {} for sensor: {}", taskId, sensor.getId());

            sensor.setStatus("updating_firmware");
        }
        catch(HttpClientErrorException | HttpServerErrorException exception) {
            log.error("Scheduling firmware update task failed for sensor: {}", sensor.getId());
            sensor.setStatus("Update_failed");
        }
        return sensor;
    }

    public TS50X scheduleConfigurationUpdate(TS50X sensor) {
        try {
            var taskId = scheduleTask(Task.createConfigUpdateTaskFor(sensor.getId()));
            log.info("Scheduled configuration update task with ID: {} for sensor: {}", taskId, sensor.getId());

            sensor.setStatus("updating_configuration");
        }
        catch(HttpClientErrorException | HttpServerErrorException exception) {
            log.error("Scheduling configuration update task failed for sensor: {}", sensor.getId());

            sensor.setStatus("Update_failed");
        }
        return sensor;
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

        var responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("id")) {
            throw new RuntimeException("Invalid response: missing task ID");
        }

        return responseBody.get("id").toString();
    }
}