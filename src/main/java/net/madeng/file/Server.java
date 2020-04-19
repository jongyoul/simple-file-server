package net.madeng.file;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

  private static final Logger logger = LoggerFactory.getLogger(Server.class);

  private final int port;
  private final ExecutorService workerPool;
  private final AtomicInteger serverHandlerNumber;

  public Server(int port, ExecutorService workerPool) {
    this.port = port;
    this.workerPool = workerPool;
    this.serverHandlerNumber = new AtomicInteger(0);
  }

  public void start() {
    boolean isRunning;
    try {
      ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
      serverSocketChannel.configureBlocking(true);
      serverSocketChannel.bind(new InetSocketAddress(port));
      isRunning = true;

      while (isRunning) {
        SocketChannel socketChannel = serverSocketChannel.accept();
        workerPool
            .submit(new ServerSocketHandler(socketChannel, serverHandlerNumber.getAndIncrement()));
      }
      workerPool.awaitTermination(30, TimeUnit.SECONDS);
    } catch (IOException | InterruptedException e) {
      logger.error("Error while running the server", e);
    }
  }

  public static void main(String[] args) {
    final int port = 9000;
    final ExecutorService workerPool = Executors.newCachedThreadPool();

    Server server = new Server(port, workerPool);
    server.start();
  }
}
