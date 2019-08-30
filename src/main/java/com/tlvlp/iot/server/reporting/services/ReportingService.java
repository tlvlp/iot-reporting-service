package com.tlvlp.iot.server.reporting.services;

import com.tlvlp.iot.server.reporting.persistence.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;

@Service
public class ReportingService {

    private MongoTemplate mongoTemplate;

    public ReportingService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Value> getFilteredValues(
            Value filter, Boolean includeLowerBound, Boolean includeUpperBound) {
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
            Criteria criteria = Criteria.where("timeFrom");
            query.addCriteria(includeLowerBound ? criteria.gte(filter.getTimeFrom()) : criteria.gt(filter.getTimeFrom()));
        }
        if (filter.getTimeTo() != null) {
            Criteria criteria = Criteria.where("timeTo");
            query.addCriteria(includeUpperBound ? criteria.lte(filter.getTimeTo()) : criteria.lt(filter.getTimeTo()));
        }
        System.out.println("@@@@@@@@@@ query " + query.toString());
        return mongoTemplate.find(query, Value.class);
    }

    List<Value> updateRollingAverages(Value value) throws ReportingException {
        if (value.getScope() == Value.Scope.YEARLY) {
            return Arrays.asList(value);
        } else {
            System.out.println("@@@@@@@@@@ value " + value); //TODO: REMOVE ALL DEUBG
            List<Value> scopeValues = getValuesInNextScope(value);
            System.out.println("@@@@@@@@@@ scopeValues " + scopeValues);
            Value average = getAverageInScope(scopeValues);
            System.out.println("@@@@@@@@@@ average " + average);
            List<Value> averages = updateRollingAverages(average);
            System.out.println("@@@@@@@@@@ averages " + averages);
            averages.add(average);
            return averages;
        }
    }

    private List<Value> getValuesInNextScope(Value value) throws ReportingException {
        Value.Scope previousScopeLevel = value.getScope();
        System.out.println("@@@@@@@@@@ previousScopeLevel " + previousScopeLevel);
        Value.Scope targetScope;
        ChronoUnit chronoUnit;
        switch (previousScopeLevel) {
            case RAW:
                targetScope = Value.Scope.HOURLY;
                chronoUnit = ChronoUnit.HOURS;
                break;
            case HOURLY:
                targetScope = Value.Scope.DAILY;
                chronoUnit = ChronoUnit.DAYS;
                break;
            case DAILY:
                targetScope = Value.Scope.WEEKLY;
                chronoUnit = ChronoUnit.WEEKS;
                break;
            case WEEKLY:
                targetScope = Value.Scope.MONTHLY;
                chronoUnit = ChronoUnit.MONTHS;
                break;
            case MONTHLY:
                targetScope = Value.Scope.YEARLY;
                chronoUnit = ChronoUnit.YEARS;
                break;
            default:
                throw new ReportingException(String.format("Unhandled scope found: %s", previousScopeLevel));
        }
        Value filter = new Value(value)
                .setValueID(null)
                .setValue(null)
                .setScope(targetScope)
                .setTimeFrom(value.getTimeFrom().truncatedTo(chronoUnit));
        System.out.println("@@@@@@@@@@ filter " + filter);
        return getFilteredValues(filter, true, true);
    }

    private Value getAverageInScope(List<Value> scopeValues) throws ReportingException {
        OptionalDouble average = scopeValues.stream().mapToDouble(Value::getValue).average();
        if (average.isPresent()) {
            LocalDateTime timeFrom = scopeValues.stream()
                    .map(Value::getTimeFrom)
                    .min(LocalDateTime::compareTo).get();
            LocalDateTime timeTo = scopeValues.stream()
                    .map(Value::getTimeTo)
                    .max(LocalDateTime::compareTo).get();
            Value sampleValue = scopeValues.get(0);
            return new Value()
                    .setValue(average.getAsDouble())
                    .setTimeFrom(timeFrom)
                    .setTimeTo(timeTo)
                    .setScope(sampleValue.getScope())
                    .setModuleID(sampleValue.getModuleID())
                    .setUnitID(sampleValue.getUnitID());
        } else {
            throw new ReportingException(String.format("Cannot calculate average from values: %s", scopeValues));
        }
    }

}
