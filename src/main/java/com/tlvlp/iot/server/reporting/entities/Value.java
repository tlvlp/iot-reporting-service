package com.tlvlp.iot.server.reporting.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.Objects;

@Document(collection = "values")
public class Value {

    @Id
    private String valueID;
    @NotBlank
    private String unitID;
    @NotBlank
    private String moduleID;
    @NotNull
    private Double value;
    @PastOrPresent
    private LocalDateTime time;


    public Value() {
    }

    public Value(Value copyValue) {
        this.valueID = copyValue.getValueID();
        this.unitID = copyValue.getUnitID();
        this.moduleID = copyValue.getModuleID();
        this.time = copyValue.getTime() == null ? null : LocalDateTime.from(copyValue.getTime());
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
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return Objects.equals(valueID, value.valueID) &&
                Objects.equals(unitID, value.unitID) &&
                Objects.equals(moduleID, value.moduleID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueID, unitID, moduleID);
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