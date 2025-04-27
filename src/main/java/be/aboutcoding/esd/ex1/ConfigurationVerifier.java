package be.aboutcoding.esd.ex1;

import org.springframework.stereotype.Component;

/**
 * Verifies if a sensor configuration is valid.
 * According to business rules, a configuration is valid when it matches the filename: config123.cfg
 */
@Component
public class ConfigurationVerifier {

    private static final String VALID_CONFIGURATION_FILENAME = "config123.cfg";

    /**
     * Checks if a configuration filename is valid.
     *
     * @param configFileName The configuration filename to verify
     * @return true if the configuration is valid, false otherwise
     */
    public boolean isValid(String configFileName) {
        if (configFileName == null || configFileName.isEmpty()) {
            return false;
        }

        return VALID_CONFIGURATION_FILENAME.equals(configFileName);
    }
}
