package net.madeng.file;

import static net.madeng.file.Constant.BUF_SIZE;
import static net.madeng.file.Constant.DEFAULT_CHARSET;
import static net.madeng.file.Util.makeMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.madeng.file.Command.CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {

  private static final Logger logger = LoggerFactory.getLogger(Client.class);

  private final String host;
  private final int port;
  private final Scanner scanner;
  private final ExecutorService downloadWorkerPool;

  private SocketChannel socketChannel;
  private boolean isRunning = true;

  public Client(String host, int port, Scanner scanner,
      ExecutorService downloadWorkerPool) {
    this.host = host;
    this.port = port;
    this.scanner = scanner;
    this.downloadWorkerPool = downloadWorkerPool;
  }

  public void start() {
    try {
      logger.info("Start a new Client");
      socketChannel = SocketChannel.open();
      socketChannel.configureBlocking(true);
      socketChannel.connect(new InetSocketAddress(host, port));
    } catch (IOException e) {
      logger.error("Cannot start Client", e);
      isRunning = false;
    }

    try {
      while (isRunning && scanner.hasNext()) {
        Command command = Command.of(scanner.next());
        logger.debug(command.toString());
        handle(command);
      }
    } catch (Exception e) {
      logger.error("error while handling command", e);
    }

    try {
      socketChannel.close();
    } catch (IOException e) {
      logger.debug("Error while closing socket", e);
    }
  }

  private void handle(Command command) throws IOException {
    CommandType commandType = command.getCommandType();
    ByteBuffer buffer = ByteBuffer.allocate(BUF_SIZE);

    switch (commandType) {
      case GET:
        CompletableFuture.runAsync(() -> {
          try {
            ClientDownloadHandler clientDownloadHandler = new ClientDownloadHandler(command, host,
                port);
            clientDownloadHandler.start();
          } catch (IOException e) {
            System.out.println("error");
          }
        }, downloadWorkerPool);
        break;
      case INDEX:
      case QUIT:
      case UNKNOWN:
      default:
        socketChannel.write(makeMessage(command.toString()));
        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();
        System.out.println(DEFAULT_CHARSET.decode(buffer));
        break;
    }
  }


  public static void main(String[] args) throws IOException {
    final String host = "localhost";
    final int port = 9000;

    Scanner scanner = new Scanner(System.in).useDelimiter("\n");
    ExecutorService downloadWorkerPool = Executors.newCachedThreadPool();

    Client client = new Client(host, port, scanner, downloadWorkerPool);
    client.start();
  }
}
