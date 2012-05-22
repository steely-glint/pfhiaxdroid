/*
 * Copyright 2011 Westhawk Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.phonefromhere.android.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

/**
 * A simple logger.
 * 
 * @author <a href="mailto:thp@westhawk.co.uk">Tim Panton</a>
 * @version $Revision: 1.4 $ $Date: 2011/03/07 14:03:59 $
 */
public class IaxLog implements LogInterface {
    @SuppressWarnings("unused")
    private final static String version_id = "@(#)$Id: IaxLog.java,v 1.4 2011/03/07 14:03:59 uid1003 Exp $ Copyright Westhawk Ltd";

    private final static String POST_URL = "http://error.phonefromhere.com/digium/error_post.xsql";

    protected int _level = 1;
    private boolean _doLogDatabase = true;
    private URL _post_url = null;

    // To log in database:
    private String _stack_version = "?";
    private String _bind_host = "?";
    private String _local_hostname = "?";
    private String _http_agent = "?";
    private String _java_version = "?";
    private String _java_vendor = "?";
    private String _os_arch = "?";
    private String _os_name = "?";
    private String _os_version = "?";
    private String _user_country = "?";
    private String _user_language = "?";

    private static LogInterface _me;

    /**
     * Constructor for the IaxLog object
     */
    protected IaxLog() {
        if (_me == null) {
            _me = this;
        }
    }

    public static LogInterface getLog() {
        if (_me == null) {
            _me = new IaxLog();
        }
        return _me;
    }

    public static void setLog(LogInterface log) {
        if (log != null) {
            _me = log;
        }
    }

    @Override
    public void setBindHost(String bind_host) {
        _bind_host = bind_host;
    }

    @Override
    public void setVersion(String version) {
        _stack_version = version;
    }

    /**
     * Sets the level attribute of the IaxLog class
     * 
     * @param level
     *            The new level value
     */
    @Override
    public void setLevel(int level) {
        _level = level;

        // IP address (browser)
        // http.agent = Mozilla/4.0 (Linux 2.6.13-15.18-smp)
        // java.version = 1.5.0_06
        // java.vendor = Sun Microsystems Inc.
        // os.arch = i386
        // os.name = Linux
        // os.version = 2.6.13-15.18-smp
        // user.country = GB
        // user.language = en

        try {
            InetAddress inetA = InetAddress.getLocalHost();
            _local_hostname = inetA.getHostName();
        } catch (UnknownHostException ex) {
        }

        try {
            _http_agent = System.getProperty("http.agent");
            _java_version = System.getProperty("java.version");
            _java_vendor = System.getProperty("java.vendor");
            _os_arch = System.getProperty("os.arch");
            _os_name = System.getProperty("os.name");
            _os_version = System.getProperty("os.version");
            _user_country = System.getProperty("user.country");
            _user_language = System.getProperty("user.language");
        } catch (java.lang.SecurityException exc) {
        }

        if (_post_url == null) {
            try {
                _post_url = new URL(POST_URL);
            } catch (MalformedURLException ex) {
                _me.debug("IaxLog.setLevel(): MalformedURLException: "
                        + ex.getMessage());
            }
        }
    }

    /**
     * Gets the level attribute of the IaxLog class
     * 
     * @return The level value
     */
    @Override
    public int getLevel() {
        return _level;
    }

    @Override
    public void setLogDatabase(boolean dolog) {
        _doLogDatabase = dolog;
    }

    @Override
    public boolean isLogDatabase() {
        return _doLogDatabase;
    }

    /**
     * error
     * 
     * @param string
     *            String
     */
    @Override
    public void error(String string) {
        log(ERROR, "ERROR", string);
    }

    @Override
    public void error(Throwable exc) {
        log(ERROR, "ERROR", exc.getMessage());
    }

    /**
     * warn
     * 
     * @param string
     *            String
     */
    @Override
    public void warn(String string) {
        log(WARN, "WARN", string);

    }

    /**
     * info
     * 
     * @param string
     *            String
     */
    @Override
    public void info(String string) {
        log(INFO, "INFO", string);
    }

    /**
     * debug
     * 
     * @param string
     *            Description of Parameter
     */
    @Override
    public void debug(String string) {
        log(DEBUG, "DEBUG", string);
    }

    @Override
    public void debug(Throwable exc) {
        log(DEBUG, "DEBUG", exc.getMessage());
    }

    /**
     * verbose
     * 
     * @param string
     *            Description of Parameter
     */
    @Override
    public void verb(String string) {
        log(VERB, "VERB", string);
    }

    /**
     * iax
     * 
     * @param string
     *            Description of Parameter
     */
    @Override
    public void iax(String string) {
        log(IAX, "IAX", string);
    }
    
    @Override
    public void sound(String string) {
        log(SOUND, "SOUND", string);
    }


    /**
     * where
     */
    @Override
    public void where() {
        Exception x = new Exception("Called From");
        debug(x);
    }

