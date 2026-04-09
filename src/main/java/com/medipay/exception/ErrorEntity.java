package com.medipay.exception;

public record ErrorEntity(
        int code,
        String message) {
}
