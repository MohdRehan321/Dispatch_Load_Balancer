package com.dispatch_load_balancer.validation;

import com.dispatch_load_balancer.exception.DuplicateRequestException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Component
public class RequestIdValidator {

    public <T> void validateUniqueIds(
            List<T> items,
            Function<T, String> idExtractor,
            String entityName) {

        Set<String> ids = new HashSet<>();

        for (T item : items) {
            String id = idExtractor.apply(item).trim();

            if (!ids.add(id)) {
                throw new DuplicateRequestException(
                        "Duplicate " + entityName + " ID: " + id
                );
            }
        }
    }
}
