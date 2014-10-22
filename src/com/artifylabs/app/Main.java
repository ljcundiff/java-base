package com.artifylabs.app;

import com.artifylabs.server.SimpleHttpProcess;
import com.artifylabs.server.SimpleServerFactory;

/**
 * User: lloyd
 * Date: 10/21/14
 * Time: 4:25 PM
 */
public class Main
{
  public static void main(String[] args)
  {
    new SimpleServerFactory<AppProperties>(AppProperties.class)
            .withProcess("WEB", new SimpleHttpProcess<AppProperties>(AppProperties.PORT))
            .create()
            .execute();
  }

}
