package com.tlvlp.iot.server.reporting.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Objects;

@Document(collection = "values")
public class Value {

    @Id
    private String valueID;
    private String unitID;
    private String moduleID;
    private Double value;
    private LocalDateTime time;


    public Value() {
    }

    public Value(Value copyValue) {
        this.valueID = copyValue.getValueID();
        this.unitID = copyValue.getUnitID();
        this.moduleID = copyValue.getModuleID();
        this.time = copyValue.getTime();
        this.value = copyValue.getValue();
    }

    @Override
    public String toString() {
        return "Value{" +
                "valueID='" + valueID + '\'' +
                ", unitID='" + unitID + '\'' +
                ", moduleID='" + moduleID + '\'' +
                ", time=" + time +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Value)) return false;
        Value that = (Value) o;
        return valueID.equals(that.valueID) &&
                unitID.equals(that.unitID) &&
                moduleID.equals(that.moduleID) &&
                time.equals(that.time) &&
                value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueID, unitID, moduleID, time, value);
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

    public String getModuleID() {
        return moduleID;
    }

    public Value setModuleID(String moduleID) {
        this.moduleID = moduleID;
        return this;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public Value setTime(LocalDateTime time) {
        this.time = time;
        return this;
    }

    public Double getValue() {
        return value;
    }

    public Value setValue(Double value) {
        this.value = value;
        return this;
    }

}