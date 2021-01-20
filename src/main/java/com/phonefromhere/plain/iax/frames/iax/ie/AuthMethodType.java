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
package com.phonefromhere.plain.iax.frames.iax.ie;

public enum AuthMethodType {
    PLAINTEXT(0x0001, "Plain text"),
    MD5(0x0002, "MD5"),
    RSH(0x0004, "RSH");

    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: AuthMethodType.java,v 1.1 2011/02/10 16:16:25 uid1003 Exp $ Copyright Westhawk Ltd";

    private int _value;
    private String _name;

    private AuthMethodType(int value, String name) {
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
        String str = this.getClass().getSimpleName() + ": " + _name + "("
                + _value + ")";
        return str;
    }
}
