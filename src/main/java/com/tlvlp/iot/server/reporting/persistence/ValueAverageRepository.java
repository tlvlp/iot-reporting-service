package com.tlvlp.iot.server.reporting.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface ValueAverageRepository
        extends MongoRepository<Value, String>, QueryByExampleExecutor<Value> {

}
