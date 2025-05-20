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
            TS50X TS50X = sensorInformationClient.getSensorInformation(sensorId);
            be.aboutcoding.esd.ex1.model.TS50X validatedSensor = validateSensor(TS50X);
            validatedSensors.add(validatedSensor);
        }

        return validatedSensors;
    }

    private TS50X validateSensor(TS50X TS50X) {
        // Check if firmware is missing
        if (TS50X.getFirmwareVersion() == null) {
            TS50X.setStatus("firmware_unknown");
            return TS50X;
        }

        // Check if firmware needs to be updated
        if (!TS50X.hasValidFirmware()) {
            return taskClient.scheduleFirmwareUpdate(TS50X);
        }

        // Check if configuration is missing
        if (TS50X.getConfiguration() == null) {
            return taskClient.scheduleConfigurationUpdate(TS50X);
        }

        // Check if configuration needs to be updated
        if (!TS50X.hasValidConfiguration()) {
            return taskClient.scheduleConfigurationUpdate(TS50X);
        }

        TS50X.setStatus("ready");
        return TS50X;
    }
}
