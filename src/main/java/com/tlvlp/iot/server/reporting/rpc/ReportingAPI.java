package com.tlvlp.iot.server.reporting.rpc;

import com.tlvlp.iot.server.reporting.persistence.Value;
import com.tlvlp.iot.server.reporting.services.ReportingService;
import com.tlvlp.iot.server.reporting.services.ValueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class ReportingAPI {

    private ValueService valueService;
    private ReportingService reportingService;

    public ReportingAPI(ValueService valueService, ReportingService reportingService) {
        this.valueService = valueService;
        this.reportingService = reportingService;
    }

    @GetMapping("${REPORTING_SERVICE_API_GET_FILTERED_VALUES}")
    public ResponseEntity getFilteredValues(@RequestBody Value value,
                                            @RequestParam Optional<Boolean> includeLowerBound,
                                            @RequestParam Optional<Boolean> includeUpperBound) {
        List<Value> filteredValues = reportingService.getFilteredValues(
                value,
                includeLowerBound.orElse(true),
                includeUpperBound.orElse(false));
        return new ResponseEntity<>(filteredValues, HttpStatus.OK);
    }

    @PostMapping("${REPORTING_SERVICE_API_POST_VALUES}")
    public ResponseEntity saveValues(@RequestBody List<Value> values) {
        return new ResponseEntity<>(valueService.saveIncomingValues(values), HttpStatus.MULTI_STATUS);

    }
}
