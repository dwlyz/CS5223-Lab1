package dslabs.atmostonce;

import dslabs.framework.Application;
import dslabs.framework.Command;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public final class AMOApplication<T extends Application> implements Application {
  @Getter @NonNull private final T application;

  // Use a map to store executed command since stated in piazza that map is allowed
  private final java.util.Map<dslabs.framework.Address, AMOResult> executedCommandMap = new java.util.HashMap<>();

  @Override
  public AMOResult execute(Command command) {
    if (!(command instanceof AMOCommand)) {
      throw new IllegalArgumentException();
    }

    AMOCommand amoCommand = (AMOCommand) command;

    // check map if command already executed
    if (alreadyExecuted(amoCommand)) {
      return executedCommandMap.get(amoCommand.address());
    }

    // if never execute, then execute
    Result result = application.execute(amoCommand.command());
    AMOResult amoResult = new AMOResult(result, amoCommand.sequenceNum());
    // then store command in map
    executedCommandMap.put(amoCommand.address(), amoResult);
    return amoResult;
  }

  public Result executeReadOnly(Command command) {
    if (!command.readOnly()) {
      throw new IllegalArgumentException();
    }

    if (command instanceof AMOCommand) {
      return execute(command);
    }

    return application.execute(command);
  }

  public boolean alreadyExecuted(AMOCommand amoCommand) {
    // Check if map has command
    AMOResult saved = executedCommandMap.get(amoCommand.address());
    return saved != null && saved.sequenceNum() >= amoCommand.sequenceNum();
  }
}
