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

  // Your code here...
  private final java.util.Map<dslabs.framework.Address, AMOResult> executed = new java.util.HashMap<>();

  @Override
  public AMOResult execute(Command command) {
    if (!(command instanceof AMOCommand)) {
      throw new IllegalArgumentException();
    }

    AMOCommand amoCommand = (AMOCommand) command;

    if (alreadyExecuted(amoCommand)) {
      return executed.get(amoCommand.address());
    }

    Result result = application.execute(amoCommand.command());
    AMOResult amoResult = new AMOResult(result, amoCommand.sequenceNum());
    executed.put(amoCommand.address(), amoResult);
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
    // Your code here...
    AMOResult saved = executed.get(amoCommand.address());
    return saved != null && saved.sequenceNum() >= amoCommand.sequenceNum();
  }
}
