package com.orange.moviebackend.common.exception;

public class FileNameLengthLimitExceededException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FileNameLengthLimitExceededException() {
    }

    public FileNameLengthLimitExceededException(String message) {
        super(message);
    }

}
