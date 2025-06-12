package com.orange.moviebackend.common.response;

import com.orange.moviebackend.common.constant.HttpStatus;
import java.util.HashMap;

public class R extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    // Key for status code
    private static final String CODE = "code";

    // Key for operation result message
    private static final String MESSAGE = "msg";

    // Key for returned data
    private static final String DATA = "data";

    /**
     * Empty response message result
     */
    public R() {
    }

    /**
     * Response message without data
     *
     * @param code Status code
     * @param msg  Response message
     */
    public R(int code, String msg) {
        super.put(CODE, code);
        super.put(MESSAGE, msg);
    }

    /**
     * Construct a response message
     *
     * @param code Status code
     * @param msg  Response message
     * @param data Response data
     */
    public R(int code, String msg, Object data) {
        super.put(CODE, code);
        super.put(MESSAGE, msg);
        if (data != null) {
            super.put(DATA, data);
        }
    }

    /* Return success message */
    public static R success() {
        return success("Operation successful");
    }

    public static R success(String msg) {
        return success(msg, null);
    }

    public static R success(Object data) {
        return success("Operation successful", data);
    }

    /**
     * Return success message
     *
     * @param msg  Response message
     * @param data Response data
     * @return Response message object
     */
    public static R success(String msg, Object data) {
        return new R(HttpStatus.SUCCESS, msg, data);
    }

    /* Return error message */
    public static R error() {
        return error("Operation failed");
    }

    public static R error(String msg) {
        return error(msg, null);
    }

    /**
     * Return an error message with a specified status code
     *
     * @param code Status code
     * @param msg  Response message
     * @return Response message object
     */
    public static R error(int code, String msg) {
        return new R(code, msg, null);
    }

    /**
     * Default error message (500)
     *
     * @param msg  Response message
     * @param data Response data
     * @return Response message object
     */
    public static R error(String msg, Object data) {
        return new R(HttpStatus.ERROR, msg, data);
    }
}
