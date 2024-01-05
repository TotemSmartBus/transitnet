package whu.edu.cs.transitnet.lsm;

import edu.whu.hytra.core.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;

@Service
public class SocketStorageManager implements StorageManager {
    private static final Logger log = LoggerFactory.getLogger(whu.edu.cs.transitnet.lsm.SocketStorageManager.class);
    @Value("${hytra.socket.host}")
    private String host;

    @Value("${hytra.socket.port}")
    private int port;
    private Socket s;

    public SocketStorageManager() {
    }

    public void put(String key, String value) throws Exception {
        this.write("put:" + key + "," + value);
        String code = this.read();
        if (!"Insert OK!".equals(code)) {
            throw new IOException("error sending command," + code);
        }
    }

    public String get(String key) throws Exception {
        this.write("get:" + key);
        return this.read();
    }

    public String status() throws Exception {
        this.write("status");
        return this.read();
    }

    public boolean config(String key, String path) throws Exception {
        this.write("config:" + key + "," + path);
        String result = this.read();
        return "Insert OK!".equals(result);
    }

    private void write(String msg) throws Exception {
        OutputStream os = this.s.getOutputStream();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        bw.write(msg+"\0");
        log.debug("[socket]" + msg);
        bw.flush();
    }

    private String read() throws Exception {
        InputStream is = this.s.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String mess = br.readLine();
        log.debug("[socket]receive " + mess);
        return mess;
    }

    public void close() throws Exception {
        this.write("exit");
        this.s.close();
    }

    public void open() throws IOException {
        this.s=new Socket(host,port);
        this.s.setKeepAlive(true);
    }
}
