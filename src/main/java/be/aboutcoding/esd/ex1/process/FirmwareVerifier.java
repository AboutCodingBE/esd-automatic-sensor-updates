package be.aboutcoding.esd.ex1.process;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirmwareVerifier {

    private static final String MINIMUM_FIRMWARE_VERSION = "59.1.12Rev4";
    private static final Pattern FIRMWARE_VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)Rev(\\d+)");

    /**
     * Verifies if a firmware version is up to date by comparing it with the minimum required version.
     * A firmware is outdated if it is below version 59.1.12Rev4
     *
     * @param firmwareVersion The firmware version to verify
     * @return true if the firmware is up to date, false otherwise
     */
    public boolean isUpToDate(String firmwareVersion) {
        if (firmwareVersion == null) {
            return false;
        }

        try {
            // Parse the components of the current and minimum firmware versions
            int[] currentVersionComponents = parseVersionComponents(firmwareVersion);
            int[] minimumVersionComponents = parseVersionComponents(MINIMUM_FIRMWARE_VERSION);

            // Compare version components in order of significance
            for (int i = 0; i < 4; i++) {
                if (currentVersionComponents[i] > minimumVersionComponents[i]) {
                    return true;
                } else if (currentVersionComponents[i] < minimumVersionComponents[i]) {
                    return false;
                }
                // If components are equal, continue to the next component
            }

            // If all components are equal, the version is up to date
            return true;

        } catch (IllegalArgumentException e) {
            // If the version format is invalid, consider it outdated
            return false;
        }
    }

    /**
     * Parses a firmware version string into its numeric components.
     *
     * @param version The firmware version string (e.g., "59.1.12Rev4")
     * @return An array of integers representing the version components [major, minor, patch, revision]
     * @throws IllegalArgumentException if the version string doesn't match the expected format
     */
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
}
