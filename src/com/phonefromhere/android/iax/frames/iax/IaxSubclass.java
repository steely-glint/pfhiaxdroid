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
package com.phonefromhere.android.iax.frames.iax;

/*
 * http://www.rfc-editor.org/rfc/rfc5456.txt
 * http://www.iana.org/assignments/iax-parameters/iax-parameters.xml
 * 
 8.4.  IAX Frames

 Frames of type 'IAX' are used to provide management of IAX endpoints.
 They handle IAX signalling (e.g., call setup, maintenance, and tear-
 down).  They MAY also handle direct transmission of media data, but
 this is not optimal for VoIP calls.  They do not carry session-
 specific control (e.g., device state), as this is the purpose of
 Control Frames.  The IAX commands are listed and described below.

 */

public enum IaxSubclass {
    NEW(0x01, "NEW"),
    PING(0x02, "PING"),
    PONG(0x03, "PONG"),
    ACK(0x04, "ACK"),
    HANGUP(0x05, "HANGUP"),
    REJECT(0x06, "REJECT"),
    ACCEPT(0x07, "ACCEPT"),
    AUTHREQ(0x08, "AUTHREQ"),
    AUTHREP(0x09, "AUTHREP"),
    INVAL(0x0a, "INVAL"),
    LAGRQ(0x0b, "LAGRQ"),
    LAGRP(0x0c, "LAGRP"),
    REGREQ(0x0d, "REGREQ"),
    REGAUTH(0x0e, "REGAUTH"),
    REGACK(0x0f, "REGACK"),
    REGREJ(0x10, "REGREJ"),
    REGREL(0x11, "REGREL"),
    VNAK(0x12, "VNAK"),
    DPREQ(0x13, "DPREQ"),
    DPREP(0x14, "DPREP"),
    DIAL(0x15, "DIAL"),
    TXREQ(0x16, "TXREQ"),
    TXCNT(0x17, "TXCNT"),
    TXACC(0x18, "TXACC"),
    TXREADY(0x19, "TXREADY"),
    TXREL(0x1a, "TXREL"),
    TXREJ(0x1b, "TXREJ"),
    QUELCH(0x1c, "QUELCH"),
    UNQUELCH(0x1d, "UNQUELCH"),
    POKE(0x1e, "POKE"),
    RESERVED1(0x1f, "RESERVED1"),
    MWI(0x20, "MWI"),
    UNSUPPORT(0x21, "UNSUPPORT"),
    TRANSFER(0x22, "TRANSFER"),
    RESERVED2(0x23, "RESERVED2"),
    RESERVED3(0x24, "RESERVED3"),
    RESERVED4(0x25, "RESERVED4"),
    TXMEDIA(0x26, "TXMEDIA"),
    RTKEY(0x27, "RTKEY"),
    CALLTOKEN(0x28, "CALLTOKEN");

    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: IaxSubclass.java,v 1.1 2011/02/03 14:33:15 uid1003 Exp $ Copyright Westhawk Ltd";

    private int _value;
    private String _name;

    private IaxSubclass(int value, String name) {
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
        String str = "IaxSubclass: " + _name + "(0x"
                + Integer.toHexString(_value) + ")";
        return str;
    }

}
