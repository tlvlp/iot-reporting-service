package com.tlvlp.iot.server.reporting.services;

import com.tlvlp.iot.server.reporting.persistence.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.*;

@Service
public class ReportingService {

    private MongoTemplate mongoTemplate;

    public ReportingService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Map<ChronoUnit, Map<String, Double>> getAverages(String unitID, String moduleID, LocalDateTime timeFrom, LocalDateTime timeTo) {
        List<Value> rawValues = getRawValuesFromDb(unitID, moduleID, timeFrom, timeTo);
        Map<ChronoUnit, Map<String, Double>> averagesReport = new HashMap<>();
        // TODO add scope selector
        // TODO add raw values as well
        averagesReport.put(HOURS, getHourlyAverages(rawValues));
        averagesReport.put(DAYS, getDailyAverages(rawValues));
        averagesReport.put(MONTHS, getMonthlyAverages(rawValues));
        averagesReport.put(YEARS, getYearlyAverages(rawValues));
        return averagesReport;

    }


    private List<Value> getRawValuesFromDb(String unitID, String moduleID, LocalDateTime timeFrom, LocalDateTime timeTo) {
        Query query = new Query();
        query.addCriteria(Criteria.where("unitID").is(unitID));
        query.addCriteria(Criteria.where("moduleID").is(moduleID));
        query.addCriteria(Criteria.where("time").gte(timeFrom).lt(timeTo));
        query.with(new Sort(Sort.Direction.ASC, "time"));
        query.fields()
                .include("time")
                .include("value")
                .exclude("valueID");
        return mongoTemplate.find(query, Value.class);
    }

    private Map<String, Double> getHourlyAverages(List<Value> rawList) {
        Set<LocalDateTime> hourSet = rawList.stream()
                .map(value -> value.getTime().truncatedTo(HOURS))
                .collect(Collectors.toSet());
        Map<String, Double> hourlyAverages = new TreeMap<>();
        for (LocalDateTime date : hourSet) {
            OptionalDouble average = rawList.stream()
                    .filter(rawValue -> date.getYear() == rawValue.getTime().getYear())
                    .filter(rawValue -> date.getMonth() == rawValue.getTime().getMonth())
                    .filter(rawValue -> date.getDayOfMonth() == rawValue.getTime().getDayOfMonth())
                    .filter(rawValue -> date.getHour() == rawValue.getTime().getHour())
                    .mapToDouble(Value::getValue)
                    .average();
            if (average.isPresent()) {
                hourlyAverages.put(date.toString(), average.getAsDouble());
            }
        }
        return hourlyAverages;
    }

    private Map<String, Double> getDailyAverages(List<Value> rawList) {
        Set<LocalDate> daySet = rawList.stream()
                .map(value -> value.getTime().toLocalDate())
                .collect(Collectors.toSet());
        Map<String, Double> dailyAverages = new TreeMap<>();
        for (LocalDate date : daySet) {
            OptionalDouble average = rawList.stream()
                    .filter(rawValue -> date.getYear() == rawValue.getTime().getYear())
                    .filter(rawValue -> date.getMonth() == rawValue.getTime().getMonth())
                    .filter(rawValue -> date.getDayOfMonth() == rawValue.getTime().getDayOfMonth())
                    .mapToDouble(Value::getValue)
                    .average();
            if (average.isPresent()) {
                dailyAverages.put(date.toString(), average.getAsDouble());
            }
        }
        return dailyAverages;
    }

    private Map<String, Double> getMonthlyAverages(List<Value> rawList) {
        Set<YearMonth> monthSet = rawList.stream()
                .map(Value::getTime)
                .map(date -> YearMonth.of(date.getYear(), date.getMonth()))
                .collect(Collectors.toSet());
        Map<String, Double> monthlyAverages = new TreeMap<>();
        for (YearMonth date : monthSet) {
            OptionalDouble average = rawList.stream()
                    .filter(rawValue -> date.getYear() == rawValue.getTime().getYear())
                    .filter(rawValue -> date.getMonth() == rawValue.getTime().getMonth())
                    .mapToDouble(Value::getValue)
                    .average();
            if (average.isPresent()) {
                monthlyAverages.put(date.toString(), average.getAsDouble());
            }
        }
        return monthlyAverages;
    }

    private Map<String, Double> getYearlyAverages(List<Value> rawList) {
        Set<Year> yearSet = rawList.stream()
                .map(value -> Year.of(value.getTime().getYear()))
                .collect(Collectors.toSet());
        Map<String, Double> yearlyAverages = new TreeMap<>();
        for (Year date : yearSet) {
            OptionalDouble average = rawList.stream()
                    .filter(rawValue -> date.getValue() == rawValue.getTime().getYear())
                    .mapToDouble(Value::getValue)
                    .average();
            if (average.isPresent()) {
                yearlyAverages.put(date.toString(), average.getAsDouble());
            }
        }
        return yearlyAverages;
    }

}
