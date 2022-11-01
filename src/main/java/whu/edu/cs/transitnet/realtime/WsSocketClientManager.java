package whu.edu.cs.transitnet.realtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WsSocketClientManager {
    private static ConcurrentHashMap<String, WebSocketSession> SESSION_POOL = new ConcurrentHashMap<>();

    /**
     * 向所有客户端广播更新内容
     *
     * @param msg 广播的消息内容
     */
    public static void broadcast(String msg) {
        SESSION_POOL.forEach((s, webSocketSession) -> {
            try {
                webSocketSession.sendMessage(new TextMessage(msg));
            } catch (IOException e) {
                log.warn("[websocket]Error while update msg for client {}", s);
            }
        });
    }

    public static boolean add(String key, WebSocketSession session) {
        // 设定池子最大容量
        if (SESSION_POOL.size() < 10) {
            SESSION_POOL.put(key, session);
            return true;
        } else {
            return false;
        }
    }

    public static boolean remove(String key) {
        WebSocketSession session = SESSION_POOL.get(key);
        if (session != null) {
            session = SESSION_POOL.remove(key);
            try {
                session.close();
            } catch (IOException e) {
                log.warn("[WsSocketManager]Error while closing socket");
                e.printStackTrace();
            }
        }
        return false;
    }

    public static WebSocketSession get(String key) {
        return SESSION_POOL.get(key);
    }


}
