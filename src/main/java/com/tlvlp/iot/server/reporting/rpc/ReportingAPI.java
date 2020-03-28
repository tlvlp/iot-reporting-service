package com.tlvlp.iot.server.reporting.rpc;

import com.tlvlp.iot.server.reporting.entities.Average;
import com.tlvlp.iot.server.reporting.entities.Value;
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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@RestController
public class ReportingAPI {

    private ValueService valueService;
    private ReportingService reportingService;

    public ReportingAPI(ValueService valueService, ReportingService reportingService) {
        this.valueService = valueService;
        this.reportingService = reportingService;
    }

    @GetMapping("${tlvlp.iot.server.reporting_service.api.get_averages}")
    public List<Average> getAverages(@RequestParam @NotBlank String unitID,
                                      @RequestParam @NotBlank String moduleID,
                                      @RequestParam
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timeFrom,
                                      @RequestParam
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timeTo,
                                      @RequestParam @NotEmpty Set<ChronoUnit> requestedScopes)
    {
        return reportingService.getAverages(unitID, moduleID, timeFrom, timeTo, requestedScopes);

    }

    @PostMapping("${tlvlp.iot.server.reporting_service.api.post_values}")
    public ResponseEntity<HashMap<Value, ResponseEntity<String>>> saveValues(@RequestBody @NotEmpty List<Value> values) {
        return new ResponseEntity<>(valueService.saveIncomingValues(values), HttpStatus.MULTI_STATUS);
    }
}
