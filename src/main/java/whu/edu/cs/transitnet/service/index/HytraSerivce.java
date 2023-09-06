package whu.edu.cs.transitnet.service.index;

import edu.whu.hytra.core.SocketStorageManager;
import edu.whu.hytra.core.StorageManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.Socket;

@Slf4j
@Configuration
public class HytraSerivce {

    @Value("${hytra.socket.host}")
    private String host;

    @Value("${hytra.socket.port}")
    private int port;

    @Bean
    /**
     * 进行 LsmTreeTest 测试时注释掉下面这句
     */
    @ConditionalOnProperty(value = "${transitnet.index.enable}", havingValue = "true")
    public SocketStorageManager storageManager() {
        try {
            Socket socket = new Socket(host, port);
            socket.sendUrgentData(0xff);
            return new SocketStorageManager(socket);
        } catch (Exception e) {
            log.error(String.format("Socket connection error! %s:%d", host, port), e);
            System.exit(-1);
        }
        return null;
    }

    @Bean
    @ConditionalOnProperty(value = "${transitnet.index.enable}", havingValue = "false",matchIfMissing = true)
    public StorageManager emptyStorageManager() {
        return new StorageManager() {
            @Override
            public void put(String s, String s1) throws Exception {

            }

            @Override
            public String get(String s) throws Exception {
                return null;
            }

            @Override
            public String status() throws Exception {
                return null;
            }

            @Override
            public boolean config(String s, String s1) throws Exception {
                return false;
            }

            @Override
            public void close() throws Exception {

            }
        };
    }
}
