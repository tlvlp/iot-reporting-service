package com.tlvlp.iot.server.reporting.services;

import com.tlvlp.iot.server.reporting.persistence.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


@Service
public class ValueService {

    private static final Logger log = LoggerFactory.getLogger(ValueService.class);
    private MongoTemplate mongoTemplate;
    private ReportingService reportingService;

    public ValueService(MongoTemplate mongoTemplate, ReportingService reportingService) {
        this.mongoTemplate = mongoTemplate;
        this.reportingService = reportingService;
    }

    public HashMap<Value, ResponseEntity<String>> saveIncomingValues(List<Value> values) {
        HashMap<Value, ResponseEntity<String>> results = new HashMap<>();
        for (Value value : values) {
            results.put(value, updateValues(value));
        }
        return results;
    }

    private ResponseEntity<String> updateValues(Value value) {
        try {
            checkValueValidity(value);
            LocalDateTime now = LocalDateTime.now();
            value.setValueID(getNewValueID())
                    .setTimeFrom(now)
                    .setTimeTo(now)
                    .setScope(Value.Scope.RAW);
            mongoTemplate.save(value);
            log.info("Value saved: {}", value);
            updateRollingAverages(value);
            return new ResponseEntity<>("Saved", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            log.error("Value cannot be saved: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private void checkValueValidity(Value value) throws IllegalArgumentException {
        if (!isValidString(value.getUnitID())) {
            throw new IllegalArgumentException(String.format("unitID must be a valid String! %s", value));
        } else if (!isValidString(value.getModuleID())) {
            throw new IllegalArgumentException(String.format("moduleID must be a valid String! %s", value));
        } else if (value.getValue() == null) {
            throw new IllegalArgumentException(String.format("value must be a valid Double! %s", value));
        }
    }

    private void updateRollingAverages(Value value) {
        try {
            List<Value> averages = reportingService.updateRollingAverages(value);
            for (Value average : averages) {
                removeOutdatedAverages(average);
                average.setValueID(getNewValueID());
                mongoTemplate.save(average);
                log.info("Average saved: {}", average);
            }
        } catch (ReportingException e) {
            log.error("Cannot update rolling averages: {}", e.getMessage());
        }
    }

    private void removeOutdatedAverages(Value value) {
        Value filter = new Value(value).setValueID(null).setValue(null);
        List<Value> outdatedAverages = reportingService.getFilteredValues(filter, true, true);
        outdatedAverages.forEach(this::deleteValue);
    }

    private Boolean isValidString(String str) {
        return str != null && !str.isEmpty();
    }

    private String getNewValueID() {
        return String.format("%s-%s-VALUE", LocalDate.now().toString(), UUID.randomUUID().toString());
    }

    private void deleteValue(Value average) {
        mongoTemplate.remove(average);
        log.debug(String.format("Deleting value: %s", average));
    }

}
