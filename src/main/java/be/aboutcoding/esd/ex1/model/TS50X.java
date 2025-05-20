package be.aboutcoding.esd.ex1.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TS50X {

    private Long id;
    private String firmwareVersion;
    private String configuration;
    private String status;

    private static final String MINIMUM_FIRMWARE_VERSION = "59.1.12Rev4";
    private static final String VALID_CONFIGURATION_FILENAME = "config123.cfg";
    private static final Pattern FIRMWARE_VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)Rev(\\d+)");

    public TS50X(Long id, String firmwareVersion, String configuration, String status) {
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

    public boolean hasValidFirmware() {
        if (firmwareVersion == null) {
            return false;
        }

        try {
            int[] currentVersionComponents = parseVersionComponents(firmwareVersion);
            int[] minimumVersionComponents = parseVersionComponents(MINIMUM_FIRMWARE_VERSION);

            for (int i = 0; i < 4; i++) {
                if (currentVersionComponents[i] > minimumVersionComponents[i]) {
                    return true;
                } else if (currentVersionComponents[i] < minimumVersionComponents[i]) {
                    return false;
                }
            }
            return true;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private int[] parseVersionComponents(String version) {
        Matcher matcher = FIRMWARE_VERSION_PATTERN.matcher(version);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid firmware version format: " + version);
        }

        int[] components = new int[4];
        for (int i = 0; i < 4; i++) {
            components[i] = Integer.parseInt(matcher.group(i + 1));
        }

        return components;
    }

    public boolean hasValidConfiguration() {
        if (configuration == null || configuration.isEmpty()) {
            return false;
        }

        return VALID_CONFIGURATION_FILENAME.equals(configuration);
    }
}