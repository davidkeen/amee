/*
 * This file is part of AMEE.
 *
 * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
 *
 * AMEE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * AMEE is free software and is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Created by http://www.dgen.net.
 * Website http://www.amee.cc
 */
package com.amee.googleds;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;


/**
 * A servlet used to dynamically adjust package logging levels
 * while an application is running.  NOTE: This servlet is
 * only aware of pre-configured packages and packages that contain objects
 * that have logged at least one message since application startup.
 *
 */
public class Log4jConfigurationServlet extends HttpServlet {
  /**
   * The response content type: text/html
   */
  private static final String CONTENT_TYPE = "text/html";

  /**
   * The root appender.
   */
  private static final String ROOT = "Root";

  /**
   * The name of the class / package.
   */
  private static final String CLASS = "CLASS";

  /**
   * The logging level.
   */
  private static final String LEVEL = "LEVEL";

  /**
   * Print the status of all current <code>Logger</code>s and
   * an option to change their respective logging levels.
   *
   * @param request a <code>HttpServletRequest</code> value
   * @param response a <code>HttpServletResponse</code> value
   * @exception ServletException if an error occurs
   * @exception IOException if an error occurs
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    response.setContentType(CONTENT_TYPE);

    PrintWriter out = response.getWriter();
    List loggers = getSortedLoggers();
    Logger logger = null;
    String loggerName = null;
    int loggerNum = 0;

    // print title and header
    printHeader(out, request);

    out.println("<table width=\"50%\" border=\"1\">");
    out.println("<tr BGCOLOR=\"#5991A6\">");
    out.println(
      "<td><FONT  COLOR=\"BLACK\" FACE=\"Helvetica\"><B>Class</B></FONT></td>");
    out.print(
      "<td><FONT  COLOR=\"BLACK\" FACE=\"Helvetica\"><B>Level</B></FONT>");
    out.println("</td>");
    out.println("</tr>");

    // print the root Logger
    displayLogger(out, Logger.getRootLogger(), loggerNum++);

    // print the rest of the loggers
    Iterator ii = loggers.iterator();

    while (ii.hasNext()) {
      displayLogger(out, (Logger) ii.next(), loggerNum++);
    }

    out.println("</table>");
    out.println(
      "<FONT SIZE=\"-3\" COLOR=\"BLACK\" FACE=\"Helvetica\">* "
      + "Inherits LEVEL From Parent.</FONT><BR>");
    out.println("<A href=\"" + request.getRequestURI() + "\"><FONT  COLOR=\"BLACK\" SIZE=\"-2\" FACE=\"Helvetica\">Refresh</FONT></A><HR>");

    // print set options
    out.println(
      "<FORM action=\"" + request.getRequestURI() + "\" method=\"post\">");
    out.println(
      "<FONT COLOR=\"BLACK\" FACE=\"Helvetica\"><H3>"
      + "Set Log4J Option</H3><BR></FONT>");
    out.println("<FONT COLOR=\"BLACK\" FACE=\"Helvetica\">");
    out.println("<table width=\"50%\" border=\"1\">");
    out.println("<tr BGCOLOR=\"#5991A6\">");
    out.println(
      "<td><FONT COLOR=\"BLACK\" "
      + "FACE=\"Helvetica\"><B>Class Name:</B></FONT></td>");
    out.println("<td><SELECT name=\"CLASS\">");
    out.println("<OPTION VALUE=\"" + ROOT + "\">" + ROOT + "</OPTION>");

    ii = loggers.iterator();

    while (ii.hasNext()) {
      logger = (Logger) ii.next();
      loggerName = (logger.getName().equals("") ? "Root" : logger.getName());
      out.println(
        "<OPTION VALUE=\"" + loggerName + "\">" + loggerName + "</OPTION>");
    }

    out.println("</SELECT><BR></td></tr>");

    // print logging levels
    printLevelSelector(out);

    out.println("</table></FONT>");
    out.println(
      "<input type=\"submit\" name=\"Submit\" value=\"Set Option\"></FONT>");
    out.println("</FORM>");
    out.println("</body></html>");

    out.flush();
    out.close();
  }

  /**
   * Change a <code>Logger</code>'s level, then call <code>doGet</code>
   * to refresh the page.
   *
   * @param request a <code>HttpServletRequest</code> value
   * @param response a <code>HttpServletResponse</code> value
   * @exception ServletException if an error occurs
   * @exception IOException if an error occurs
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    String className = request.getParameter(CLASS);
    String level = request.getParameter(LEVEL);

    if (className != null) {
      setClass(className, level);
    }

    doGet(request, response);
  }

  /**
    Print a Logger and its current level.

    @param out the output writer.
    @param logger the logger to output.
    @param row the row number in the table this logger will appear in.*/
  private void displayLogger(
    PrintWriter out, Logger logger, int row) {
    String color = null;
    String loggerName =
      (logger.getName().equals("") ? ROOT : logger.getName());

    color = ((row % 2) == 1) ? "#E1E1E1" : "#FBFBFB";

    out.println("<tr BGCOLOR=\"" + color + "\">");
    out.println(
      "<td><FONT SIZE=\"-2\" COLOR=\"BLACK\" FACE=\"Helvetica\">" + loggerName
      + "</FONT></td>");
    out.println(
      "<td><FONT SIZE=\"-2\" COLOR=\"BLACK\" FACE=\"Helvetica\">"
      + ((logger.getLevel() == null)
      ? (logger.getEffectiveLevel().toString() + "*")
      : logger.getLevel().toString()) + "</FONT></td>");
    out.println("</tr>");
  }

