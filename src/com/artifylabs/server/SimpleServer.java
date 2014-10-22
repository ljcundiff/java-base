package com.artifylabs.server;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;

/**
 * User: lloyd
 * Date: 10/21/14
 * Time: 3:40 PM
 */
public class SimpleServer<T extends Enum>
{
  private static final Logger log = Logger.getLogger(SimpleServer.class);

  private final LinkedHashMap<String, ServerProcess> serverProcesses;
  private final LinkedHashMap<String, ServerProcess> serverProcessesShutdownStack;
  private Thread thread;
  private boolean running;
  private final int waitSeconds;
  private final Properties properties;

  protected SimpleServer(Class<? extends Enum> propertiesEnum, LinkedHashMap<String, ServerProcess> serverProcesses, int waitSeconds)
  {
    this.serverProcesses = new LinkedHashMap<String, ServerProcess>(serverProcesses);
    this.serverProcessesShutdownStack = new LinkedHashMap<String, ServerProcess>();
    this.running = false;
    this.waitSeconds = waitSeconds;

    this.properties = new Properties();

    // Next from ENV and System properties in that order
    for (Enum e :  propertiesEnum.getEnumConstants())
    {
      if (System.getenv().containsKey(e.name()))
      {
        properties.setProperty(e.name(), System.getenv().get(e.name()));
        log.debug(String.format("Adding property from ENV: %s = %s", e.name(), sanitizedValue(e.name())));
      }
      else if (System.getProperties().containsKey(e.name()))
      {
        properties.setProperty(e.name(), System.getProperty(e.name()));
        log.debug(String.format("Adding property from System Properties: %s = %s", e.name(), sanitizedValue(e.name())));
      }
    }
  }

  public String getProperty(T e)
  {
    if (!properties.containsKey(e.name()))
    {
      throw new RuntimeException("Property not defined: " + e.name());
    }
    return properties.getProperty(e.name());
  }

  public String getProperty(T e, String defaultValue)
  {
    String value = properties.getProperty(e.name());
    return value == null ? defaultValue : value;
  }

  public Integer getPropertyInt(T e)
  {
    String value = getProperty(e);
    return Integer.decode(value);
  }

  public Integer getPropertyInt(T e, int defaultValue)
  {
    String value = getProperty(e, Integer.toString(defaultValue));
    return Integer.decode(value);
  }

  private void logEnvironment()
  {
    RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    List<String> arguments = runtimeMxBean.getInputArguments();

    log.info("******************** JVM Arguments ************************");
    for (String arg : arguments)
    {
      log.info("* " + arg);
    }
    log.info("***********************************************************");

    List<String> keys =  new ArrayList<String>(properties.stringPropertyNames());
    Collections.sort(keys);
    log.info("******************** Environment **************************");
    for (String key : keys)
    {
      String value = sanitizedValue(key);properties.getProperty(key);
      if (key.contains("_PWD") || key.contains("_SECRET") || key.contains("_PASSWORD"))
      {
        value = "xxxxxxxxxx";
      }
      log.info("* " + key + " = " + value);
    }
    log.info("***********************************************************");
  }

  private String sanitizedValue(String key)
  {
    if (key.contains("_PWD") || key.contains("_SECRET") || key.contains("_PASSWORD"))
    {
      return "xxxxxxxxxx";
    }
    return properties.getProperty(key);
  }

  public void execute()
  {
    logEnvironment();

    log.info("SimpleServer process starting...");

    synchronized (this)
    {
      if (running)
      {
        throw new RuntimeException("Attempt to execute running server");
      }
      running = true;
    }

    // Start the server processes
    try
    {
      for (String processName : serverProcesses.keySet())
      {
        log.info(String.format("Starting process: %s", processName));
        ServerProcess process = serverProcesses.get(processName);
        process.start(this);
        serverProcessesShutdownStack.put(processName, process);
      }
    }
    catch (Exception ex)
    {
      stopProcesses();
      throw new RuntimeException("Error starting server processes", ex);
    }

    // Register a shutdown hook that will stop the processes
    Runtime.getRuntime().addShutdownHook(new Thread(new ServerShutdown()));

    // Create a thread that will run forever (well 100 years to be exact)
    // To terminate the app use System.exit(0)
    thread = new Thread(new Runnable() {
      public void run()
      {
        try
        {
          Thread.sleep(3153600000000l);
        }
        catch (InterruptedException e)
        {
          // Do nothing
        }
        log.info("Framework thread exiting...");
      }
    });
    thread.start();
  }

  private void stopProcesses()
  {
    // Stop processes in reverse order
    List<String> list = new ArrayList<String>(serverProcessesShutdownStack.keySet());
    ListIterator<String> iter = list.listIterator(list.size());
    while(iter.hasPrevious())
    {
      String processName = iter.previous();
      log.info(String.format("Stopping process: %s", processName));
      serverProcessesShutdownStack.get(processName).stop(this);
    }
  }

  private class ServerShutdown implements Runnable
  {
    public ServerShutdown()
    {
    }

    public void run()
    {
      log.info("SimpleServer process stopping...");
      stopProcesses();
      thread.interrupt();
      try
      {
        thread.join(waitSeconds * 1000);
      }
      catch (InterruptedException e)
      {
        // do nothing
      }
      log.info("SimpleServer shutdown complete");
    }
  }
}
