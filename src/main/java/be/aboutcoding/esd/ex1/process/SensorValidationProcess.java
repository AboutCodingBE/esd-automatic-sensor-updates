package be.aboutcoding.esd.ex1.process;

import be.aboutcoding.esd.ex1.model.TS50X;
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

    public List<TS50X> validateSensors(List<Long> sensorIds) {
        List<TS50X> validatedSensors = new ArrayList<>();

        for (Long sensorId : sensorIds) {
            var TS50X = sensorInformationClient.getSensorInformation(sensorId);
            var validatedSensor = validateSensor(TS50X);
            validatedSensors.add(validatedSensor);
        }

        return validatedSensors;
    }

    private TS50X validateSensor(TS50X sensor) {
        // Check if firmware is missing
        if (sensor.getFirmwareVersion() == null) {
            sensor.setStatus("firmware_unknown");
            return sensor;
        }

        if (!sensor.hasValidFirmware()) {
            return taskClient.scheduleFirmwareUpdate(sensor);
        }

        if (sensor.getConfiguration() == null) {
            return taskClient.scheduleConfigurationUpdate(sensor);
        }

        if (!sensor.hasValidConfiguration()) {
            return taskClient.scheduleConfigurationUpdate(sensor);
        }

        sensor.setStatus("ready");
        return sensor;
    }
}
