package com.intellibank.mcp.banking;

public class BankingApiException extends RuntimeException {
    private final int status;

    public BankingApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public BankingApiException(String message, Throwable cause) {
        super(message, cause);
        this.status = 502;
    }

    public int getStatus() {
        return status;
    }
}
