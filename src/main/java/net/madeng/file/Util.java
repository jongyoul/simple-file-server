package net.madeng.file;

import static net.madeng.file.Constant.DEFAULT_CHARSET;
import static net.madeng.file.Constant.DELIMITER;

import java.nio.ByteBuffer;

public class Util {

  private static String trimNewLine(String s) {
    return s.replace("\r", "").replace("\n", "");
  }

  private static String attachNewLine(String s) {
    return s + "\r\n";
  }

  public static ByteBuffer makeMessage(String s) {
    return DEFAULT_CHARSET.encode(attachNewLine(s));
  }

  public static String getString(ByteBuffer buffer) {
    return trimNewLine(DEFAULT_CHARSET.decode(buffer).toString());
  }

  public static String[] getCommands(String commands) {
    return trimNewLine(commands).split(DELIMITER);
  }
}
