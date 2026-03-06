package com.kkirok.server.global.swagger;

import io.swagger.v3.oas.models.examples.Example;
import lombok.Builder;
import lombok.Getter;

/**
 * Swagger 예시 묶음 전달 객체.
 */
@Getter
@Builder
public class ExampleHolder {

    private String name;
    private int code;
    private String description;
    private Example holder;
}
