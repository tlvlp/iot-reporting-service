package com.tlvlp.iot.server.reporting.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Objects;

@Document
public class Value {

    @Id
    private String valueID;
    private String unitID;
    private String module;
    private String moduleID;
    private LocalDateTime timeFrom;
    private LocalDateTime timeTo;
    private Double value;
    private Scope scope;

    @Override
    public String toString() {
        return "ValueAverage{" +
                "valueID='" + valueID + '\'' +
                ", unitID='" + unitID + '\'' +
                ", module='" + module + '\'' +
                ", moduleID='" + moduleID + '\'' +
                ", timeFrom=" + timeFrom +
                ", timeTo=" + timeTo +
                ", value=" + value +
                ", scope=" + scope +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Value)) return false;
        Value that = (Value) o;
        return valueID.equals(that.valueID) &&
                unitID.equals(that.unitID) &&
                module.equals(that.module) &&
                moduleID.equals(that.moduleID) &&
                timeFrom.equals(that.timeFrom) &&
                timeTo.equals(that.timeTo) &&
                value.equals(that.value) &&
                scope == that.scope;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueID, unitID, module, moduleID, timeFrom, timeTo, value, scope);
    }

    public String getValueID() {
        return valueID;
    }

    public Value setValueID(String valueID) {
        this.valueID = valueID;
        return this;
    }

    public String getUnitID() {
        return unitID;
    }

    public Value setUnitID(String unitID) {
        this.unitID = unitID;
        return this;
    }

    public String getModule() {
        return module;
    }

    public Value setModule(String module) {
        this.module = module;
        return this;
    }

    public String getModuleID() {
        return moduleID;
    }

    public Value setModuleID(String moduleID) {
        this.moduleID = moduleID;
        return this;
    }

    public LocalDateTime getTimeFrom() {
        return timeFrom;
    }

    public Value setTimeFrom(LocalDateTime timeFrom) {
        this.timeFrom = timeFrom;
        return this;
    }

    public LocalDateTime getTimeTo() {
        return timeTo;
    }

    public Value setTimeTo(LocalDateTime timeTo) {
        this.timeTo = timeTo;
        return this;
    }

    public Double getValue() {
        return value;
    }

    public Value setValue(Double value) {
        this.value = value;
        return this;
    }

    public Scope getScope() {
        return scope;
    }

    public Value setScope(Scope scope) {
        this.scope = scope;
        return this;
    }

    public enum Scope {
        RAW, HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY
    }
}