package net.madeng.file;

import static java.lang.String.join;
import static net.madeng.file.Constant.DELIMITER;
import static net.madeng.file.Util.getCommands;
import static net.madeng.file.Util.getString;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Command {

  private static final Command UNKNOWN = new Command(CommandType.UNKNOWN);

  public enum CommandType {
    INDEX(0, "index"),
    GET(1, "get"),
    QUIT(0, "q", "quit"),
    UNKNOWN(0, ""),

    // Internal command to check the size from clients
    SIZE(1, "size");

    private final int numParameter;
    private final List<String> commands;

    CommandType(int numParameter, String... command) {
      this.commands = List.of(command);
      this.numParameter = numParameter;
    }

    public int getNumParameter() {
      return numParameter;
    }

    public List<String> getCommands() {
      return commands;
    }
  }

  private final CommandType commandType;
  private final List<String> parameters;

  private Command(CommandType commandType) {
    this(commandType, Collections.emptyList());
  }

  private Command(CommandType commandType, List<String> parameters) {
    this.commandType = commandType;
    this.parameters = parameters;
  }

  public CommandType getCommandType() {
    return commandType;
  }

  public List<String> getParameters() {
    return parameters;
  }

  @Override
  public String toString() {
    return join(DELIMITER, commandType.toString().toLowerCase(), join(DELIMITER, parameters));
  }

  public static Command of(String command) {
    String[] commands = getCommands(command);

    if (commands.length < 1) {
      return UNKNOWN;
    }

    return Arrays.stream(CommandType.values())
        .filter(commandType -> commandType.getCommands().contains(commands[0]) &&
            (commandType.getNumParameter() == commands.length - 1))
        .findFirst()
        .map(commandType -> {
          int numParameter = commandType.getNumParameter();
          if (numParameter == 0) {
            return new Command(commandType);
          } else {
            List<String> parameters = List
                .of(Arrays.copyOfRange(commands, 1, 1 + commandType.getNumParameter()));
            return new Command(commandType, parameters);
          }
        })
        .orElse(UNKNOWN);
  }

  public static Command of(ByteBuffer command) {
    return of(getString(command));
  }
}
