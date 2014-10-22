package com.artifylabs.server;

import java.util.LinkedHashMap;

/**
 * User: lloyd
 * Date: 10/21/14
 * Time: 3:47 PM
 */
public class SimpleServerFactory<T extends Enum>
{
  private final LinkedHashMap<String, ServerProcess> processes;
  private int waitOnShutdown;
  private Class<? extends Enum> propertiesEnum;

  public SimpleServerFactory(Class<? extends Enum> propertiesEnum)
  {
    this.propertiesEnum = propertiesEnum;
    this.processes = new LinkedHashMap<String, ServerProcess>();
    this.waitOnShutdown = 10;
  }

  public SimpleServerFactory withProcess(String processName, ServerProcess process)
  {
    processes.put(processName, process);
    return this;
  }

  public SimpleServerFactory withWaitOnShutdown(int waitTimeInSeconds)
  {
    if (waitTimeInSeconds < 0 || waitTimeInSeconds > 60)
    {
      throw new IllegalArgumentException("Invalid wait on shutdown time.  Must be 0..60");
    }
    waitOnShutdown = waitTimeInSeconds;
    return this;
  }

  public SimpleServer create()
  {
    return new SimpleServer<T>(propertiesEnum, processes, waitOnShutdown);
  }
}
