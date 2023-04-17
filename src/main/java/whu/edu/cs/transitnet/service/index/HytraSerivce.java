package whu.edu.cs.transitnet.service.index;

import edu.whu.hytra.core.SocketStorageManager;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.net.Socket;

@Service
public class HytraSerivce {
    @Bean
    public SocketStorageManager getStorageManager() {
        try {
            Socket socket = new Socket("127.0.0.1", 9200);
            return new SocketStorageManager(socket);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}
