package com.tlvlp.iot.server.reporting.services;

import com.tlvlp.iot.server.reporting.persistence.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("Reporting service tests")
class ReportingServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ReportingService reportingService;

    private String unitID;
    private String moduleID;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Value> dbValues;

    @BeforeEach
    private void generateTestValues() {
        unitID = "unit";
        moduleID = "module";
        startDate = LocalDateTime.of(2019, 12, 31, 0, 0, 0);
        endDate = startDate.plusDays(1);
        dbValues = IntStream.rangeClosed(1, 10)
                .mapToObj(i ->
                        new Value()
                                .setModuleID(moduleID)
                                .setUnitID(unitID)
                                .setValueID(Integer.valueOf(i).toString())
                                .setValue(2d)
                                .setTime(startDate.plusMinutes(i * 10)))
                .collect(Collectors.toList());
    }

    @Test
    @DisplayName("Report output tests")
    void getAveragesTest() {
        // given
        given(mongoTemplate.find(any(Query.class), eq(Value.class))).willReturn(dbValues);

        // when
        Set<ChronoUnit> scopes = Set.of(ChronoUnit.MINUTES);
        Map<ChronoUnit, TreeMap<String, Double>> report =
                reportingService.getAverages(unitID, moduleID, startDate, endDate, scopes);

        // then
        then(mongoTemplate).should().find(any(Query.class), eq(Value.class));

        assertNotNull(report);
        assertArrayEquals(
                scopes.toArray(),
                report.keySet().toArray(),
                "Report contains the correct scopes"
        );

        for (TreeMap<String, Double> currentScope : report.values()) {
            assertNotNull(currentScope, "Each scope average should not be null");
        }
    }
}