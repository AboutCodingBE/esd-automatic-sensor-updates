package be.aboutcoding.esd.ex1.process;

import be.aboutcoding.esd.ex1.model.Sensor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SensorValidationProcess {

    private final SensorInformationClient sensorInformationClient;
    private final TaskClient taskClient;

    public SensorValidationProcess(SensorInformationClient sensorInformationClient,
                                   TaskClient taskClient) {
        this.sensorInformationClient = sensorInformationClient;
        this.taskClient = taskClient;
    }

    public List<Sensor> validateSensors(List<Long> sensorIds) {
        List<Sensor> validatedSensors = new ArrayList<>();

        for (Long sensorId : sensorIds) {
            Sensor sensor = sensorInformationClient.getSensorInformation(sensorId);
            Sensor validatedSensor = validateSensor(sensor);
            validatedSensors.add(validatedSensor);
        }

        return validatedSensors;
    }

    private Sensor validateSensor(Sensor sensor) {
        // Check if firmware is missing
        if (sensor.getFirmwareVersion() == null) {
            sensor.setStatus("firmware_unknown");
            return sensor;
        }

        // Check if firmware needs to be updated
        if (!sensor.hasValidFirmware()) {
            return taskClient.scheduleFirmwareUpdate(sensor);
        }

        // Check if configuration is missing
        if (sensor.getConfiguration() == null) {
            return taskClient.scheduleConfigurationUpdate(sensor);
        }

        // Check if configuration needs to be updated
        if (!sensor.hasValidConfiguration()) {
            return taskClient.scheduleConfigurationUpdate(sensor);
        }

        sensor.setStatus("ready");
        return sensor;
    }
}
