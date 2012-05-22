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
package com.phonefromhere.android.iax.frames.iax.ie;

public enum NetworkType {
    USER(0x00, "User Specified"),
    NATIONAL(0x10, "National Network Identification"),
    INTERNATIONAL(0x11, "International Network Identification");

    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: NetworkType.java,v 1.2 2011/02/10 16:16:25 uid1003 Exp $ Copyright Westhawk Ltd";

    private int _value;
    private String _name;

    private NetworkType(int value, String name) {
        _value = value;
        _name = name;
    }

    public int getValue() {
        return _value;
    }

    public String getName() {
        return _name;
    }

    @Override
    public String toString() {
        String str = "NetworkType: " + _name + "(" + _value + ")";
        return str;
    }
}
