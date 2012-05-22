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
package com.phonefromhere.android.iax.frames.control;

/*
 * http://www.rfc-editor.org/rfc/rfc5456.txt
 * http://www.iana.org/assignments/iax-parameters/iax-parameters.xml

 8.3.  Control Frames Subclasses

 The following table specifies valid Control Frame Subclasses:

 */

public enum ControlSubclass {
    HANGUP(0x01, "Hangup"),
    RESERVED1(0x02, "Reserved1"),
    RINGING(0x03, "Ringing"),
    ANSWER(0x04, "Answer"),
    BUSY(0x05, "Busy"),
    RESERVED2(0x06, "Reserved2"),
    RESERVED3(0x07, "Reserved3"),
    CONGESTION(0x08, "Congestion"),
    FLASH_HOOK(0x09, "Flash Hook"),
    RESERVED4(0x0a, "Reserved4"),
    OPTION(0x0b, "Option"),
    KEY_RADIO(0x0c, "Key Radio"),
    UNKEY_RADIO(0x0d, "Unkey Radio"),
    CALL_PROGRESS(0x0e, "Call Progress"),
    CALL_PROCEEDING(0x0f, "Call Proceeding"),
    HOLD(0x10, "Hold"),
    UNHOLD(0x11, "Unhold"),
    VIDUPDATE(0x12, "Video frame update"),
    T38(0x13, "T38 state change"),
    SRCUPDATE(0x14, "Source of media changed"),
    STOPSOUNDS(255, "Stop Sounds");

    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: ControlSubclass.java,v 1.3 2011/03/06 15:10:50 uid1003 Exp $ Copyright Westhawk Ltd";

    private int _value;
    private String _name;

    private ControlSubclass(int value, String name) {
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
        String str = "ControlSubclass: " + _name + "(0x"
                + Integer.toHexString(_value) + ")";
        return str;
    }
}
