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

public enum CallingTonType {
    UNKNOWN(0x00, "Unknown"),
    INTERNATIONAL(0x10, "International Number"),
    NATIONAL(0x20, "National Number"),
    NETWORK(0x30, "Network Specific Number"),
    SUBSCRIBER(0x40, "Subscriber Number"),
    ABBR(0x60, "Abbreviated Number"),
    RESERVED(0x70, "Reserved for extension"), ;

    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: CallingTonType.java,v 1.1 2011/02/03 14:33:15 uid1003 Exp $ Copyright Westhawk Ltd";

    private int _value;
    private String _name;

    private CallingTonType(int value, String name) {
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
        String str = "CallingTonType: " + _name + "(" + _value + ")";
        return str;
    }
}
