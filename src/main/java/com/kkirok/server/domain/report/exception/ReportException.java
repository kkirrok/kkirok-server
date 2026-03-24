package com.kkirok.server.domain.report.exception;

import com.kkirok.server.global.common.exception.KkirokException;

public class ReportException extends KkirokException {
    public ReportException(ReportErrorCode baseErrorCode) {
        super(baseErrorCode);
    }
}
