package be.aboutcoding.esd.ex1;

import be.aboutcoding.esd.ex1.infrastructure.Task;
import be.aboutcoding.esd.ex1.model.TS50X;
import be.aboutcoding.esd.ex1.process.TaskClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest
@ActiveProfiles("test")
class TaskClientTest {

    @Autowired
    private TaskClient taskClient;
    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper;

    private static final Long SENSOR_ID = 123456789L;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void scheduleFirmwareUpdate_shouldReturnSensorWithUpdatedStatus() throws Exception{
        // Arrange
        var TS50X = new TS50X(SENSOR_ID, "50.1.12Rev1", "config123.cfg");
        var expectedRequest = Task.createFirmwareUpdateTaskFor(TS50X.getId());
        var taskRequestBody = objectMapper.writeValueAsString(expectedRequest);

        // Setup mock response
        mockServer.expect(requestTo("http://localhost:8086/api/tasks"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("x-auth-id", "CL019567d9-6e3b-738b-9b6d-32496110bd35=="))
                .andExpect(content().json(taskRequestBody))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":\"task-123\"}"));

        // Act
        var updatedSensor = taskClient.scheduleFirmwareUpdate(TS50X);

        // Assert
        mockServer.verify();
        assertThat(updatedSensor).isNotNull();
        assertThat(updatedSensor.getId()).isEqualTo(SENSOR_ID);
        assertThat(updatedSensor.getStatus()).isEqualTo("updating_firmware");
    }

    @Test
    void scheduleConfigurationUpdate_shouldReturnSensorWithUpdatedStatus() throws Exception{
        // Arrange
        var sensor = new TS50X(SENSOR_ID, "59.1.12Rev4", "invalid_config.cfg");
        var expectedRequest = Task.createConfigUpdateTaskFor(sensor.getId());
        var taskRequestBody = objectMapper.writeValueAsString(expectedRequest);

        // Setup mock response
        mockServer.expect(requestTo("http://localhost:8086/api/tasks"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("x-auth-id", "CL019567d9-6e3b-738b-9b6d-32496110bd35=="))
                .andExpect(content().json(taskRequestBody))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\":\"task-456\"}"));

        // Act
        var updatedSensor = taskClient.scheduleConfigurationUpdate(sensor);

        // Assert
        mockServer.verify();
        assertThat(updatedSensor).isNotNull();
        assertThat(updatedSensor.getId()).isEqualTo(SENSOR_ID);
        assertThat(updatedSensor.getStatus()).isEqualTo("updating_configuration");
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"INTERNAL_SERVER_ERROR", "BAD_REQUEST", "FORBIDDEN", "GATEWAY_TIMEOUT"})
    void shouldReturnSensorWithStatusUpdateFailedWhenConfigUpdateRequestFails(HttpStatus status) throws Exception {
        var sensor = new TS50X(SENSOR_ID, "50.1.49Rev12", "config.cfg");
        var expectedRequest = Task.createConfigUpdateTaskFor(sensor.getId());
        var taskRequestBody = objectMapper.writeValueAsString(expectedRequest);

        mockServer.expect(requestTo("http://localhost:8086/api/tasks"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("x-auth-id", "CL019567d9-6e3b-738b-9b6d-32496110bd35=="))
                .andExpect(content().json(taskRequestBody))
                .andRespond(withStatus(status));

        var updatedSensor = taskClient.scheduleConfigurationUpdate(sensor);

        assertThat(updatedSensor.getId()).isEqualTo(SENSOR_ID);
        assertThat(updatedSensor.getStatus()).isEqualTo("Update_failed");
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"INTERNAL_SERVER_ERROR", "BAD_REQUEST", "FORBIDDEN", "GATEWAY_TIMEOUT"})
    void shouldReturnSensorWithStatusUpdateFailedWhenFirmwareUpdateRequestFails(HttpStatus status) throws Exception {
        var sensor = new TS50X(SENSOR_ID, "50.1.49Rev12", "config.cfg");
        var expectedRequest = Task.createFirmwareUpdateTaskFor(sensor.getId());
        var taskRequestBody = objectMapper.writeValueAsString(expectedRequest);

        mockServer.expect(requestTo("http://localhost:8086/api/tasks"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("x-auth-id", "CL019567d9-6e3b-738b-9b6d-32496110bd35=="))
                .andExpect(content().json(taskRequestBody))
                .andRespond(withStatus(status));

        var updatedSensor = taskClient.scheduleFirmwareUpdate(sensor);

        assertThat(updatedSensor.getId()).isEqualTo(SENSOR_ID);
        assertThat(updatedSensor.getStatus()).isEqualTo("Update_failed");
    }
}