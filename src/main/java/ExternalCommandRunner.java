import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class ExternalCommandRunner implements CommandRunner {

  private final Process process;

  public ExternalCommandRunner(List<String> args, Shell shell) throws Exception {
    ProcessBuilder pb = new ProcessBuilder(args);
    pb.directory(shell.getCurrentDirectory());
    this.process = pb.start();
  }

  @Override
  public InputStream stdout() {
    return process.getInputStream();
  }

  @Override
  public InputStream stderr() {
    return process.getErrorStream();
  }

  @Override
  public OutputStream stdin() {
    return process.getOutputStream();
  }

  @Override
  public void start() {
    /* already started */
  }

  @Override
  public void stop() {
    process.destroy();
  }

  @Override
  public void waitFor() throws Exception {}
}
