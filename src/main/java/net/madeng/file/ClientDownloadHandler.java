package net.madeng.file;

import static java.lang.String.format;
import static java.lang.String.join;
import static net.madeng.file.Constant.BUF_SIZE;
import static net.madeng.file.Constant.DELIMITER;
import static net.madeng.file.Util.getString;
import static net.madeng.file.Util.makeMessage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import net.madeng.file.Command.CommandType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientDownloadHandler {

  private static final Logger logger = LoggerFactory.getLogger(ClientDownloadHandler.class);

  private final SocketChannel socketChannel;
  private final String commandName;
  private final String commandParameter;

  public ClientDownloadHandler(Command command, String host, int port) throws IOException {
    this.socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
    socketChannel.configureBlocking(true);
    this.commandName = command.getCommandType().getCommands().get(0);
    this.commandParameter = command.getParameters().get(0);
  }

  public void start() {
    try {
      long size = getSize();
      if (-1 != size) {
        download(size);
        System.out.println("ok");
      } else {
        System.out.println("error");
      }
    } catch (Exception e) {
      logger.error("Error while downloading the file of {}", commandParameter, e);
      System.out.println("error");
    }
  }

  private long getSize() throws IOException {
    socketChannel.write(
        makeMessage(join(DELIMITER, CommandType.SIZE.getCommands().get(0), commandParameter)));
    ByteBuffer buffer = ByteBuffer.allocate(BUF_SIZE);
    socketChannel.read(buffer);
    buffer.flip();
    String size = getString(buffer);
    if (size.contains("error")) {
      return -1;
    } else {
      return Long.parseLong(size);
    }
  }

  public void download(long size) throws IOException {
    socketChannel.write(makeMessage(join(DELIMITER, commandName, commandParameter)));

    // TODO: Change the pattern of filename
    String target = commandParameter.replace(File.pathSeparator, "_");
    RandomAccessFile out = new RandomAccessFile(target, "rw");
    ByteBuffer buffer = ByteBuffer.allocate(BUF_SIZE);

    long received = 0;
    while (size > received && (received += socketChannel.read(buffer)) > 0) {
      System.out
          .println(format("Progress. %s %02.0f", commandParameter, (float) received / size * 100));
      buffer.flip();
      out.getChannel().write(buffer);
      buffer.clear();
    }
    out.setLength(size);
    out.close();
  }
}
