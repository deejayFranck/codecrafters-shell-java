public class CommandDispatcher {

  private final BuiltinCommandHandler builtinHandler = new BuiltinCommandHandler();
  private final ExternalCommandExecutor externalExecutor = new ExternalCommandExecutor();
  private final PipelineExecutor pipelineExecutor = new PipelineExecutor();

  public void dispatch(String input, Shell shell) throws Exception {

    if (input.contains("|")) {
      pipelineExecutor.executePipeline(input, shell);
    } else {
      if (!builtinHandler.handle(input, shell)) {
        externalExecutor.execute(input, shell);
      }
    }
  }
}
