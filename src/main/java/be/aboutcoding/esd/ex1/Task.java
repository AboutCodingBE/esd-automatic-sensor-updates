package be.aboutcoding.esd.ex1;

public record Task(String id, String type, String configurationFilename){

    public static Task createConfigUpdateTaskFor(String sensorId) {
        return new Task(sensorId, "configuration_update", "config123.cnf");
    }

    public static Task createFirmwareUpdateTaskFor(String sensorId) {
        return new Task(sensorId, "firmware_update", null);
    }
}
