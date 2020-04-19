package net.madeng.file;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface Constant {

  int BUF_SIZE = 4 * 1024;
  Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  String DELIMITER = " ";
  String LENGTH_PREFIX = "length: ";
}
