package be.aboutcoding.esd.ex1.infrastructure;

import be.aboutcoding.esd.ex1.model.TS50X;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SensorInformationResponse(@JsonProperty("serial") Long serial,
                                                @JsonProperty("current_firmware") String currentFirmware,
                                                @JsonProperty("current_configuration") String currentConfiguration) {
    public TS50X toSensor() {
        return new TS50X(
                serial,
                currentFirmware(),
                currentConfiguration(),
                null // Status will be determined by the validation process
        );
    }
}
