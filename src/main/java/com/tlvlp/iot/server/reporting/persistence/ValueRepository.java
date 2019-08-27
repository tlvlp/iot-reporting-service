package com.tlvlp.iot.server.reporting.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;

public interface ValueRepository
        extends MongoRepository<Value, String>, QueryByExampleExecutor<Value> {

//    private String valueID;
//    private String unitID;
//    private String module;
//    private String moduleID;
//    private LocalDateTime timeFrom;
//    private LocalDateTime timeTo;
//    private Double value;
//    private Scope scope;

    List<Value> findBy();

}
