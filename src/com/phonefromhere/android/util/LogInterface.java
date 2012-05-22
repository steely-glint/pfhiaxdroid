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


public interface LogInterface {
    static final String version_id = "@(#)$Id: LogInterface.java,v 1.3 2011/02/24 09:20:40 uid1003 Exp $ Copyright Westhawk Ltd";
    /**
     * IaxLog all text
     */
    public static int ALL = 9;

    /**
     * IaxLog IAX text (and down)
     */
    public static int SOUND = 7;
    
    /**
     * IaxLog IAX text (and down)
     */
    public static int IAX = 6;

    /**
     * IaxLog verbose text (and down)
     */
    public static int VERB = 5;

    /**
     * IaxLog debug text (and down)
     */
    public static int DEBUG = 4;

    /**
     * IaxLog info text (and down)
     */
    public static int INFO = 3;

    /**
     * IaxLog warning text (and down)
     */
    public static int WARN = 2;

    /**
     * IaxLog error text (and down)
     */
    public static int ERROR = 1;

    /**
     * IaxLog nothing
     */
    public static int NONE = 0;

    public void setBindHost(String bind_host);

    public void setVersion(String version);

    public void setTag(String tag);
    /**
     * Sets the level attribute of the IaxLog class
     * 
     * @param level
     *            The new level value
     */
    public void setLevel(int level);

    /**
     * Gets the level attribute of the IaxLog class
     * 
     * @return The level value
     */
    public int getLevel();

    public void setLogDatabase(boolean dolog);

    public boolean isLogDatabase();

    /**
     * error
     * 
     * @param string
     *            String
     */
    public void error(String string);
    public void error(Throwable exc);

    /**
     * warn
     * 
     * @param string
     *            String
     */
    public void warn(String string);

    /**
     * info
     * 
     * @param string
     *            String
     */
    public void info(String string);

    /**
     * debug
     * 
     * @param string
     *            Description of Parameter
     */
    public void debug(String string);
    public void debug(Throwable exc);

    /**
     * verbose
     * 
     * @param string
     *            Description of Parameter
     */
    public void verb(String string);

    /**
     * iax
     * 
     * @param string
     *            Description of Parameter
     */
    public void iax(String string);
    
    public void sound(String string);

    /**
     * where
     */
    public void where();

    public void database(Throwable throwable, Object object, String method_name);

    public void database(Throwable throwable, String class_name,
            String method_name);

}
