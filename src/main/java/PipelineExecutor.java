import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class PipelineExecutor {

  public void executePipeline(String input, Shell shell) throws Exception {

    String[] parts = input.split("\\|");
    int n = parts.length;

    List<CommandRunner> runners = new ArrayList<>();

    for (String part : parts) {
      runners.add(create(part.trim(), shell));
    }

    List<Thread> threads = new ArrayList<>();

    // pipe stdout[i] → stdin[i+1]
    for (int i = 0; i < n - 1; i++) {
      CommandRunner left = runners.get(i);
      CommandRunner right = runners.get(i + 1);

      Thread pipe =
          new Thread(
              () -> {
                try (BufferedReader in =
                        new BufferedReader(new InputStreamReader(left.stdout()));
                    BufferedWriter out =
                        new BufferedWriter(new OutputStreamWriter(right.stdin()))) {

                  String line;
                  while ((line = in.readLine()) != null) {
                    out.write(line);
                    out.newLine();
                    out.flush(); // REQUIRED for tail -f
                  }
                } catch (IOException ignored) {
                }
              });

      threads.add(pipe);
    }

    // stderr passthrough
    for (CommandRunner r : runners) {
      threads.add(stream(r.stderr(), System.err));
    }

    // final stdout → terminal
    Thread finalOut = stream(runners.get(n - 1).stdout(), System.out);
    threads.add(finalOut);

    // start commands
    for (CommandRunner r : runners) {
      r.start();
    }

    // start all pipes/streams
    for (Thread t : threads) {
      t.start();
    }

    // WAIT ONLY FOR LAST COMMAND OUTPUT
    finalOut.join();

    // stop everything else (important for tail -f)
    for (CommandRunner r : runners) {
      r.stop();
    }
  }

  private CommandRunner create(String cmd, Shell shell) throws Exception {
    List<String> args = ArgumentParser.parse(cmd);
    if (BuiltinCommandHandler.BUILT_INS.contains(args.get(0))) {
      return new BuiltinCommandRunner(cmd, shell);
    }
    return new ExternalCommandRunner(args, shell);
  }

  private Thread stream(InputStream in, OutputStream out) {
    return new Thread(
        () -> {
          try {
            in.transferTo(out);
            out.flush();
          } catch (IOException ignored) {
          }
        });
  }
}
