public class CommandDispatcher {

    private final BuiltinCommandHandler builtinHandler = new BuiltinCommandHandler();
    private final ExternalCommandExecutor externalExecutor = new ExternalCommandExecutor();
    private final PipelineExecutor pipelineExecutor = new PipelineExecutor();

    public void dispatch(String input, Shell shell) throws Exception {
        if (builtinHandler.handle(input, shell)) {
            return;
        }
        if(input.contains("|")){
            pipelineExecutor.executePipeline(input, shell);
            return;
        }
        externalExecutor.execute(input, shell);
    }
}
