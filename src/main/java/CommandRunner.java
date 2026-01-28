import java.io.InputStream;
import java.io.OutputStream;

public interface CommandRunner {

  InputStream stdout();

  OutputStream stdin();

  InputStream stderr();

  void start();

  void waitFor() throws Exception;

  void stop();
}
