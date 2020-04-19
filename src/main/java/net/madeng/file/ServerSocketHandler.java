package net.madeng.file;

import static java.lang.String.format;
import static java.lang.String.join;
import static net.madeng.file.Constant.BUF_SIZE;
import static net.madeng.file.Util.makeMessage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.madeng.file.Command.CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerSocketHandler implements Runnable {

  private final Logger logger;
  private final SocketChannel socketChannel;

  private boolean isRunning = true;

  public ServerSocketHandler(SocketChannel socketChannel, int handlerNumber) {
    this.socketChannel = socketChannel;
    this.logger = LoggerFactory
        .getLogger(format("%s-%d", this.getClass().getSimpleName(), handlerNumber));
  }

  @Override
  public void run() {
    try {
      logger.info("Start a new SocketHandler: {}", socketChannel.getRemoteAddress());
    } catch (IOException e) {
      logger.error("Cannot get RemoteAddress", e);
      isRunning = false;
    }

    ByteBuffer in = ByteBuffer.allocate(BUF_SIZE);
    Command command;
    while (isRunning) {
      if(!socketChannel.isConnected()) {
        isRunning = false;
      } else {
        try {
          in.clear();
          socketChannel.read(in);
          in.flip();
          command = Command.of(in);
          logger.debug(command.toString());
          handle(command);
        } catch (IOException e) {
          logger.error("Cannot read data from the client");
          isRunning = false;
        } catch (RuntimeException e) {
          // TODO: Make a concrete type of exception
        }
      }
    }

    logger.info("Stop the SocketHandler");
    try {
      socketChannel.close();
    } catch (IOException ignore) {
      logger.debug("error while closing");
    }
  }

  void handle(Command command) {
    CommandType commandType = command.getCommandType();

    try {
      switch (commandType) {
        case INDEX:
          socketChannel
              .write(makeMessage(join(System.lineSeparator(), getFiles(Paths.get("")))));
          break;
        case GET:
          try {
            sendFile(command.getParameters().get(0));
            socketChannel.write(makeMessage("ok"));
          } catch (FileNotFoundException e) {
            socketChannel.write(makeMessage("error"));
          }
          break;
        case QUIT:
          isRunning = false;
          break;
        case SIZE:
          try {
            sendSize(command.getParameters().get(0));
          } catch (FileNotFoundException e) {
            socketChannel.write(makeMessage("error"));
          }
          break;
        case UNKNOWN:
        default:
          logger.debug("Unknown command");
          socketChannel.write(makeMessage("unknown command"));
      }
    } catch (IOException e) {
      logger.error("error while handling command: {}", command, e);
    }
  }

  List<String> getFiles(Path cur) throws IOException {
    List<String> files = new ArrayList<>();

    // To pass exception to the caller
    for (Path p : Files.newDirectoryStream(cur)) {
      if (Files.isDirectory(p)) {
        files.addAll(getFiles(p));
      } else {
        files.add(p.toString());
      }
    }

    Collections.sort(files);
    return files;
  }

  void sendFile(String p) throws IOException {
    if (!Files.exists(Paths.get(p)) || Files.isDirectory(Paths.get(p))) {
      throw new FileNotFoundException(format("The file of %s doesn't exist", p));
    }

    ByteBuffer buffer = ByteBuffer.allocate(BUF_SIZE);
    RandomAccessFile file = new RandomAccessFile(p, "r");
    FileChannel in = file.getChannel();
    while (in.read(buffer) > 0) {
      buffer.flip();
      socketChannel.write(buffer);
      buffer.clear();
    }
  }

  void sendSize(String p) throws IOException {
    if (!Files.exists(Paths.get(p)) || Files.isDirectory(Paths.get(p))) {
      throw new FileNotFoundException(format("The file of %s doesn't exist", p));
    }

    RandomAccessFile file = new RandomAccessFile(p, "r");
    socketChannel.write(makeMessage(String.valueOf(file.length())));
  }
}
