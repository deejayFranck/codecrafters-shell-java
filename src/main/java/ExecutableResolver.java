import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ExecutableResolver {

  public static String[] directories = System.getenv("PATH").split(File.pathSeparator);

  public static File find(String name) {
    for (String dir : directories) {
      File f = new File(dir, name);
      if (f.exists() && f.canExecute()) return f;
    }
    return null;
  }

  public static Set<String> getExecutables(){

    Set<String> executables = new HashSet<>();
    File[] files = null;
    if(directories == null)
      return null;

    for(String dir : directories){
      File fileDirectory = new File(dir);

      if(fileDirectory.isDirectory())
        files = fileDirectory.listFiles();

      if(files != null){
        for(File file : files){
          if(file.isFile() && file.canExecute())
            executables.add(file.getName());
        }
      }
    }

    return executables;

  }
}
