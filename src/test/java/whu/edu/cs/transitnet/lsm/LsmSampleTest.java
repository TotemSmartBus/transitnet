package whu.edu.cs.transitnet.lsm;

import edu.whu.hytra.core.SocketStorageManager;
import edu.whu.hytra.core.StorageManager;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class LsmSampleTest {
    @Test
    public void ATest() throws Exception {
        Socket socket = new Socket("127.0.0.1", 9201);
        socket.sendUrgentData(0xff);
        SocketStorageManager sm=new SocketStorageManager(socket);
        sm.write("\\test");
        sm.read();
    }

    public class SocketStorageManager implements StorageManager {
        private Logger log = LoggerFactory.getLogger(edu.whu.hytra.core.SocketStorageManager.class);
        private String SUCCESS = "Insert OK!";
        private Socket s;

        public SocketStorageManager(Socket socket) {
            this.s = socket;
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
            bw.write(msg);
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
    }
}
