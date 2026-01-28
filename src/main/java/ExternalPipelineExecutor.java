import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class ExternalPipelineExecutor {

  public void executePipeline(String input, Shell shell) throws Exception {
    String[] parts = input.split("\\|", 2);

    List<String> leftArgs = ArgumentParser.parse(parts[0].trim());
    List<String> rightArgs = ArgumentParser.parse(parts[1].trim());

    ProcessBuilder pb1 = new ProcessBuilder(leftArgs);
    ProcessBuilder pb2 = new ProcessBuilder(rightArgs);

    pb1.directory(shell.getCurrentDirectory());
    pb2.directory(shell.getCurrentDirectory());

    Process p1 = pb1.start();
    Process p2 = pb2.start();

    // stdout of p1 → stdin of p2 (LINE-BY-LINE, FLUSHED)
    Thread pipe =
        new Thread(
            () -> {
              try (BufferedReader in =
                      new BufferedReader(new InputStreamReader(p1.getInputStream()));
                  BufferedWriter out =
                      new BufferedWriter(new OutputStreamWriter(p2.getOutputStream()))) {
                String line;
                while ((line = in.readLine()) != null) {
                  out.write(line);
                  out.newLine();
                  out.flush(); 
                }
              } catch (IOException ignored) {
              }
            });

    // stderr passthrough
    Thread err1 = streamTo(p1.getErrorStream(), System.err);
    Thread err2 = streamTo(p2.getErrorStream(), System.err);

    // stdout of p2 → terminal
    Thread out = streamTo(p2.getInputStream(), System.out);

    pipe.start();

    // wait for RIGHT side only
    p2.waitFor();

    // kill LEFT side (tail -f)
    p1.destroy();

    pipe.join();
    err1.join();
    err2.join();
    out.join();
  }

  private Thread streamTo(InputStream in, OutputStream out) {
    Thread t =
        new Thread(
            () -> {
              try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = br.readLine()) != null) {
                  out.write((line + System.lineSeparator()).getBytes());
                  out.flush();
                }
              } catch (IOException ignored) {
              }
            });
    t.start();
    return t;
  }
}
