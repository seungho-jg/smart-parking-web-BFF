package bff.handlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public interface Handler {
    /**
     * 요청을 처리하고, HTTP 응답을 작성합니다.
     * @param in      클라이언트 요청 스트림
     * @param out     클라이언트 응답 스트림
     * @param method  HTTP 메서드(GET, POST 등)
     * @param path    요청 경로
     */
    void handle(BufferedReader in,
                BufferedWriter out,
                String method,
                String path) throws IOException;
}
