package com.kkirok.server.global.common.dto;

import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.global.common.exception.PaginationErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public record PagingRequest(int page, int size) {

    public static final int MAX_SIZE = 50;

    public static PagingRequest of(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_SIZE) {
            throw new BadRequestException(PaginationErrorCode.INVALID_PAGING_REQUEST);
        }

        return new PagingRequest(page, size);
    }

    public Pageable toPageable() {
        return PageRequest.of(page, size);
    }

    public void validatePage(long totalElements, int totalPages) {
        if (totalElements == 0) {
            if (page > 0) {
                throw new BadRequestException(PaginationErrorCode.PAGE_OUT_OF_RANGE);
            }
            return;
        }

        if (page >= totalPages) {
            throw new BadRequestException(PaginationErrorCode.PAGE_OUT_OF_RANGE);
        }
    }
}
