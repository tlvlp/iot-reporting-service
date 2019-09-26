package com.tlvlp.iot.server.reporting.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlvlp.iot.server.reporting.persistence.Value;
import com.tlvlp.iot.server.reporting.services.ReportingService;
import com.tlvlp.iot.server.reporting.services.ValueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Reporting API Test")
class ReportingAPITest {


    private MockMvc mockMvc;

    @Mock
    private ValueService valueService;
    @Mock
    private ReportingService reportingService;

    @InjectMocks
    private ReportingAPI reportingAPI;

    @BeforeEach
    void beforeEach() throws JsonProcessingException {
        mockMvc = MockMvcBuilders.standaloneSetup(reportingAPI)
                .addPlaceholderValue("REPORTING_SERVICE_API_GET_AVERAGES", "/endpoint_averages")
                .addPlaceholderValue("REPORTING_SERVICE_API_POST_VALUES", "/endpoint_values")
                .build();
    }

    @Test
    @DisplayName("Get averages - Valid values")
    void getAveragesValid() throws Exception {
        // given
        var unitID = "unitID";
        var moduleID = "moduleID";
        var timeFrom = LocalDateTime.now();
        var timeTo = LocalDateTime.now();
        var requestedScopes = new HashSet<ChronoUnit>();
        requestedScopes.add(MINUTES);
        given(reportingService.getAverages(unitID, moduleID, timeFrom, timeTo, requestedScopes))
                .willReturn(Map.of(MINUTES, new TreeMap<>()));

        // when
        mockMvc.perform(get("/endpoint_averages")
                    .contentType("application/json")
                    .param("unitID", unitID)
                    .param("moduleID", moduleID)
                    .param("timeFrom",  timeFrom.toString())
                    .param("timeTo", timeTo.toString())
                    .param("requestedScopes", "MINUTES"))
                .andExpect(status().isOk());

        // then
        then(reportingService).should().getAverages(
                anyString(), anyString(), any(LocalDateTime.class), any(LocalDateTime.class), anySet());

    }

    @Test
    @DisplayName("Get averages - Missing unitID")
    void getAveragesMissingUnitID() throws Exception {
        mockMvc.perform(get("/endpoint_averages")
                    .contentType("application/json")
                    .param("moduleID", "moduleID")
                    .param("timeFrom",  LocalDateTime.now().toString())
                    .param("timeTo", LocalDateTime.now().toString())
                    .param("requestedScopes", "MINUTES"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get averages - Missing moduleID")
    void getAveragesMissingModuleID() throws Exception {
        mockMvc.perform(get("/endpoint_averages")
                    .contentType("application/json")
                    .param("unitID", "unitID")
                    .param("timeFrom",  LocalDateTime.now().toString())
                    .param("timeTo", LocalDateTime.now().toString())
                    .param("requestedScopes", "MINUTES"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get averages - Missing timeFrom")
    void getAveragesMissingTimeFrom() throws Exception {
        mockMvc.perform(get("/endpoint_averages")
                        .contentType("application/json")
                        .param("unitID", "unitID")
                        .param("moduleID", "moduleID")
                        .param("timeTo", LocalDateTime.now().toString())
                        .param("requestedScopes", "MINUTES"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get averages - Missing timeTo")
    void getAveragesMissingTimeTo() throws Exception {
        mockMvc.perform(get("/endpoint_averages")
                        .contentType("application/json")
                        .param("unitID", "unitID")
                        .param("moduleID", "moduleID")
                        .param("timeFrom",  LocalDateTime.now().toString())
//                        .param("timeTo", LocalDateTime.now().toString())
                        .param("requestedScopes", "MINUTES"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get averages - Missing requestedScopes")
    void getAveragesMissingRequestedScopes() throws Exception {
        mockMvc.perform(get("/endpoint_averages")
                        .contentType("application/json")
                        .param("unitID", "unitID")
                        .param("moduleID", "moduleID")
                        .param("timeFrom",  LocalDateTime.now().toString())
                        .param("timeTo", LocalDateTime.now().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Save reporting values - Valid list")
    void saveValuesValidList() throws Exception {
        var validList = List.of(new Value().setUnitID("1").setModuleID("1").setValue(1d));
        mockMvc.perform(post("/endpoint_values")
                .contentType("application/json")
                .content(new ObjectMapper().writeValueAsString(validList)))
                .andExpect(status().isMultiStatus());

        then(valueService).should().saveIncomingValues(validList);
    }
}