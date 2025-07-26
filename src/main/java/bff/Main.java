// Main.java
package bff;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bff.handlers.*;

public class Main {
    static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        // 공용 인스턴스
        SessionManager sessionManager = new SessionManager();

        // Router 초기화 & 핸들러 등록
        Router router = new Router();
        router.register("/", new HomeHandler(sessionManager));

        // ServerSocket 생성
        ServerSocket serverSocket = null;
        ExecutorService executor = Executors.newFixedThreadPool(10);

        try{
            serverSocket = new ServerSocket(PORT); // 리스닝 소켓(수신 전용) 8080번 포트에 들어오는 연결 요청만 받음
            // 서버 시작 메시지 출력
            System.out.println("Server started at http://localhost:" + PORT);

            while (true) {
                // 클라이언트 요청을 기다림
                Socket clientSocket = serverSocket.accept(); //blocking 상태 -> 연결 수락: TCP 3-way handshake
                executor.submit(()->{
                    try {
                        new RequestHandler(clientSocket, router).handle(); // 이제부터 소켓을 통해 getInputStream()/getOutputStream()으로 데이터 송수신이 가능
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                });
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            if (serverSocket!=null) {
                serverSocket.close();
            }
        }
    }
}
