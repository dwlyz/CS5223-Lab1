package dslabs.clientserver;

import dslabs.atmostonce.AMOCommand;
import dslabs.atmostonce.AMOResult;
import dslabs.framework.Address;
import dslabs.framework.Client;
import dslabs.framework.Command;
import dslabs.framework.Result;
import dslabs.framework.Node;
import dslabs.framework.Result;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Simple client that sends requests to a single server and returns responses.
 *
 * <p>See the documentation of {@link Client} and {@link Node} for important implementation notes.
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
class SimpleClient extends Node implements Client {
  private final Address serverAddress;

  // Init to store
  private int sequenceNum;
  private Command currentCommand;
  private Result currentResult;

  /* -----------------------------------------------------------------------------------------------
   *  Construction and Initialization
   * ---------------------------------------------------------------------------------------------*/
  public SimpleClient(Address address, Address serverAddress) {
    super(address);
    this.serverAddress = serverAddress;
  }

  @Override
  public synchronized void init() {
    // No initialization necessary
  }

  /* -----------------------------------------------------------------------------------------------
   *  Client Methods
   * ---------------------------------------------------------------------------------------------*/
  @Override
  public synchronized void sendCommand(Command command) {
    sequenceNum++;
    currentCommand = command;
    currentResult = null;

    sendRequest();
  }

  @Override
  public synchronized boolean hasResult() {
    // if null, return false else true
    return currentResult != null;
  }

  @Override
  public synchronized Result getResult() throws InterruptedException {
    // use a while loop to block until result
    while (currentResult == null) {
      wait();
    }
    return currentResult;
  }

  /* -----------------------------------------------------------------------------------------------
   *  Message Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void handleReply(Reply m, Address sender) {
    if (m.result().sequenceNum() == sequenceNum) {
      currentResult = m.result().result();
      notify();
    }
  }

  /* -----------------------------------------------------------------------------------------------
   *  Timer Handlers
   * ---------------------------------------------------------------------------------------------*/
  private synchronized void onClientTimer(ClientTimer t) {
    if (t.sequenceNum() == sequenceNum && currentResult == null) {
      sendRequest();
    }
  }

  /* Assistant function for send  
   */
  private void sendRequest() {
    send(new Request(new AMOCommand(currentCommand, sequenceNum, address())), serverAddress);
    set(new ClientTimer(sequenceNum), ClientTimer.CLIENT_RETRY_MILLIS);
  }
}