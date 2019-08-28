package com.tlvlp.iot.server.reporting.rpc;

import com.tlvlp.iot.server.reporting.persistence.Value;
import com.tlvlp.iot.server.reporting.services.ValueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ValuesAPI {

    private ValueService valueService;

    public ValuesAPI(ValueService valueService) {
        this.valueService = valueService;
    }

    @GetMapping("${REPORTING_SERVICE_API_GET_FILTERED_VALUES}")
    public ResponseEntity getFilteredValues(Value value) {
        return new ResponseEntity<>(valueService.getFilteredValues(value), HttpStatus.OK);
    }

    @PostMapping("${REPORTING_SERVICE_API_POST_VALUES}")
    public ResponseEntity saveValues(@RequestBody List<Value> values) {
        return new ResponseEntity<>(valueService.saveIncomingValues(values), HttpStatus.MULTI_STATUS);

    }
}
