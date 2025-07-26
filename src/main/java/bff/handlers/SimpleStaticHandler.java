package bff.handlers;
import bff.utils.HttpUtils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SimpleStaticHandler implements Handler {
    private final String staticDir;

    public SimpleStaticHandler(String staticDir) {
        this.staticDir = staticDir;
    }

    @Override
    public void handle(BufferedReader in, BufferedWriter out, String method, String path) throws IOException {
        try {
            // 헤더 소비
            HttpUtils.parseHeaders(in);

            String content;
            String contentType;

            if (path.endsWith(".css")) {
                // CSS 파일 요청
                content = readFile(path);
                contentType = "text/css; charset=utf-8";
            } else if (path.endsWith(".js")) {
                // JavaScript 파일 요청
                content = readFile(path);
                contentType = "application/javascript; charset=utf-8";
            } else {
                // 그 외 모든 경우 -> index.html
                content = readFile("/index.html");
                contentType = "text/html; charset=utf-8";
            }

            // 응답 전송
            out.write("HTTP/1.1 200 OK\r\n");
            out.write("Content-Type: " + contentType + "\r\n");
            out.write("Content-Length: " + content.getBytes("UTF-8").length + "\r\n");
            out.write("\r\n");
            out.write(content);

        } catch (Exception e) {
            // 파일을 찾을 수 없거나 오류 발생
            String errorHtml = "<html><body><h1>404 Not Found</h1></body></html>";
            out.write("HTTP/1.1 404 Not Found\r\n");
            out.write("Content-Type: text/html; charset=utf-8\r\n");
            out.write("Content-Length: " + errorHtml.getBytes("UTF-8").length + "\r\n");
            out.write("\r\n");
            out.write(errorHtml);
        } finally {
            out.flush();
        }
    }

    private String readFile(String path) throws IOException {
        // 앞의 슬래시 제거
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        Path filePath = Paths.get(staticDir, path);
        return Files.readString(filePath);
    }
}