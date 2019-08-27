package com.tlvlp.iot.server.reporting.services;

import com.tlvlp.iot.server.reporting.persistence.Value;
import com.tlvlp.iot.server.reporting.persistence.ValueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Service
public class ValueService {

    private static final Logger log = LoggerFactory.getLogger(ValueService.class);
    private ValueRepository repository;

    public ValueService(ValueRepository repository) {
        this.repository = repository;
    }

    public List<Value> getFilteredValues(Value valueExample) {
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is("Eric"));
        return repository.find(query, Value.class);
    }

    public void saveValues(List<Value> values) {
        values.forEach(this::saveNewValue);
    }

    private void saveNewValue(Value value) {
        checkValueValidity(value);
        value.setValueID(getNewValueID());
        LocalDateTime now = LocalDateTime.now();
        value.setTimeFrom(now);
        value.setTimeTo(now);
        value.setScope(Value.Scope.RAW);
        repository.save(value);
        log.info("Value saved: {}", value);
    }

    private void checkValueValidity(Value value) {
        if (!isValidString(value.getUnitID())) {
            throw new IllegalArgumentException("Value unitID must be a valid String!");
        } else if (!isValidString(value.getModule())) {
            throw new IllegalArgumentException("Value module must be a valid String!");
        } else if (!isValidString(value.getModuleID())) {
            throw new IllegalArgumentException("Value moduleID must be a valid String!");
        } else if (value.getValue() != null) {
            throw new IllegalArgumentException("Value must be a valid Double!");
        }
    }

    private Boolean isValidString(String str) {
        return str != null && !str.isEmpty();
    }

    private Boolean isValueValidDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NullPointerException | NumberFormatException e) {
            return false;
        }
    }

    private String getNewValueID() {
        return String.format("%s-%s-VALUE", LocalDate.now().toString(), UUID.randomUUID().toString());
    }

}
