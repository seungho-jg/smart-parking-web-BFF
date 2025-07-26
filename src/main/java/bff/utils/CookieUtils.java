// CookieUtils.java
package bff.utils;

import java.util.Arrays;

public class CookieUtils {
    /**
     * 주어진 Cookie 헤더 문자열에서 특정 이름의 쿠키 값을 꺼내 반환합니다.
     * @param cookieHeader Cookie 헤더 전체 (예: "a=1; SESSIONID=xyz; b=2")
     * @param name 찾고자 하는 쿠키 이름 (예: "SESSIONID")
     * @return 해당 쿠키 값, 없으면 빈 문자열
     */
    public static String parse(String cookieHeader, String name) {
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            return "";
        }
        // 세미콜론(;)으로 구분된 각 쿠키 토큰 순회
        return Arrays.stream(cookieHeader.split(";"))
                .map(String::trim)
                .filter(token -> token.startsWith(name + "="))
                .map(token -> {
                    String[] parts = token.split("=", 2);
                    return parts.length > 1 ? parts[1] : "";
                })
                .findFirst()
                .orElse("");
    }
}
