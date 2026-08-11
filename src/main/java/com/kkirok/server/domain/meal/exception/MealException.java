package com.kkirok.server.domain.meal.exception;

import com.kkirok.server.global.common.exception.KkirokException;

public class MealException extends KkirokException {
    public MealException(MealErrorCode baseErrorCode) {
        super(baseErrorCode);
    }

    public MealException(MealErrorCode baseErrorCode, String additionalMessage) {
        super(baseErrorCode, additionalMessage);
    }
}
