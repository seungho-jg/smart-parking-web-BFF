package bff.handlers;


import bff.SessionInfo;
import bff.SessionManager;
import bff.utils.CookieUtils;
import bff.utils.HttpUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

public class HomeHandler implements Handler{
    private final SessionManager sessionManager;

    public HomeHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    private static final String HOME_HTML = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <title>MiniWAS HOME</title>
              <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/water.css@2/out/light.css" />
            </head>
            <body>
              <main style="max-width: 300px; margin: 100px auto; text-align: center;">
              <h1>로그인이 필요합니다.</h1>
              <a href="/login" class="button" style="padding: 0.5rem 1rem;">로그인 하러 가기</a>
              </body>
            </html>
            """;

    @Override
    public void handle(BufferedReader in,
                       BufferedWriter out,
                       String method,
                       String path) throws IOException {
        try {
            if (!method.equals("GET")) {
                HttpUtils.sendMethodNotAllowed(out);
                return;
            }
            // 헤더 파싱
            Map<String, String> headers = HttpUtils.parseHeaders(in);
            String cookieHeader = headers.getOrDefault("cookie", "");
            // 세션ID 추출
            String sessionId = CookieUtils.parse(cookieHeader, "SESSIONID");
            SessionInfo info = sessionManager.getOne(sessionId);
            if (info != null) {
                String username = info.getNickname();
                String html = """
                    <!DOCTYPE html>
                    <html lang="ko">
                    <head>
                      <meta charset="UTF-8">
                      <title>MiniWAS Home</title>
                      <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/water.css@2/out/light.css" />
                    </head>
                    <body>
                      <main style="max-width:300px; margin:100px auto; text-align:center;">
                        <h2><b>안녕하세요!</b></h2>
                        <h1>%s님</h1>
                        <p><a href="/logout">로그아웃</a></p>
                      </main>
                    </body>
                    </html>
                    """.formatted(username);
                HttpUtils.sendHtmlResponse(out, html);
            } else {
                HttpUtils.sendHtmlResponse(out, HOME_HTML);
            }

        } finally {
            out.flush();
        }
    }
}
