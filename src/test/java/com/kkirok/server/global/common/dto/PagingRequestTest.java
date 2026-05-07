package com.kkirok.server.global.common.dto;

import com.kkirok.server.global.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PagingRequestTest {

    @Test
    void of_shouldCreatePageable_whenPageAndSizeAreValid() {
        PagingRequest request = PagingRequest.of(0, 20);

        assertEquals(0, request.page());
        assertEquals(20, request.size());
        assertEquals(0, request.toPageable().getPageNumber());
        assertEquals(20, request.toPageable().getPageSize());
    }

    @Test
    void of_shouldThrowBadRequest_whenPageOrSizeIsInvalid() {
        assertThrows(BadRequestException.class, () -> PagingRequest.of(-1, 20));
        assertThrows(BadRequestException.class, () -> PagingRequest.of(0, 0));
        assertThrows(BadRequestException.class, () -> PagingRequest.of(0, PagingRequest.MAX_SIZE + 1));
    }

    @Test
    void validatePage_shouldThrowBadRequest_whenPageIsOutOfRange() {
        PagingRequest request = PagingRequest.of(1, 20);

        assertThrows(BadRequestException.class, () -> request.validatePage(0, 0));
        assertThrows(BadRequestException.class, () -> request.validatePage(10, 1));
    }
}
