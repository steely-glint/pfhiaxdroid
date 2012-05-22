/*
 * NAME
 *     $RCSfile: AndroidLog.java,v $
 * DESCRIPTION
 *      [given below in javadoc format]
 * DELTA
 *      $Revision: 1.3 $
 * CREATED
 *      $Date: 2011/02/24 12:43:19 $
 * COPYRIGHT
 *      Westhawk Ltd
 * TO DO
 *
 */
package com.phonefromhere.android;

import com.phonefromhere.android.util.IaxLog;
import com.phonefromhere.android.util.LogInterface;

import android.util.Log;

public class AndroidLog extends IaxLog implements LogInterface {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: AndroidLog.java,v 1.3 2011/02/24 12:43:19 uid1003 Exp $ Copyright Westhawk Ltd";

    private String _tag = "AndroidPhone";
    private static LogInterface _log = null;

    protected AndroidLog() {
    }

    public static LogInterface getLog() {
        if (_log == null) {
            _log = new AndroidLog();
            @SuppressWarnings("unused")
            IaxLog javaLog = (IaxLog) IaxLog.getLog();
            IaxLog.setLog(_log);
        }
        return _log;
    }

    public static void setLog(LogInterface log) {
        if (log != null) {
            _log = log;
            IaxLog.setLog(_log);
        }
    }

    @Override
    public void setTag(String tag) {
        _tag = tag;
    }

    @Override
    public void where() {
        Exception x = new Exception("Called From");
        x.printStackTrace();
        Log.i(_tag, x.getMessage(), x);
    }

    @Override
    public void error(Throwable exc) {
        if (_level >= ERROR) {
            Log.e(_tag, exc.getMessage(), exc);
        }
    }

    @Override
    public void debug(Throwable exc) {
        if (_level >= DEBUG) {
            Log.d(_tag, exc.getMessage(), exc);
        }
    }

    @Override
    protected void log(int level, String levelString, String message) {
        if (_level >= level) {
            // String logMessageShort = getMessage(message);
            String logMessageLong = getMessage(levelString, message);
            switch (level) {
                case NONE:
                    break;
                case ERROR:
                    Log.e(_tag, logMessageLong);
                    break;
                case WARN:
                    Log.w(_tag, logMessageLong);
                    break;
                case INFO:
                    Log.i(_tag, logMessageLong);
                    break;
                case DEBUG:
                    Log.d(_tag, logMessageLong);
                    break;
                case VERB:
                case IAX:
                case SOUND:
                case ALL:
                    Log.v(_tag, logMessageLong);
                    break;
                default:
                    Log.v(_tag, logMessageLong);
            }
        }
    }

    @Override
    protected void log(String level, String string) {
    }

}
