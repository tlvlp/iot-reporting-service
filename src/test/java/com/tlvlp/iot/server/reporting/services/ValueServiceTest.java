package com.tlvlp.iot.server.reporting.services;

import com.tlvlp.iot.server.reporting.persistence.Value;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("Value service tests")
class ValueServiceTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ValueService valueService;

    @Test
    @DisplayName("Save correct value and collect ACCEPTED status")
    void saveIncomingValues_Correct() {
        // given
        Value baseValue = new Value().setUnitID("unit").setModuleID("module").setValue(1d);
        given(mongoTemplate.save(any(Value.class)))
                .willReturn(baseValue);

        // when
        List<Value> values = Collections.singletonList(baseValue);
        HashMap<Value, ResponseEntity<String>> results = valueService.saveIncomingValues(values);

        // then
        then(mongoTemplate).should().save(any(Value.class));
        assertNotNull(results);
        assertNotNull(results.get(baseValue));
        assertEquals(results.size(), values.size());
        assertEquals(results.get(baseValue).getStatusCode(), HttpStatus.ACCEPTED);
        //todo check generated values (valueID, date) - ArgumentCaptor?
    }

    @Test
    @DisplayName("Try to save incorrect values and collect BAD_REQUEST status for each")
    void saveIncomingValues_Errors() {
        // when
        Value baseValue = new Value().setUnitID("unit").setModuleID("module").setValue(1d);
        List<Value> values = Arrays.asList(
                new Value(baseValue).setUnitID(null),
                new Value(baseValue).setModuleID(null),
                new Value(baseValue).setValue(null)
        );
        HashMap<Value, ResponseEntity<String>> results = valueService.saveIncomingValues(values);

        // then
        then(mongoTemplate).shouldHaveZeroInteractions();
        assertNotNull(results);
        assertEquals(results.size(), values.size());
        for (ResponseEntity<String> status : results.values()) {
            assertNotNull(status);
            assertEquals(status.getStatusCode(), HttpStatus.BAD_REQUEST);
        }
    }
}