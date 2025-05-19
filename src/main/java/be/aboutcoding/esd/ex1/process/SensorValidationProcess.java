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
            var ts50x = sensorInformationClient.getSensorInformation(sensorId);
            var validatedTs50x = validateSensor(ts50x);
            validatedSensors.add(validatedTs50x);
        }

        return validatedSensors;
    }

    private Sensor validateSensor(Sensor sensor) {
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
