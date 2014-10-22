package com.artifylabs.server;

/**
 * User: lloyd
 * Date: 10/21/14
 * Time: 3:47 PM
 */
public interface ServerProcess<T extends Enum>
{
  public void start(SimpleServer<T> simpleServer);
  public void stop(SimpleServer<T> simpleServer);
}
