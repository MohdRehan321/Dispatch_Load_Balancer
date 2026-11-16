package com.dispatch_load_balancer.validation;

import com.dispatch_load_balancer.exception.DuplicateRequestException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RequestIdValidatorTest {

    private final RequestIdValidator validator = new RequestIdValidator();

    @Test
    void acceptsDistinctIds() {
        assertDoesNotThrow(() -> validator.validateUniqueIds(
                List.of("ORD-1", "ORD-2"), value -> value, "order"
        ));
    }

    @Test
    void rejectsDuplicateIdsIgnoringSurroundingWhitespace() {
        DuplicateRequestException exception = assertThrows(
                DuplicateRequestException.class,
                () -> validator.validateUniqueIds(
                        List.of("ORD-1", " ORD-1 "), value -> value, "order"
                )
        );

        assertEquals("Duplicate order ID: ORD-1", exception.getMessage());
    }
}
