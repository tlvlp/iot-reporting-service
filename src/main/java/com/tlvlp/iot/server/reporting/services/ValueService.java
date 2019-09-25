package com.tlvlp.iot.server.reporting.services;

import com.tlvlp.iot.server.reporting.persistence.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.validation.Validation;
import javax.validation.ValidationException;
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

    public HashMap<Value, ResponseEntity<String>> saveIncomingValues(List<Value> values) {
        HashMap<Value, ResponseEntity<String>> results = new HashMap<>();
        for (Value value : values) {
            results.put(value, updateValueInDB(value));
        }
        return results;
    }

    private ResponseEntity<String> updateValueInDB(Value value) {
        try {
            var localValue = new Value(value);
            localValue
                    .setValueID(getNewValueID())
                    .setValueID(getNewValueID())
                    .setTime(LocalDateTime.now());
            var validationProblems = Validation.buildDefaultValidatorFactory().getValidator().validate(localValue);
            if(! validationProblems.isEmpty()) {
                throw new IllegalArgumentException(validationProblems.toString());
            };
            mongoTemplate.save(localValue);
            log.info("Value saved: {}", localValue);
            return new ResponseEntity<>("Saved", HttpStatus.ACCEPTED);
        } catch (IllegalArgumentException | ValidationException e) {
            log.error("Value cannot be saved: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private String getNewValueID() {
        return String.format("%s-VALUE-%s", LocalDate.now().toString(), UUID.randomUUID().toString());
    }

}
