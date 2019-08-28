package com.tlvlp.iot.server.reporting.services;

import com.tlvlp.iot.server.reporting.persistence.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

    public ValueService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Value> getFilteredValues(Value filter) {
        return mongoTemplate.find(getQueryFromExample(filter), Value.class);
    }

    private Query getQueryFromExample(Value filter) {
        Query query = new Query();
        if (filter.getValueID() != null) {
            query.addCriteria(Criteria.where("valueID").is(filter.getValueID()));
        }
        if (filter.getUnitID() != null) {
            query.addCriteria(Criteria.where("unitID").is(filter.getUnitID()));
        }
        if (filter.getModuleID() != null) {
            query.addCriteria(Criteria.where("moduleID").is(filter.getModuleID()));
        }
        if (filter.getValue() != null) {
            query.addCriteria(Criteria.where("value").is(filter.getValue()));
        }
        if (filter.getScope() != null) {
            query.addCriteria(Criteria.where("scope").is(filter.getScope()));
        }
        if (filter.getTimeFrom() != null) {
            query.addCriteria(Criteria.where("timeFrom").gte(filter.getTimeFrom()));
        }
        if (filter.getTimeTo() != null) {
            query.addCriteria(Criteria.where("timeTo").lte(filter.getTimeTo()));
        }
        return query;
    }

    public HashMap<Value, ResponseEntity<String>> saveIncomingValues(List<Value> values) {
        HashMap<Value, ResponseEntity<String>> results = new HashMap<>();
        for (Value value : values) {
            results.put(value, saveNewValue(value));
        }
        return results;
    }

    private ResponseEntity<String> saveNewValue(Value value) {
        try {
            checkValueValidity(value);
            value.setValueID(getNewValueID());
            LocalDateTime now = LocalDateTime.now();
            value.setTimeFrom(now);
            value.setTimeTo(now);
            value.setScope(Value.Scope.RAW);
            mongoTemplate.save(value);
            log.info("Value saved: {}", value);
            return new ResponseEntity<>("Saved", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException e) {
            log.error("Value cannot be saved: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private void checkValueValidity(Value value) throws IllegalArgumentException {
        if (!isValidString(value.getUnitID())) {
            throw new IllegalArgumentException("Value unitID must be a valid String!");
        } else if (!isValidString(value.getModuleID())) {
            throw new IllegalArgumentException("Value moduleID must be a valid String!");
        } else if (value.getValue() != null) {
            throw new IllegalArgumentException("Value must be a valid Double!");
        }
    }

    private Boolean isValidString(String str) {
        return str != null && !str.isEmpty();
    }

    private String getNewValueID() {
        return String.format("%s-%s-VALUE", LocalDate.now().toString(), UUID.randomUUID().toString());
    }

}
