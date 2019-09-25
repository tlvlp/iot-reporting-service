package com.tlvlp.iot.server.reporting.services;

import com.tlvlp.iot.server.reporting.persistence.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ReportingService.class);
    private MongoTemplate mongoTemplate;

    public ReportingService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Map<ChronoUnit, TreeMap<String, Double>> getAverages(String unitID, String moduleID, LocalDateTime timeFrom,
                                                                LocalDateTime timeTo, Set<ChronoUnit> requestedScopes) {
        List<Value> rawValues = getRawValuesFromDB(unitID, moduleID, timeFrom, timeTo);
        Map<ChronoUnit, TreeMap<String, Double>> averagesReport = new HashMap<>();
        for (ChronoUnit scope : requestedScopes) {
            switch (scope) {
                case MINUTES:
                    averagesReport.put(MINUTES, getFormattedRawValues(rawValues));
                    break;
                case HOURS:
                    averagesReport.put(HOURS, getHourlyAverages(rawValues));
                    break;
                case DAYS:
                    averagesReport.put(DAYS, getDailyAverages(rawValues));
                    break;
                case MONTHS:
                    averagesReport.put(MONTHS, getMonthlyAverages(rawValues));
                    break;
                case YEARS:
                    averagesReport.put(YEARS, getYearlyAverages(rawValues));
                    break;
            }
        }
        log.info(String.format("Returning averages:" +
                        "{unitID=%s, moduleID=%s, timeFrom=%s, timeTo=%s, requestedScopes=%s, rawValueCount=%s}",
                unitID, moduleID, timeFrom, timeTo, requestedScopes, rawValues.size()));
        return averagesReport;
    }


    private List<Value> getRawValuesFromDB(String unitID, String moduleID, LocalDateTime timeFrom, LocalDateTime timeTo) {
        Query query = new Query();
        query.addCriteria(Criteria.where("unitID").is(unitID));
        query.addCriteria(Criteria.where("moduleID").is(moduleID));
        query.addCriteria(Criteria.where("time").gte(timeFrom).lt(timeTo));
        query.with(Sort.by(Sort.Direction.ASC, "time"));
        query.fields()
                .include("time")
                .include("value")
                .exclude("valueID");
        return mongoTemplate.find(query, Value.class);
    }

    private TreeMap<String, Double> getFormattedRawValues(List<Value> rawList) {
        return rawList.stream()
                .collect(Collectors
                        .toMap(v -> v.getTime().truncatedTo(MINUTES).toString(),
                                Value::getValue, (o1, o2) -> o1,
                                TreeMap::new));

    }

    private TreeMap<String, Double> getHourlyAverages(List<Value> rawList) {
        Set<LocalDateTime> hourSet = rawList.stream()
                .map(value -> value.getTime().truncatedTo(HOURS))
                .collect(Collectors.toSet());
        TreeMap<String, Double> hourlyAverages = new TreeMap<>();
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

    private TreeMap<String, Double> getDailyAverages(List<Value> rawList) {
        Set<LocalDate> daySet = rawList.stream()
                .map(value -> value.getTime().toLocalDate())
                .collect(Collectors.toSet());
        TreeMap<String, Double> dailyAverages = new TreeMap<>();
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

    private TreeMap<String, Double> getMonthlyAverages(List<Value> rawList) {
        Set<YearMonth> monthSet = rawList.stream()
                .map(Value::getTime)
                .map(date -> YearMonth.of(date.getYear(), date.getMonth()))
                .collect(Collectors.toSet());
        TreeMap<String, Double> monthlyAverages = new TreeMap<>();
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

    private TreeMap<String, Double> getYearlyAverages(List<Value> rawList) {
        Set<Year> yearSet = rawList.stream()
                .map(value -> Year.of(value.getTime().getYear()))
                .collect(Collectors.toSet());
        TreeMap<String, Double> yearlyAverages = new TreeMap<>();
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
