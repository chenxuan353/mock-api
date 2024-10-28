package com.chenxuan353.mock.util;

import com.chenxuan353.mock.core.consts.MockRespError;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RespUtil {
    public static <T> Map<String, Object> standRespMap(String code, String msg, T data) {
        Map<String, Object> map = new HashMap<>(3);
        map.put("code", code);
        map.put("msg", msg);
        if (data != null) {
            map.put("data", data);
        }
        return map;
    }

    public static <T> ResponseEntity<?> success(T data) {
        return ResponseEntity.ok(standRespMap("0", "ok!", data));
    }

    public static <T> ResponseEntity<?> success() {
        return success(null);
    }

    private static <T> ResponseEntity<?> error(String code, String msg, T data) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(standRespMap(code, msg, data));
    }

    public static <T> ResponseEntity<?> error(MockRespError code, T data, String format, Object... arg) {
        return error(code.getCode(), MessageFormatter.format(format, arg).getMessage(), data);
    }

    public static <T> ResponseEntity<?> error(MockRespError code, T data, String msg) {
        return error(code.getCode(), msg, data);
    }

    public static <T> ResponseEntity<?> error(MockRespError code, T data) {
        return error(code.getCode(), code.getMsg(), data);
    }

    public static ResponseEntity<?> error(MockRespError code, String msg) {
        return error(code.getCode(), msg, null);
    }

    public static ResponseEntity<?> error(MockRespError code, String format, Object... arg) {
        return error(code.getCode(), MessageFormatter.format(format, arg).getMessage(), null);
    }

    public static ResponseEntity<?> error(MockRespError code) {
        return error(code.getCode(), code.getMsg(), null);
    }
}
