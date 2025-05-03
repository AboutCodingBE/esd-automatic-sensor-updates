package be.aboutcoding.esd.ex1;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SensorValidationProcess {

    private final IdParser idParser;
    private final SensorInformationClient sensorInformationClient;
    private final FirmwareVerifier firmwareVerifier;
    private final ConfigurationVerifier configurationVerifier;
    private final TaskClient taskClient;

    public SensorValidationProcess(SensorInformationClient sensorInformationClient,
                                   TaskClient taskClient) {
        this.idParser = new IdParser();
        this.sensorInformationClient = sensorInformationClient;
        this.firmwareVerifier = new FirmwareVerifier();
        this.configurationVerifier = new ConfigurationVerifier();
        this.taskClient = taskClient;
    }

    public List<Sensor> validateSensors(InputStream csvInputStream) {
        List<Long> sensorIds = idParser.parse(csvInputStream);
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
        if (!firmwareVerifier.isUpToDate(sensor.getFirmwareVersion())) {
            return taskClient.scheduleFirmwareUpdate(sensor);
        }

        // Check if configuration is missing
        if (sensor.getConfiguration() == null) {
            return taskClient.scheduleConfigurationUpdate(sensor);
        }

        // Check if configuration needs to be updated
        if (!configurationVerifier.isValid(sensor.getConfiguration())) {
            return taskClient.scheduleConfigurationUpdate(sensor);
        }

        // If everything is valid, mark as ready
        sensor.setStatus("ready");
        return sensor;
    }
}
