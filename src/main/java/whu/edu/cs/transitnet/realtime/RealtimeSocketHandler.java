package whu.edu.cs.transitnet.realtime;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 管理所有连接到服务器上的客户端 websocket 连接。
 * 建立连接、用户消息、用户断开等接口实现。
 */
@Slf4j
@Component
public class RealtimeSocketHandler extends TextWebSocketHandler {

    @Autowired
    private RealtimeService realtimeService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = session.getId();
        boolean result = WsSocketClientManager.add(token, session);
        if (!result) {
            log.warn("there are too many clients! Consider upgrade the service?");
            log.warn("连接过多！考虑扩张一下服务？");
            String msg = "[msg]服务器拥堵，请稍后再试。";
            session.sendMessage(new TextMessage(msg));
            session.close();
            return;
        }
        // 连接后即将实时数据发送给客户端。
        String msg = JSON.toJSONString(realtimeService.getAllVehicles());
        session.sendMessage(new TextMessage(msg));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 暂时不处理前端传入的消息
        log.info("[socket]message from session {}: {}", session.getRemoteAddress(), message.toString());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 用户退出
        String token = session.getId();
        boolean result = WsSocketClientManager.remove(token);
        if (!result) {
            log.warn("[socket]Error while user connection closed, remove session from pool failed.");
        }
    }

}
