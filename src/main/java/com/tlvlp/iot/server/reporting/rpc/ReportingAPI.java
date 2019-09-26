package com.tlvlp.iot.server.reporting.rpc;

import com.tlvlp.iot.server.reporting.persistence.Value;
import com.tlvlp.iot.server.reporting.services.ReportingService;
import com.tlvlp.iot.server.reporting.services.ValueService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@RestController
public class ReportingAPI {

    private ValueService valueService;
    private ReportingService reportingService;

    public ReportingAPI(ValueService valueService, ReportingService reportingService) {
        this.valueService = valueService;
        this.reportingService = reportingService;
    }

    @GetMapping("${REPORTING_SERVICE_API_GET_AVERAGES}")
    public ResponseEntity getAverages(@RequestParam @NotBlank String unitID,
                                      @RequestParam @NotBlank String moduleID,
                                      @RequestParam
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timeFrom,
                                      @RequestParam
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timeTo,
                                      @RequestParam @NotEmpty Set<ChronoUnit> requestedScopes)
    {
        Map<ChronoUnit, TreeMap<String, Double>> filteredValues =
                reportingService.getAverages(unitID, moduleID, timeFrom, timeTo, requestedScopes);
        return new ResponseEntity<>(filteredValues, HttpStatus.OK);
    }

    @PostMapping("${REPORTING_SERVICE_API_POST_VALUES}")
    public ResponseEntity saveValues(@RequestBody @NotEmpty List<Value> values) {
        return new ResponseEntity<>(valueService.saveIncomingValues(values), HttpStatus.MULTI_STATUS);
    }
}
