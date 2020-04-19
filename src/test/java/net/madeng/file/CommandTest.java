package net.madeng.file;

import static net.madeng.file.Command.CommandType.GET;
import static net.madeng.file.Command.CommandType.INDEX;
import static net.madeng.file.Command.CommandType.QUIT;
import static net.madeng.file.Command.CommandType.UNKNOWN;
import static net.madeng.file.Util.makeMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CommandTest {

  @Test
  void testGetCommand() {
    assertEquals(INDEX, Command.of(makeMessage("index")).getCommandType());
    assertEquals(UNKNOWN, Command.of(makeMessage("index 1")).getCommandType());
    assertEquals(GET, Command.of(makeMessage("get 1")).getCommandType());
    assertEquals(UNKNOWN, Command.of(makeMessage("get")).getCommandType());
    assertEquals(UNKNOWN, Command.of(makeMessage("get 1 2")).getCommandType());
    assertEquals(QUIT, Command.of(makeMessage("quit")).getCommandType());
    assertEquals(QUIT, Command.of(makeMessage("q")).getCommandType());
    assertEquals(UNKNOWN, Command.of(makeMessage("quit 1")).getCommandType());
    assertEquals(UNKNOWN, Command.of(makeMessage("q 1")).getCommandType());
  }
}