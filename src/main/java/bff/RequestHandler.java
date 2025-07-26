
package bff;

import bff.handlers.Handler;

import java.io.*;
import java.net.Socket;
import bff.utils.HttpUtils;


public class RequestHandler {
    private final Socket client;
    private final Router router;

    public RequestHandler(Socket client, Router router) {
        this.client = client;
        this.router = router;
    }

    public void handle() throws IOException {
        /* 여기서 BufferedReader를 사용하는 이유
            InputStreamReader로 바이트 단위로 읽는 것 보다 8kb 정도의 버퍼를 가지고 한번에 읽어오는게
            시스템 콜 횟수를 줄여 부담이 적다.
        */
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

        // 요청 첫 줄 읽기 및 null 체크
        String requestLine = in.readLine(); // GET / HTTP/1.1
        System.out.println(requestLine);
        // 1) null 체크: 아무것도 없다면 처리할 게 없으니 바로 리턴
        if (requestLine == null) {
            return;
        }

        // method와 path 파싱 (split by space)
        String[] parts = requestLine.split(" ");
        String method = parts[0];
        String path = parts[1];
        //String httpVersion = parts[2];

        // 라우터에서 핸들러 가져오기
        Handler handler = router.resolve(path);

        if (handler != null) {
            handler.handle(in, out, method, path);
        } else {
            // 404 Not Found 응답
            HttpUtils.sendCustomResponse(out, "404 Notfound");
            out.flush();
        }
        // 클라이언트 소켓 닫기
        client.close();
    }
}
