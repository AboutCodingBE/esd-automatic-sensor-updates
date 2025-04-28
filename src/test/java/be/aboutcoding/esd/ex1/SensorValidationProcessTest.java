package be.aboutcoding.esd.ex1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SensorValidationProcessTest {

    private static final String CSV_CONTENT = "id,type\n323445678,TS50X";
    private static final String VALID_CONFIGURATION = "config123.cfg";
    private static final String INVALID_CONFIGURATION = "invalidconfig.cfg";
    private static final String VALID_FIRMWARE_VERSION = "60.1.12Rev1";
    private static final String INVALID_FIRMWARE_VERSION = "49.1.0Rev3";

    @Mock
    private SensorInformationClient sensorInformationClient;

    @Mock
    private TaskClient taskClient;

    private SensorValidationProcess sensorValidationProcess;

    @BeforeEach
    void setUp() {
        sensorValidationProcess = new SensorValidationProcess(
                sensorInformationClient,
                taskClient
        );
    }

    @Test
    void shouldReturnReadyStatus_whenSensorHasValidFirmwareAndConfiguration() {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream(CSV_CONTENT.getBytes());

        var sensor = aSensorWith("60.1.12Rev1", VALID_CONFIGURATION);
        when(sensorInformationClient.getSensorInformation(sensor.getId()))
                .thenReturn(sensor);

        // Act
        var result = sensorValidationProcess.validateSensors(inputStream);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(sensor.getId());
        assertThat(result.get(0).getStatus()).isEqualTo("ready");

        verify(sensorInformationClient).getSensorInformation(sensor.getId());
        verifyNoInteractions(taskClient);
    }

    @Test
    void shouldReturnUpdatingFirmwareStatus_whenSensorHasOutdatedFirmware() {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream(CSV_CONTENT.getBytes());
        var sensor = aSensorWith(INVALID_FIRMWARE_VERSION, VALID_CONFIGURATION);

        when(sensorInformationClient.getSensorInformation(sensor.getId()))
                .thenReturn(sensor);

        sensor.setStatus("updating_firmware"); // I don't really like this... it is a bit bad practice
        when(taskClient.scheduleFirmwareUpdate(sensor.getId()))
                .thenReturn(sensor);

        // Act
        var result = sensorValidationProcess.validateSensors(inputStream);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(sensor.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(sensor.getStatus());

        verify(sensorInformationClient).getSensorInformation(sensor.getId());
        verify(taskClient).scheduleFirmwareUpdate(sensor.getId());
    }

    @Test
    void validateSensors_shouldReturnConfigurationUpdateStatus_whenSensorHasInvalidConfiguration() {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream(CSV_CONTENT.getBytes());

        var sensor = aSensorWith(VALID_FIRMWARE_VERSION, INVALID_CONFIGURATION);
        when(sensorInformationClient.getSensorInformation(sensor.getId())).thenReturn(sensor);

        sensor.setStatus("configuration_update"); //I don't really like this... it is a bit bad practice
        when(taskClient.scheduleConfigurationUpdate(sensor.getId())).thenReturn(sensor);

        // Act
        var result = sensorValidationProcess.validateSensors(inputStream);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(sensor.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(sensor.getStatus());

        verify(sensorInformationClient).getSensorInformation(sensor.getId());
        verify(taskClient).scheduleConfigurationUpdate(sensor.getId());
    }

    @Test
    void validateSensors_shouldPrioritizeFirmwareUpdate_whenBothFirmwareAndConfigurationNeedUpdates() {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream(CSV_CONTENT.getBytes());

        var sensor = aSensorWith(INVALID_FIRMWARE_VERSION, INVALID_CONFIGURATION);
        when(sensorInformationClient.getSensorInformation(sensor.getId())).thenReturn(sensor);

        sensor.setStatus("updating_firmware"); // again, don't love this, we might have to do something about it
        when(taskClient.scheduleFirmwareUpdate(sensor.getId())).thenReturn(sensor);

        // Act
        var result = sensorValidationProcess.validateSensors(inputStream);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(sensor.getId());
        assertThat(result.get(0).getStatus()).isEqualTo(sensor.getStatus());

        verify(sensorInformationClient).getSensorInformation(sensor.getId());
        verify(taskClient).scheduleFirmwareUpdate(sensor.getId());
        verify(taskClient, never()).scheduleConfigurationUpdate(anyString());
    }

    @Test
    void validateSensors_shouldHandleMissingFirmware() {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream(CSV_CONTENT.getBytes());

        var sensor = aSensorWith(null, VALID_CONFIGURATION);
        when(sensorInformationClient.getSensorInformation(sensor.getId())).thenReturn(sensor);

        // Act
        var result = sensorValidationProcess.validateSensors(inputStream);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(sensor.getId());
        assertThat(result.get(0).getStatus()).isEqualTo("firmware_unknown");

        verify(sensorInformationClient).getSensorInformation(sensor.getId());
        verifyNoInteractions(taskClient);
    }

    @Test
    void validateSensors_shouldHandleMissingConfiguration() {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream(CSV_CONTENT.getBytes());

        var sensor = aSensorWith(VALID_FIRMWARE_VERSION, null);
        when(sensorInformationClient.getSensorInformation(sensor.getId())).thenReturn(sensor);

        sensor.setStatus("configuration_update");
        when(taskClient.scheduleConfigurationUpdate(sensor.getId())).thenReturn(sensor);

        // Act
        var result = sensorValidationProcess.validateSensors(inputStream);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(sensor.getId());
        assertThat(result.get(0).getStatus()).isEqualTo("configuration_update");

        verify(sensorInformationClient).getSensorInformation(sensor.getId());
        verify(taskClient).scheduleConfigurationUpdate(sensor.getId());
    }

    @Test
    void validateSensors_shouldProcessMultipleSensors() {
        // Arrange
        var multisensorContent = "id,type\n323445678,TS50X\n323449876,TS50X";
        InputStream inputStream = new ByteArrayInputStream(multisensorContent.getBytes());

        var sensor1 = aSensorWith(VALID_FIRMWARE_VERSION, VALID_CONFIGURATION);
        when(sensorInformationClient.getSensorInformation(sensor1.getId())).thenReturn(sensor1);

        var sensor2 = aSensorWith(INVALID_FIRMWARE_VERSION, VALID_CONFIGURATION);
        sensor2.setId("323449876");
        when(sensorInformationClient.getSensorInformation(sensor2.getId())).thenReturn(sensor2);

        sensor2.setStatus("updating_firmware");
        when(taskClient.scheduleFirmwareUpdate(sensor2.getId())).thenReturn(sensor2);

        // Act
        var result = sensorValidationProcess.validateSensors(inputStream);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(sensor1.getId());
        assertThat(result.get(0).getStatus()).isEqualTo("ready");
        assertThat(result.get(1).getId()).isEqualTo(sensor2.getId());
        assertThat(result.get(1).getStatus()).isEqualTo(sensor2.getStatus());

        verify(sensorInformationClient).getSensorInformation(sensor1.getId());
        verify(sensorInformationClient).getSensorInformation(sensor2.getId());
    }

    private Sensor aSensorWith(String firmwareVersion, String configurationVersion) {
        return new Sensor("323445678",
                firmwareVersion,
                configurationVersion,
                null);
    }
}