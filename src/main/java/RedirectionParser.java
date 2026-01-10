public class RedirectionParser {

  public static Redirection parse(String input) {
    boolean inSingleQuotes = false, inDoubleQuotes = false;

    for (int i = 0; i < input.length(); i++) {
      char character = input.charAt(i);

      if (character == '\'' && !inDoubleQuotes) inSingleQuotes = !inSingleQuotes;
      else if (character == '"' && !inSingleQuotes) inDoubleQuotes = !inDoubleQuotes;
      else if (!inSingleQuotes && !inDoubleQuotes) {
        if (input.startsWith("2>", i)) {
          return new Redirection(
              input.substring(0, i), input.substring(i + 2).trim(), Redirection.Type.STDERR);
        }
        if (input.startsWith("1>>", i) || input.startsWith(">>", i)) {
          int offset = input.startsWith("1>>", i) ? 3 : 2;
          return new Redirection(
              input.substring(0, i), input.substring(i + offset).trim(), Redirection.Type.APPEND);
        }
        if (input.startsWith("1>", i)
            || (character == '>' && i + 1 < input.length() && input.charAt(i + 1) != '>')) {
          int offset = input.startsWith("1>", i) ? 2 : 1;
          return new Redirection(
              input.substring(0, i), input.substring(i + offset).trim(), Redirection.Type.STDOUT);
        }
      }
    }
    return Redirection.none(input);
  }
}
