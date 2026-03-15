package com.kkirok.server.domain.member.application.service;

public interface EmailSender {

    void sendVerificationCode(String email, String code);
}
