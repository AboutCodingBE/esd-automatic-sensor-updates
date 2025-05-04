package be.aboutcoding.esd.ex1.model;

public class Sensor {
    private Long id;
    private String firmwareVersion;
    private String configuration;
    private String status;

    public Sensor(Long id, String firmwareVersion, String configuration, String status) {
        this.id = id;
        this.firmwareVersion = firmwareVersion;
        this.configuration = configuration;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}