package com.orange.moviebackend.common.exception;

public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码，可以用于前端根据不同错误码做不同处理，可选。
     */
    private Integer code;

    /**
     * 错误消息，将直接展示给用户。
     */
    private String message;

    /**
     * 构造一个不带错误码的业务异常。
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.message = message;
    }

    /**
     * 构造一个带有错误码和消息的业务异常。
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public BusinessException setCode(Integer code) {
        this.code = code;
        return this;
    }

    public BusinessException setMessage(String message) {
        this.message = message;
        return this;
    }
}