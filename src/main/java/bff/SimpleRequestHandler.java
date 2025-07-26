package bff;

import bff.handlers.Handler;
import bff.handlers.SimpleStaticHandler;
import java.io.*;
import java.net.Socket;

public class SimpleRequestHandler {
    private final Socket clientSocket;
    private final Router router;
    private final SimpleStaticHandler staticHandler;

    public SimpleRequestHandler(Socket clientSocket, Router router, SimpleStaticHandler staticHandler) {
        this.clientSocket = clientSocket;
        this.router = router;
        this.staticHandler = staticHandler;
    }

    public void handle() throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            String requestLine = in.readLine();
            if (requestLine == null) return;

            String[] parts = requestLine.split(" ");
            String method = parts[0];
            String path = parts[1];

            System.out.println(method + " " + path);

            // API 요청인지 확인
            if (path.startsWith("/api/")) {
                Handler handler = router.resolve(path);
                if (handler != null) {
                    handler.handle(in, out, method, path);
                } else {
                    // API 경로인데 핸들러가 없음
                    out.write("HTTP/1.1 404 Not Found\r\n\r\n404 API Not Found");
                    out.flush();
                }
            } else {
                // 정적 파일 또는 React 라우팅
                staticHandler.handle(in, out, method, path);
            }

        } finally {
            clientSocket.close();
        }
    }
}
