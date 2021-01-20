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

public enum CallingPresType {
    ALLOW_NOT(0x00, "Allowed user/number not screened"),
    ALLOW_PASSED(0x01, "Allowed user/number passed screen"),
    ALLOW_FAILED(0x02, "Allowed user/number failed screen"),
    ALLOW_NETWORK(0x03, "Allowed network number"),
    PROH_NOT(0x20, "Prohibited user/number not screened"),
    PROH_PASSED(0x21, "Prohibited user/number passed screen"),
    PROH_FAILED(0x22, "Prohibited user/number failed screen"),
    PROH_NETWORK(0x23, "Prohibited network number"),
    NOT_AVAIL(0x43, "Number not available");

    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: CallingPresType.java,v 1.2 2011/02/10 16:16:25 uid1003 Exp $ Copyright Westhawk Ltd";

    private int _value;
    private String _name;

    private CallingPresType(int value, String name) {
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
        String str = "CallingPresType: " + _name + "(" + _value + ")";
        return str;
    }
}
