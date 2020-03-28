package com.tlvlp.iot.server.reporting.services;

import com.tlvlp.iot.server.reporting.entities.Average;
import com.tlvlp.iot.server.reporting.entities.Value;
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
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.*;

@Service
public class ReportingService {

    private static final Logger log = LoggerFactory.getLogger(ReportingService.class);
    private MongoTemplate mongoTemplate;

    public ReportingService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Average> getAverages(String unitID, String moduleID, LocalDateTime timeFrom, LocalDateTime timeTo, Set<ChronoUnit> requestedScopes) {
        List<Value> rawValues = getRawValuesFromDB(unitID, moduleID, timeFrom, timeTo);
        List<Average> averagesReport = new ArrayList<>();
        for (ChronoUnit scope : requestedScopes) {
            switch (scope) {
                case MINUTES:
                    averagesReport.addAll(getFormattedRawValues(rawValues));
                    break;
                case HOURS:
                    averagesReport.addAll(getHourlyAverages(rawValues));
                    break;
                case DAYS:
                    averagesReport.addAll(getDailyAverages(rawValues));
                    break;
                case MONTHS:
                    averagesReport.addAll(getMonthlyAverages(rawValues));
                    break;
                case YEARS:
                    averagesReport.addAll(getYearlyAverages(rawValues));
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

    private List<Average> getFormattedRawValues(List<Value> rawList) {
        return rawList.stream()
                .map(value -> new Average()
                        .setScope(MINUTES)
                        .setDate(value.getTime().truncatedTo(MINUTES).toString())
                        .setValue(value.getValue()))
                .collect(Collectors.toList());

    }

    private List<Average> getHourlyAverages(List<Value> rawList) {
        Set<LocalDateTime> hourSet = rawList.stream()
                .map(value -> value.getTime().truncatedTo(HOURS))
                .collect(Collectors.toSet());
        List<Average> hourlyAverages = new ArrayList<>();
        for (LocalDateTime date : hourSet) {
            OptionalDouble average = rawList.stream()
                    .filter(rawValue -> date.getYear() == rawValue.getTime().getYear())
                    .filter(rawValue -> date.getMonth() == rawValue.getTime().getMonth())
                    .filter(rawValue -> date.getDayOfMonth() == rawValue.getTime().getDayOfMonth())
                    .filter(rawValue -> date.getHour() == rawValue.getTime().getHour())
                    .mapToDouble(Value::getValue)
                    .average();
            if (average.isPresent()) {
                hourlyAverages.add(new Average().setScope(HOURS)
                        .setDate(date.toString())
                        .setValue(average.getAsDouble()));
            }
        }
        return hourlyAverages;
    }

    private List<Average> getDailyAverages(List<Value> rawList) {
        Set<LocalDate> daySet = rawList.stream()
                .map(value -> value.getTime().toLocalDate())
                .collect(Collectors.toSet());
        List<Average> dailyAverages = new ArrayList<>();
        for (LocalDate date : daySet) {
            OptionalDouble average = rawList.stream()
                    .filter(rawValue -> date.getYear() == rawValue.getTime().getYear())
                    .filter(rawValue -> date.getMonth() == rawValue.getTime().getMonth())
                    .filter(rawValue -> date.getDayOfMonth() == rawValue.getTime().getDayOfMonth())
                    .mapToDouble(Value::getValue)
                    .average();
            if (average.isPresent()) {
                dailyAverages.add(new Average().setScope(DAYS)
                        .setDate(date.toString())
                        .setValue(average.getAsDouble()));
            }
        }
        return dailyAverages;
    }

    private List<Average> getMonthlyAverages(List<Value> rawList) {
        Set<YearMonth> monthSet = rawList.stream()
                .map(Value::getTime)
                .map(date -> YearMonth.of(date.getYear(), date.getMonth()))
                .collect(Collectors.toSet());
        List<Average> monthlyAverages = new ArrayList<>();
        for (YearMonth date : monthSet) {
            OptionalDouble average = rawList.stream()
                    .filter(rawValue -> date.getYear() == rawValue.getTime().getYear())
                    .filter(rawValue -> date.getMonth() == rawValue.getTime().getMonth())
                    .mapToDouble(Value::getValue)
                    .average();
            if (average.isPresent()) {
                monthlyAverages.add(new Average().setScope(MONTHS)
                        .setDate(date.toString())
                        .setValue(average.getAsDouble()));
            }
        }
        return monthlyAverages;
    }

    private List<Average> getYearlyAverages(List<Value> rawList) {
        Set<Year> yearSet = rawList.stream()
                .map(value -> Year.of(value.getTime().getYear()))
                .collect(Collectors.toSet());
        List<Average> yearlyAverages = new ArrayList<>();
        for (Year date : yearSet) {
            OptionalDouble average = rawList.stream()
                    .filter(rawValue -> date.getValue() == rawValue.getTime().getYear())
                    .mapToDouble(Value::getValue)
                    .average();
            if (average.isPresent()) {
                yearlyAverages.add(new Average().setScope(YEARS)
                        .setDate(date.toString())
                        .setValue(average.getAsDouble()));
            }
        }
        return yearlyAverages;
    }

}
