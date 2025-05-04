package be.aboutcoding.esd.ex1.infrastructure;

public record Task(Long id, String type, String configurationFilename){

    public static Task createConfigUpdateTaskFor(Long sensorId) {
        return new Task(sensorId, "configuration_update", "config123.cnf");
    }

    public static Task createFirmwareUpdateTaskFor(Long sensorId) {
        return new Task(sensorId, "firmware_update", null);
    }
}
