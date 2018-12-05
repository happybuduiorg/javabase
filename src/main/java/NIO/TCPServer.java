package NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class TCPServer {
    private static final int BUF_SIZE = 1024;
    private static final int PORT = 8080;
    private static final int TIMEOUT = 3000;
    private TCPServer() {
    }
    public static final TCPServer instance = new TCPServer();
    public static TCPServer getInstance() {
        return instance;
    }

    /*
     * 关键类
     */
    public void selector() {
        try (Selector selector = Selector.open(); ServerSocketChannel ssChannel = ServerSocketChannel.open()) {
            ssChannel.bind(new InetSocketAddress(PORT));
            ssChannel.configureBlocking(false);
            ssChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                if (0 == selector.select(TIMEOUT)) {
                    System.out.println("==");
                    continue;
                }

                // 使用 for (SelectionKey key : selector.selectedKeys()) 方式无法移除对象
                // Iterator对象的remove方法是迭代过程中删除元素的唯一方法
                Iterator<SelectionKey> selectKeys = selector.selectedKeys().iterator();
                while (selectKeys.hasNext()) {
                    SelectionKey key = selectKeys.next();
                    //分别处理接受、连接、读、写4中状态
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    if (key.isConnectable()) {
                        System.out.println("isConnectable = true");
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    if (key.isWritable() && key.isValid()) {
                        handleWrite(key);
                    }
                    selectKeys.remove();
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();// 获取ServerSocket的Channel
        SocketChannel sc = ssChannel.accept();// 监听新进来的链接
        sc.configureBlocking(false);// 设置client链接为非阻塞
        sc.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(BUF_SIZE));// 将channel注册Selector
    }

    public void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();// 获取链接client的channel
        ByteBuffer buf = (ByteBuffer) key.attachment();// 获取附加在key上的数据
        long bytesRead = channel.read(buf);// 读channel上数据到buf?这部分表达的是否正确？
        while (bytesRead > 0) {
            buf.flip();
            while (buf.hasRemaining()) {
                System.out.println(buf.getChar());
            }
            System.out.println();
            buf.clear();
            bytesRead = channel.read(buf);
        }
        if (-1 == bytesRead) {
            channel.close();
        }
    }

    public void handleWrite(SelectionKey key) throws IOException {
        ByteBuffer buf = (ByteBuffer) key.attachment();// 获取buffer对象
        buf.flip();
        SocketChannel channel = (SocketChannel) key.channel();
        while (buf.hasRemaining()) {
            channel.write(buf);
        }
        buf.compact();
    }
}