  /**
    Set a logger's level.

    @param className class name of the logger to set.
    @param level the level to set the logger to.
    @return String return message for display. */
  private synchronized String setClass(String className, String level) {
    Logger logger = null;

    try {
      logger =
        (className.equals(ROOT)) ? Logger.getRootLogger()
                                 : Logger.getLogger(className);

      logger.setLevel(Level.toLevel(level));
    } catch (Exception e) {
      System.out.println("ERROR Setting LOG4J Logger:" + e);
    }

    return "Message Set For "
    + (logger.getName().equals("") ? ROOT : logger.getName());
  }

  /**
    Get a sorted list of all current loggers.

    @return List the list of sorted loggers. */
  private List getSortedLoggers() {
    Enumeration enumeration = LogManager.getCurrentLoggers();
    Comparator comp = new LoggerComparator();
    List list = new ArrayList();

    // Add all current loggers to the list
    while (enumeration.hasMoreElements()) {
      list.add(enumeration.nextElement());
    }

    // sort the loggers
    Collections.sort(list, comp);

    return list;
  }

  /**
   * Prints the page header.
   *
   * @param out The output writer
   * @param request The request
   */
  private void printHeader(PrintWriter out, HttpServletRequest request) {
    out.println(
      "<html><head><title>Log4J Control Console</title></head>"
      + "<body><H3><FONT FACE=\"Helvetica\">Log4J Control Console</FONT></H3>");

    /** Print log4j version/vendor information. */
    Package p = Layout.class.getPackage();
    out.println("<table><tr><td><FONT SIZE=\"-2\" COLOR=\"BLACK\" FACE=\"Helvetica\">");
    out.println("<tr><td><FONT SIZE=\"-2\" COLOR=\"BLACK\" FACE=\"Helvetica\">");
    out.println("Implementation version: "+p.getImplementationVersion());
    out.println("</FONT></td></tr>");
    out.println("</table>");
    out.println("<A href=\"" + request.getRequestURI() + "\"><FONT  COLOR=\"BLACK\" SIZE=\"-2\" FACE=\"Helvetica\">Refresh</FONT></A><HR>");
  }

  /**
   * Prints the Level select HTML.
   *
   * @param out The output writer
   */
  private void printLevelSelector(PrintWriter out) {
    out.println(
      "<tr BGCOLOR=\"#5991A6\"><td><FONT COLOR=\"BLACK\" "
      + "FACE=\"Helvetica\"><B>Level:</B></FONT></td>");
    out.println("<td><SELECT name=\"" + LEVEL + "\">");
    out.println(
      "<OPTION VALUE=\"" + Level.OFF + "\">" + Level.OFF + "</OPTION>");
    out.println(
      "<OPTION VALUE=\"" + Level.FATAL + "\">" + Level.FATAL + "</OPTION>");
    out.println(
      "<OPTION VALUE=\"" + Level.ERROR + "\">" + Level.ERROR + "</OPTION>");
    out.println(
      "<OPTION VALUE=\"" + Level.WARN + "\">" + Level.WARN + "</OPTION>");
    out.println(
      "<OPTION VALUE=\"" + Level.INFO + "\">" + Level.INFO + "</OPTION>");
    out.println(
      "<OPTION VALUE=\"" + Level.DEBUG + "\">" + Level.DEBUG + "</OPTION>");
    out.println(
      "<OPTION VALUE=\"" + Level.ALL + "\">" + Level.ALL + "</OPTION>");
    out.println("</SELECT><BR></td></tr>");
  }

  /**
   * Compare the names of two <code>Logger</code>s.  Used
   * for sorting.
   */
  private class LoggerComparator implements Comparator {
    /**
     * Compare the names of two <code>Logger</code>s.
     *
     * @param o1 an <code>Object</code> value
     * @param o2 an <code>Object</code> value
     * @return an <code>int</code> value
     */
    public int compare(Object o1, Object o2) {
      Logger logger1 = (Logger) o1;
      Logger logger2 = (Logger) o2;
      ;

      String logger1Name = null;
      String logger2Name = null;

      if (logger1 != null) {
        logger1Name = (logger1.getName().equals("") ? ROOT : logger1.getName());
      }

      if (logger2 != null) {
        logger2Name = (logger2.getName().equals("") ? ROOT : logger2.getName());
      }

      return logger1Name.compareTo(logger2Name);
    }

    /**
     * Return <code>true</code> if the <code>Object</code> is a
     * <code>LoggerComparator</code> instance.
     *
     *
     * @param o an <code>Object</code> value
     * @return a <code>boolean</code> value
     */
    public boolean equals(Object o) {
      return (o instanceof LoggerComparator);
    }

    /**
     * Returns the parent's hashcode.
     *
     * @return int The hashcode value
     */
     public int hashCode() {
       return super.hashCode();
     }
  }
}


//EOF
