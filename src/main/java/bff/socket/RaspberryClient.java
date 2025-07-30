package bff.socket;

import java.io.*;
import java.net.Socket;

public class RaspberryClient {
    private Socket socket;
    private BufferedWriter out;
    private BufferedReader in;
    private boolean connected = false;

    public void raspberryConnect() throws IOException {
        try {
            socket = new Socket("192.168.0.51", 8888);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            System.out.println("라즈베리파이 연결 성공: " + socket);
        } catch (IOException e) {
            connected = false;
            System.out.println("라즈베리파이 연결 실패: " + e.getMessage());
            throw e;
        }
    }

    public String getParkingSpaceData() throws IOException {
        if (!connected) {
            throw new IOException("라즈베리파이에 연결되지 않음");
        }

        try {
            // 개행 문자 추가!
            out.write("GET_ALL\n");
            out.flush();
            System.out.println("GET_ALL 명령 전송완료");

            // 응답 읽기
            String response = in.readLine();
            System.out.println("받은 응답: " + response);

            // "24:000000000000000000000000" 형식에서 상태 부분만 추출
            if (response != null && response.startsWith("24:")) {
                return response.substring(3); // "000000000000000000000000"
            }

            return response;
        } catch (IOException e) {
            connected = false;
            System.out.println("통신 오류: " + e.getMessage());
            throw e;
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            connected = false;
            System.out.println("라즈베리파이 연결 종료");
        } catch (IOException e) {
            System.out.println("연결 종료 중 오류: " + e.getMessage());
        }
    }
}