    @Override
    public void database(Throwable throwable, Object object, String method_name) {
        String class_name = object.getClass().getName();
        database(throwable, class_name, method_name);
    }

    @Override
    public void database(Throwable throwable, String class_name,
            String method_name) {
        if (_doLogDatabase && _post_url != null) {
            // call sendMessge on a new thread some how....
            Runner r = new Runner(throwable, class_name, method_name);
            r.start();
        }
    }

    void sendMessage(Throwable throwable, String class_name, String method_name) {
        String exception_class = throwable.getClass().getName();

        String exception_message = throwable.getMessage();

        _me.debug("IaxLog.database():");
        _me.debug("Stack Version = " + _stack_version);
        _me.debug("Bind Host = " + _bind_host);
        _me.debug("Local Hostname = " + _local_hostname);
        _me.debug("Http Agent = " + _http_agent);
        _me.debug("Java Version = " + _java_version);
        _me.debug("Java Vendor = " + _java_vendor);
        _me.debug("Os Arch = " + _os_arch);
        _me.debug("Os Name = " + _os_name);
        _me.debug("Os Version = " + _os_version);
        _me.debug("User Country = " + _user_country);
        _me.debug("User Lang = " + _user_language);

        _me.debug("Exc Class = " + exception_class);
        _me.debug("Exc Message = " + exception_message);
        _me.debug("Class Name = " + class_name);
        _me.debug("Method Name = " + method_name);

        StringBuffer buf = new StringBuffer();
        buf.append("stack_version=").append(_stack_version);
        buf.append("&bind_host=").append(_bind_host);
        buf.append("&local_hostname=").append(_local_hostname);
        buf.append("&http_agent=").append(_http_agent);
        buf.append("&java_version=").append(_java_version);
        buf.append("&java_vendor=").append(_java_vendor);
        buf.append("&os_arch=").append(_os_arch);
        buf.append("&os_name=").append(_os_name);
        buf.append("&os_version=").append(_os_version);
        buf.append("&user_country=").append(_user_country);
        buf.append("&user_language=").append(_user_language);
        buf.append("&exception_class=").append(exception_class);
        buf.append("&exception_message=").append(exception_message);
        buf.append("&class_name=").append(class_name);
        buf.append("&method_name=").append(method_name);

        BufferedReader inStream = postToUrl(_post_url, buf.toString());
        String line;
        if (inStream != null) {
            try {
                line = inStream.readLine();
                _me.debug(line);
                while (line != null) {
                    line = inStream.readLine();
                    _me.debug(line);
                }
            } catch (IOException ex) {
                _me.debug("IaxLog.database(): IOException: " + ex.getMessage());
            }
            try {
                inStream.close();
            } catch (IOException ex) {
                _me.debug("IaxLog.database(): IOException: " + ex.getMessage());
            }
        }
    }

    private class Runner extends Thread {
        Throwable _throwable;
        String _class_name;
        String _method_name;

        Runner(Throwable throwable, String class_name, String method_name) {
            _throwable = throwable;
            _class_name = class_name;
            _method_name = method_name;
        }

        @Override
        public void run() {
            sendMessage(_throwable, _class_name, _method_name);
        }
    }

    /**
     * Sends a command to an URL via the POST method. The command should have
     * the following format: k1=v1&amp;k2=v2&amp;k3=v3
     * 
     * @return the input stream from the URL to read the answer
     */
    private BufferedReader postToUrl(URL url, String command) {
        BufferedReader inStream = null;

        try {
            URLConnection conn;
            conn = url.openConnection();

            // No cache may be used, else we might get an old value !!
            conn.setUseCaches(false);

            // After long fiddling with Netscape, I found out that the
            // content-type has to be set, else the servlet somehow cannot
            // decode the parameters!!!!
            conn.setRequestProperty("Content-type",
                    "application/x-www-form-urlencoded");

            // Use the POST method
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // Write the parameters to the URL
            PrintWriter outStream;
            outStream = new PrintWriter(conn.getOutputStream(), true);
            outStream.write(command);
            outStream.close();

            // Read the answer
            inStream = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
        } catch (IOException e) {
            _me.debug("IaxLog.database(): IOException: " + e.getMessage());
            _doLogDatabase = false;
        }
        return inStream;
    }

    protected void log(int level, String levelString, String message) {
        if (_level >= level) {
            log(levelString, message);
        }
    }

    protected void log(String level, String string) {
        String message = getMessage(level, string);
        System.out.println(message);
    }

    protected String getMessage(String level, String string) {
        StringBuffer buf = new StringBuffer();
        buf.append(level).append(": ");
        buf.append(getMessage(string));
        return buf.toString();
    }

    protected String getMessage(String string) {
        StringBuffer buf = new StringBuffer();
        buf.append(System.currentTimeMillis());
        buf.append(" ").append(Thread.currentThread().getName());
        buf.append(" -> ").append(string);
        return buf.toString();
    }

    @Override
    public void setTag(String tag) {
    }

}
