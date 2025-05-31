package be.aboutcoding.esd.ex1.process;

import be.aboutcoding.esd.ex1.model.TS50X;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
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
class SensorInformationClientTest {

    private static final String BASE_URL = "http://localhost:8086/api";
    private static final Long SENSOR_ID = 1234567l;

    @Autowired
    private SensorInformationClient sensorInformationClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        // Create mock server
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void getSensorInformation_shouldReturnSensorWithFirmwareAndConfiguration() {
        // Arrange
        var expectedFirmwareVersion = "59.1.12Rev4";
        var expectedConfiguriation = "some_configuration.cfg";

        // Setup mock response
        mockServer.expect(requestTo(BASE_URL + "/sensors/" + SENSOR_ID))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("x-auth-id", "CL019567d9-6e3b-738b-9b6d-32496110bd35=="))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(sensorInformationFor(SENSOR_ID)));

        // Act
        TS50X sensor = sensorInformationClient.getSensorInformation(SENSOR_ID);

        // Assert
        mockServer.verify();
        assertThat(sensor).isNotNull();
        assertThat(sensor.getId()).isEqualTo(SENSOR_ID);
        assertThat(sensor.getFirmwareVersion()).isEqualTo(expectedFirmwareVersion);
        assertThat(sensor.getConfiguration()).isEqualTo(expectedConfiguriation);
        assertThat(sensor.getStatus()).isNull();
    }

    @Test
    void shouldReturnSensorWithStatusUnknownWhenNoExtraInformation() {
        // Setup mock response
        mockServer.expect(requestTo(BASE_URL + "/sensors/" + SENSOR_ID))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("x-auth-id", "CL019567d9-6e3b-738b-9b6d-32496110bd35=="))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(emptyInformationFor(SENSOR_ID)));

        // Act
        var result = sensorInformationClient.getSensorInformation(SENSOR_ID);

        // Assert
        assertThat(result.getId()).isEqualTo(SENSOR_ID);
        assertThat(result.getStatus()).isEqualTo("Unknown");
        assertThat(result.getConfiguration()).isNull();

    }
    
    @ParameterizedTest
    @EnumSource(value = HttpStatus.class, names = {"INTERNAL_SERVER_ERROR", "BAD_REQUEST", "FORBIDDEN", "GATEWAY_TIMEOUT"})
    void shouldReturnEmptySensorWhenRequestFails(HttpStatus status) {
        //Arrange
        mockServer.expect(requestTo(BASE_URL + "/sensors/" + SENSOR_ID))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("x-auth-id", "CL019567d9-6e3b-738b-9b6d-32496110bd35=="))
                .andRespond(withStatus(status));

        // Act
        var result = sensorInformationClient.getSensorInformation(SENSOR_ID);

        // Assert
        assertThat(result.getId()).isEqualTo(SENSOR_ID);
        assertThat(result.getFirmwareVersion()).isNull();
        assertThat(result.getConfiguration()).isNull();
        assertThat(result.getStatus()).isEqualTo("Unknown");
        assertThat(result.getConfiguration()).isNull();
    }

    private String sensorInformationFor(Long id) {
        return """
                {
                  "serial": %d,
                  "type": "TS50X",
                  "status_id": 1,
                  "current_configuration": "some_configuration.cfg",
                  "current_firmware": "59.1.12Rev4",
                  "created_at": "2022-03-31 11:26:08",
                  "updated_at": "2022-10-18 17:53:48",
                  "status_name": "Idle",
                  "next_task": null,
                  "task_count": 5,
                  "activity_status": "Online",
                  "task_queue": [124355, 44435322]
                }
                """.formatted(id);
    }

    private String emptyInformationFor(Long id) {
        return """
                {
                  "serial": %d,
                  "type": null,
                  "status_id": 0,
                  "current_configuration": null,
                  "current_firmware": null,
                  "created_at": null,
                  "updated_at": null,
                  "status_name": null,
                  "next_task": null,
                  "task_count": 0,
                  "activity_status": null,
                  "task_queue": null
                }
                """.formatted(id);
    }
}