package com.orange.moviebackend.common.exception;

public class FileSizeLimitExceededException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FileSizeLimitExceededException() {
    }

    public FileSizeLimitExceededException(String message) {
        super(message);
    }

}
