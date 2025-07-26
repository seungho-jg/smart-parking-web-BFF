// Router.java
package bff;

import bff.handlers.Handler;

import java.util.Map;
import java.util.HashMap;

/**
 * 요청 경로(path)마다 Handler를 매핑하고 조회하는 라우터 클래스
 */
public class Router {
    // 경로와 핸들러를 저장하는 Map
    private final Map<String, Handler> routes = new HashMap<>();

    // 핸들러 등록
    public void register(String path, Handler handler) {
        routes.put(path, handler);
    }
    // 핸들러 조회
    public Handler resolve(String path) {
        if (routes.get(path) != null) {
            return routes.get(path);
        }
        return null;
    }
}
