import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

class ShellCompleter implements Completer {

  private final List<String> executables;
  private boolean pending = false;
  private String lastWord = "";
  private String lowestCommonPrefix = "";

  ShellCompleter(Set<String> executables) {
    this.executables = new ArrayList<>(executables);
  }

  @Override
  public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {

    String word = line.word();

    List<String> matches = executables.stream().filter(e -> e.startsWith(word)).sorted().toList();

    if (matches.isEmpty()) {
      return;
    }

    var buffer = reader.getBuffer();

    if (matches.size() == 1) {
      String full = matches.get(0);
      candidates.add(new Candidate(full, full, null, null, null, null, true));
      return;
    }

    String lcp = longestCommonPrefix(matches);

    if (lcp.length() > word.length()) {
      candidates.add(new Candidate(lcp, lcp, null, null, null, null, false));
      return;
    }

    // FIRST TAB → bell only
    if (!pending || !word.equals(lastWord)) {
      reader.getTerminal().writer().print("\u0007");
      reader.getTerminal().writer().flush();
      pending = true;
      lastWord = word;
      return;
    }

    // SECOND TAB → print matches
    var out = reader.getTerminal().writer();
    out.println();
    matches.forEach(m -> out.print(m + "  "));
    out.println();
    out.flush();

    reader.callWidget(LineReader.REDRAW_LINE);
    reader.callWidget(LineReader.REDISPLAY);

    pending = false;
  }

  public String longestCommonPrefix(List<String> matches) {

    String last = matches.get(matches.size() - 1);
    String first = matches.get(0);

    int i = 0;

    while (i < first.length() && i < last.length() && last.charAt(i) == first.charAt(i)) {
      i++;
    }

    return first.substring(0, i);
  }
}
