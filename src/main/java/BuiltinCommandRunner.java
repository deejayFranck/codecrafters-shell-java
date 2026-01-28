import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class BuiltinCommandRunner implements CommandRunner {

  private final PipedInputStream stdout = new PipedInputStream();
  private final PipedOutputStream stdoutWriter = new PipedOutputStream();

  private final PipedInputStream stdin = new PipedInputStream();
  private final PipedOutputStream stdinWriter = new PipedOutputStream();

  private final BuiltinCommandHandler handler = new BuiltinCommandHandler();
  private final String command;
  private final Shell shell;
  private Thread thread;

  public BuiltinCommandRunner(String command, Shell shell) throws IOException {
    this.command = command;
    this.shell = shell;

    stdout.connect(stdoutWriter);
    stdin.connect(stdinWriter);
  }

  @Override
  public InputStream stdout() {
    return stdout;
  }

  @Override
  public InputStream stderr() {
    return InputStream.nullInputStream();
  }

  @Override
  public OutputStream stdin() {
    return stdinWriter;
  }

  @Override
  public void start() {
    thread =
        new Thread(
            () -> {
              try {
                handler.handle(command, shell, stdin, stdoutWriter, System.err);
              } catch (Exception ignored) {
              } finally {
                try {
                  stdoutWriter.close();
                } catch (IOException ignored) {
                }
              }
            });
    thread.start();
  }

  @Override
  public void stop() {
    if (thread != null) thread.interrupt();
  }

  @Override
  public void waitFor() throws Exception {}
}
