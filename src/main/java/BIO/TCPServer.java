package BIO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private ServerSocket serverSocket;

    public void runs(){
        try {
            serverSocket = new ServerSocket(8080);
        }catch (IOException e){
            e.printStackTrace();
        }

        Socket socket =null;
        int count =0;//记录客户端的数量

        while(true){
            try {
                socket = serverSocket.accept();
            }catch (IOException e){
                e.printStackTrace();
            }

            ServerThread serverThread =new ServerThread(socket);
            serverThread.run();

            count++;
            System.out.println("客户端连接的数量："+count);
        }
    }

    class ServerThread implements Runnable{

        Socket socket =null;

        public ServerThread(Socket socket){
            this.socket = socket;
        }
        /**
         * 基于TCP协议的Socket通信，实现用户登录，服务端
         */
        public void run(){
            try {
                //获取输入流，并读取客户端信息
                InputStream is = socket.getInputStream();
                InputStreamReader isr =new InputStreamReader(is);
                BufferedReader br =new BufferedReader(isr);
                String info =null;

                while((info=br.readLine())!=null){
                    System.out.println("我是服务器，客户端说："+info);
                }

                //关闭输入流
                socket.shutdownInput();

                //获取输出流，响应客户端的请求
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);
                pw.write("欢迎您！");
                pw.flush();

                //关闭资源
                pw.close();
                os.close();
                br.close();
                isr.close();
                is.close();
                socket.close();

            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }
}
