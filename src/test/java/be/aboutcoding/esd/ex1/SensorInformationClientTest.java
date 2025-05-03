package be.aboutcoding.esd.ex1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        var sensorId = 323445678L;
        var expectedFirmwareVersion = "59.1.12Rev4";
        var expectedConfiguriation = "some_configuration.cfg";

        // Setup mock response
        mockServer.expect(requestTo(BASE_URL + "/sensors/" + sensorId))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("x-auth-id", "CL019567d9-6e3b-738b-9b6d-32496110bd35=="))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(sensorInformationFor(sensorId)));

        // Act
        Sensor sensor = sensorInformationClient.getSensorInformation(sensorId);

        // Assert
        mockServer.verify();
        assertThat(sensor).isNotNull();
        assertThat(sensor.getId()).isEqualTo(sensorId);
        assertThat(sensor.getFirmwareVersion()).isEqualTo(expectedFirmwareVersion);
        assertThat(sensor.getConfiguration()).isEqualTo(expectedConfiguriation);
        assertThat(sensor.getStatus()).isNull();
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
}