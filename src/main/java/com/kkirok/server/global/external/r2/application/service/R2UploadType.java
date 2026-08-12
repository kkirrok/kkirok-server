package com.kkirok.server.global.external.r2.application.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum R2UploadType {

    KKINIPOP_EMOJI("kkinipop/emoji"),
    KKINIPOP_POST("kkinipop/post"),
    PROFILE("profile"),
    MEAL("meal");

    private final String prefix;
}
