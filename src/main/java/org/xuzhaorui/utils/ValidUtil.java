package org.xuzhaorui.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xuzhaorui.exception.pre.SocketValidationException;
import org.xuzhaorui.messageserialization.SocketMessageInterceptor;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;

public class ValidUtil {

    private final Validator validator;
    private final Logger log = LoggerFactory.getLogger(ValidUtil.class);

    public ValidUtil(Validator validator) {
        this.validator = validator;
    }



    public <T> void validateAndProcessResponse(T deserializeData) {
        Set<ConstraintViolation<T>> constraintViolations = validateAndProcess(deserializeData);
        if (!constraintViolations.isEmpty()) {
            Map<String, String> errors = extractValidationErrors(constraintViolations);
            throw new SocketValidationException(errors.toString()); // 验证失败
        }



    }
    @SafeVarargs
    private final <T> Set<ConstraintViolation<T>> validateAndProcess(T... params) {
        Set<ConstraintViolation<T>> violations = new HashSet<>();
        for (T param : params) {
            violations = validator.validate(param);
            if (!violations.isEmpty()) {
                log.error("参数校验失败: {}", violations);
            }
        }
        return violations;
    }
    private <T> Map<String, String> extractValidationErrors(Set<ConstraintViolation<T>> violations) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : violations) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return errors;
    }
}
