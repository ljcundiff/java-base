package com.artifylabs.server;

import com.artifylabs.app.AppProperties;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: lloyd
 * Date: 10/21/14
 * Time: 4:39 PM
 */
public class SimpleHttpProcess<T extends Enum> extends AbstractHandler implements ServerProcess<T>
{
  private static final Logger log = Logger.getLogger(SimpleHttpProcess.class);

  private Server server;
  private final T portEnum;

  public SimpleHttpProcess(T portEnum)
  {
    this.portEnum = portEnum;
  }

  @Override
  public void start(SimpleServer<T> simpleServer)
  {
    int port = simpleServer.getPropertyInt(portEnum , 8080);
    log.info(String.format("Starting HTTP process on port %d", port));
    server = new Server(port);
    server.setHandler(this);
    try
    {
      server.start();
    }
    catch (Exception e)
    {
      throw new RuntimeException("Error starting HTTP server", e);
    }
  }

  @Override
  public void stop(SimpleServer<T> simpleServer)
  {
    try
    {
      server.stop();
    }
    catch (Exception e)
    {
      throw new RuntimeException("Error stopping HTTP server", e);
    }
  }

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    log.info("Handled: " + baseRequest.getRequestURI());

    response.setContentType("text/plain");
    response.setStatus(HttpServletResponse.SC_OK);
    baseRequest.setHandled(true);
    response.getWriter().println("Requested: " + baseRequest.getRequestURI());
  }
}
