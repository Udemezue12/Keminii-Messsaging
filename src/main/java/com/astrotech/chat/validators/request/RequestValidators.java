package com.astrotech.chat.validators.request;

import com.astrotech.chat.exceptions.AppException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component
public class RequestValidators {
    public String validateRequestParams(String sort, String sortValue1, String sortValue2) {
        if (!Set.of(sortValue1, sortValue2).contains(sort))
            sort = sortValue1;
        return sort;
    }

    public String validateRequestParam(String sort, String sortValue1) {
        if (!Objects.equals(sortValue1, sort))
            sort = sortValue1;
        return sort;
    }

    public void throwIfTrue(boolean condition, String message) {

        if (condition) {
            throw new AppException(message);
        }
    }
}
