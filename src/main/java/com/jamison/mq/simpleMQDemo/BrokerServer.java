package com.jamison.mq.simpleMQDemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 有了消息处理中心类之后，需要将该类的功能暴露出去，这样别人能用它发送和接受消息。
 * 所以我们定义了BrokerServer类来对外提供Broker类的服务
 */
public class BrokerServer implements Runnable{
    public static int SERVICE_PORT = 9999;

    private final Socket socket;

    public BrokerServer(Socket socket) {
        this.socket = socket;
    }
    public void run() {
        try(
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream())
                ){
            while (true) {
                String str = in.readLine();
                if(str == null) {
                    continue;
                }
                System.out.println("接受到原始数据" + str);

                if(str.equals("CONSUME")) { // CONSUME表示要消费一条数据
                    //从消息队列中消费一条消息
                    String message = Broker.consume();
                    out.println(message);
                    out.flush();
                } else {
                    //其他情况都表示生产消息放到消息队列中
                    Broker.produce(str);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(SERVICE_PORT);
        while (true) {
            BrokerServer brokerServer = new BrokerServer(server.accept());
            new Thread(brokerServer).start();
        }
    }
}
