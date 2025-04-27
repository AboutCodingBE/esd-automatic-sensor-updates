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
    void validateSensors_shouldReturnReadyStatus_whenSensorHasValidFirmwareAndConfiguration() {
        // Arrange
        String csvContent = "id,type\n323445678,TS50X";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        Sensor sensor = new Sensor("323445678", "60.1.12Rev1", "config123.cfg", null);
        when(sensorInformationClient.getSensorInformation("323445678")).thenReturn(sensor);

        // Act
        List<Sensor> result = sensorValidationProcess.validateSensors(inputStream);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("323445678");
        assertThat(result.get(0).getStatus()).isEqualTo("ready");

        verify(sensorInformationClient).getSensorInformation("323445678");
        verifyNoInteractions(taskClient);
    }

    @Test
    void validateSensors_shouldReturnUpdatingFirmwareStatus_whenSensorHasOutdatedFirmware() {
        // Arrange
        String csvContent = "id,type\n323445678,TS50X";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        Sensor sensor = new Sensor("323445678", "50.1.12Rev1", "config123.cfg", null);
        when(sensorInformationClient.getSensorInformation("323445678")).thenReturn(sensor);

        Sensor updatedSensor = new Sensor("323445678", "50.1.12Rev1", "config123.cfg", "updating_firmware");
        when(taskClient.scheduleFirmwareUpdate("323445678")).thenReturn(updatedSensor);

        // Act
        List<Sensor> result = sensorValidationProcess.validateSensors(inputStream);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("323445678");
        assertThat(result.get(0).getStatus()).isEqualTo("updating_firmware");

        verify(sensorInformationClient).getSensorInformation("323445678");
        verify(taskClient).scheduleFirmwareUpdate("323445678");
    }

    @Test
    void validateSensors_shouldReturnConfigurationUpdateStatus_whenSensorHasInvalidConfiguration() {
        // Arrange
        String csvContent = "id,type\n323445678,TS50X";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        Sensor sensor = new Sensor("323445678", "60.1.12Rev1", "config_invalid.cfg", null);
        when(sensorInformationClient.getSensorInformation("323445678")).thenReturn(sensor);

        Sensor updatedSensor = new Sensor("323445678", "60.1.12Rev1", "config_invalid.cfg", "configuration_update");
        when(taskClient.scheduleConfigurationUpdate("323445678")).thenReturn(updatedSensor);

        // Act
        List<Sensor> result = sensorValidationProcess.validateSensors(inputStream);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("323445678");
        assertThat(result.get(0).getStatus()).isEqualTo("configuration_update");

        verify(sensorInformationClient).getSensorInformation("323445678");
        verify(taskClient).scheduleConfigurationUpdate("323445678");
    }

    @Test
    void validateSensors_shouldPrioritizeFirmwareUpdate_whenBothFirmwareAndConfigurationNeedUpdates() {
        // Arrange
        String csvContent = "id,type\n323445678,TS50X";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        Sensor sensor = new Sensor("323445678", "50.1.12Rev1", "config_invalid.cfg", null);
        when(sensorInformationClient.getSensorInformation("323445678")).thenReturn(sensor);

        Sensor updatedSensor = new Sensor("323445678", "50.1.12Rev1", "config_invalid.cfg", "updating_firmware");
        when(taskClient.scheduleFirmwareUpdate("323445678")).thenReturn(updatedSensor);

        // Act
        List<Sensor> result = sensorValidationProcess.validateSensors(inputStream);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("323445678");
        assertThat(result.get(0).getStatus()).isEqualTo("updating_firmware");

        verify(sensorInformationClient).getSensorInformation("323445678");
        verify(taskClient).scheduleFirmwareUpdate("323445678");
        verify(taskClient, never()).scheduleConfigurationUpdate(anyString());
    }

    @Test
    void validateSensors_shouldHandleMissingFirmware() {
        // Arrange
        String csvContent = "id,type\n323445678,TS50X";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        Sensor sensor = new Sensor("323445678", null, "config123.cfg", null);
        when(sensorInformationClient.getSensorInformation("323445678")).thenReturn(sensor);

        // Act
        List<Sensor> result = sensorValidationProcess.validateSensors(inputStream);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("323445678");
        assertThat(result.get(0).getStatus()).isEqualTo("firmware_unknown");

        verify(sensorInformationClient).getSensorInformation("323445678");
        verifyNoInteractions(taskClient);
    }

    @Test
    void validateSensors_shouldHandleMissingConfiguration() {
        // Arrange
        String csvContent = "id,type\n323445678,TS50X";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        Sensor sensor = new Sensor("323445678", "60.1.12Rev1", null, null);
        when(sensorInformationClient.getSensorInformation("323445678")).thenReturn(sensor);

        Sensor updatedSensor = new Sensor("323445678", "60.1.12Rev1", null, "configuration_update");
        when(taskClient.scheduleConfigurationUpdate("323445678")).thenReturn(updatedSensor);

        // Act
        List<Sensor> result = sensorValidationProcess.validateSensors(inputStream);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("323445678");
        assertThat(result.get(0).getStatus()).isEqualTo("configuration_update");

        verify(sensorInformationClient).getSensorInformation("323445678");
        verify(taskClient).scheduleConfigurationUpdate("323445678");
    }

    @Test
    void validateSensors_shouldProcessMultipleSensors() {
        // Arrange
        String csvContent = "id,type\n323445678,TS50X\n323445680,TS50X";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        Sensor sensor1 = new Sensor("323445678", "60.1.12Rev1", "config123.cfg", null);
        when(sensorInformationClient.getSensorInformation("323445678")).thenReturn(sensor1);

        Sensor sensor2 = new Sensor("323445680", "50.1.12Rev1", "config_invalid.cfg", null);
        when(sensorInformationClient.getSensorInformation("323445680")).thenReturn(sensor2);

        Sensor updatedSensor = new Sensor("323445680", "50.1.12Rev1", "config_invalid.cfg", "updating_firmware");
        when(taskClient.scheduleFirmwareUpdate("323445680")).thenReturn(updatedSensor);

        // Act
        List<Sensor> result = sensorValidationProcess.validateSensors(inputStream);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("323445678");
        assertThat(result.get(0).getStatus()).isEqualTo("ready");
        assertThat(result.get(1).getId()).isEqualTo("323445680");
        assertThat(result.get(1).getStatus()).isEqualTo("updating_firmware");

        verify(sensorInformationClient).getSensorInformation("323445678");
        verify(sensorInformationClient).getSensorInformation("323445680");
    }
}