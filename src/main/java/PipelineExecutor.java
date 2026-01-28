import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class PipelineExecutor {

  public void executePipeline(String input, Shell shell) throws Exception {
    String[] parts = input.split("\\|", 2);

    CommandRunner left = create(parts[0].trim(), shell);
    CommandRunner right = create(parts[1].trim(), shell);

    // pipe left stdout → right stdin
    Thread pipe =
        new Thread(
            () -> {
              try (BufferedReader in = new BufferedReader(new InputStreamReader(left.stdout()));
                  BufferedWriter out = new BufferedWriter(new OutputStreamWriter(right.stdin()))) {
                String line;
                while ((line = in.readLine()) != null) {
                  out.write(line);
                  out.newLine();
                  out.flush(); // REQUIRED for tail -f
                }
              } catch (IOException ignored) {
              }
            });

    // stderr passthrough
    Thread err1 = stream(left.stderr(), System.err);
    Thread err2 = stream(right.stderr(), System.err);

    // final stdout → terminal
    Thread out = stream(right.stdout(), System.out);

    left.start();
    right.start();

    pipe.start();
    err1.start();
    err2.start();
    out.start();

    // WAIT ONLY FOR RIGHT SIDE OUTPUT
    out.join();

    left.stop();
    right.stop();
  }

  private CommandRunner create(String cmd, Shell shell) throws Exception {
    List<String> args = ArgumentParser.parse(cmd);
    if (BuiltinCommandHandler.BUILT_INS.contains(args.get(0))) {
      return new BuiltinCommandRunner(cmd, shell);
    }
    return new ExternalCommandRunner(args, shell);
  }

  private Thread stream(InputStream in, OutputStream out) {
    Thread t =
        new Thread(
            () -> {
              try {
                in.transferTo(out);
                out.flush();
              } catch (IOException ignored) {
              }
            });
    return t;
  }
}
