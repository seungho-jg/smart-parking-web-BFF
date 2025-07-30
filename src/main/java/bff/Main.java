// Main.java
package bff;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bff.handlers.*;
import bff.socket.RaspberryClient;

public class Main {
    static final int PORT = 8080;
    static final String STATIC_DIR = "src/main/resources/static";

    public static void main(String[] args) throws IOException {
        // 공용 인스턴스
        SessionManager sessionManager = new SessionManager();

        // raspberryPi tcp 연결
        RaspberryClient raspberryConnect = new RaspberryClient();

        try {
            raspberryConnect.raspberryConnect();
            System.out.println("라즈베리파이 연결 성공!");

        } catch (IOException e) {
            System.err.println("라즈베리파이 연결 실패: " + e.getMessage());
            System.out.println("기본 모드로 서버 시작 (라즈베리파이 없이 동작)");
        }


        // Router 초기화 & API 핸들러만 등록
        Router router = new Router();
        router.register("/api/auth/login", new ApiLoginHandler(sessionManager));
        //router.register("/api/register", new RegisterHandler(userDao));
        //router.register("/api/logout", new LogoutHandler(sessionManager));
        router.register("/api/parking-spaces", new ApiParkingSpacesHandler(sessionManager, raspberryConnect));

        // 정적 파일 핸들러
        SimpleStaticHandler staticHandler = new SimpleStaticHandler(STATIC_DIR);

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
                        new SimpleRequestHandler(clientSocket, router, staticHandler).handle();
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
