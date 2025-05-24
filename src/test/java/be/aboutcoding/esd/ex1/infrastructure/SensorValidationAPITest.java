package be.aboutcoding.esd.ex1.infrastructure;

import be.aboutcoding.esd.ex1.infrastructure.SensorValidationAPI;
import be.aboutcoding.esd.ex1.model.TS50X;
import be.aboutcoding.esd.ex1.process.SensorValidationProcess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SensorValidationAPI.class)
class SensorValidationAPITest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SensorValidationProcess validationProcess;

    @ParameterizedTest
    @MethodSource("csvTestData")
    void validateSensors_shouldReturnValidationResults_whenFileUploaded(String csvContent) throws Exception {
        // Arrange
        var file = aMockFile(csvContent);

        var validatedSensors = Arrays.asList(
                new TS50X(323445678L, "60.1.12Rev1", "config123.cfg", "ready"),
                new TS50X(323445680L, "50.1.12Rev1", "config_invalid.cfg", "updating_firmware")
        );

        when(validationProcess.validateSensors(any())).thenReturn(validatedSensors);

        // Act & Assert
        mockMvc.perform(multipart("/api/sensors/validate").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("323445678"))
                .andExpect(jsonPath("$[0].status").value("ready"))
                .andExpect(jsonPath("$[1].id").value("323445680"))
                .andExpect(jsonPath("$[1].status").value("updating_firmware"));
    }

    static Stream<Arguments> csvTestData() {
        return Stream.of(arguments("id,type\n323445678,TS50X\n323445680,TS50X"),
                arguments("type, status, id\nTS50X, ready, 323445678\nTS50X, invalid, 323445680"));
    }

    @Test
    void shouldIgnoreAnIdThatisNotANumber() throws Exception {
        // Arrange
        var csvContent = "id, type\n12345678, TS50X\nhello, TS50X";
        var file = aMockFile(csvContent);

        // Act & Assert
        mockMvc.perform(multipart("/api/sensors/validate").file(file))
                .andExpect(status().isOk());

        verify(validationProcess).validateSensors(List.of(12345678L));
    }

    private MockMultipartFile aMockFile(String content) {
        return new MockMultipartFile(
                "file",
                "sensors.csv",
                "text/csv",
                content.getBytes()
        );
    }
}