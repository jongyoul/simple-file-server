package net.madeng.file;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServerSocketHandlerTest {

  @Mock
  SocketChannel mockSocketChannel;

  @Test
  void testGetFiles() {
    ServerSocketHandler serverSocketHandler = new ServerSocketHandler(mockSocketChannel, 0);

    assertDoesNotThrow(() -> {
      List<String> files = serverSocketHandler.getFiles(Paths.get(""));
      assertFalse(files.isEmpty(), "Files shouldn't be empty");
    });

    assertThrows(IOException.class, () -> serverSocketHandler.getFiles(Paths.get("/wrong")));
  }
}