package be.aboutcoding.esd.ex1;

import be.aboutcoding.esd.ex1.infrastructure.SensorValidationAPI;
import be.aboutcoding.esd.ex1.model.Sensor;
import be.aboutcoding.esd.ex1.process.SensorValidationProcess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SensorValidationAPI.class)
class SensorValidationAPITest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SensorValidationProcess validationProcess;

    @Test
    void validateSensors_shouldReturnValidationResults_whenFileUploaded() throws Exception {
        // Arrange
        String csvContent = "id,type\n323445678,TS50X\n323445680,TS50X";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sensors.csv",
                "text/csv",
                csvContent.getBytes()
        );

        List<Sensor> validatedSensors = Arrays.asList(
                new Sensor(323445678L, "60.1.12Rev1", "config123.cfg", "ready"),
                new Sensor(323445680L, "50.1.12Rev1", "config_invalid.cfg", "updating_firmware")
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
}