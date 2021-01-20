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
package com.phonefromhere.plain.iax.frames;

/*
 * http://www.rfc-editor.org/rfc/rfc5456.txt
 * http://www.iana.org/assignments/iax-parameters/iax-parameters.xml
 * 
 8.2.  Frame Types

 The IAX protocol specifies 10 types of possible frames for the
 "frametype" field of a Full Frame.  They are described in the
 following subsections.

 The following table specifies valid Frame Type Values:

 +------+-------------+--------------------------+-------------------+
 | TYPE | Description | Subclass Description     | Data Description  |
 +------+-------------+--------------------------+-------------------+
 | 0x01 | DTMF        | 0-9, A-D, *, #           | Undefined         |
 |      |             |                          |                   |
 | 0x02 | Voice       | Audio Compression Format | Data              |
 |      |             |                          |                   |
 | 0x03 | Video       | Video Compression Format | Data              |
 |      |             |                          |                   |
 | 0x04 | Control     | See Control Frame Types  | Varies with       |
 |      |             |                          | subclass          |
 |      |             |                          |                   |
 | 0x05 | Null        | Undefined                | Undefined         |
 |      |             |                          |                   |
 | 0x06 | IAX Control | See IAX Protocol         | Information       |
 |      |             | Messages                 | Elements          |
 |      |             |                          |                   |
 | 0x07 | Text        | Always 0                 | Raw Text          |
 |      |             |                          |                   |
 | 0x08 | Image       | Image Compression Format | Raw image         |
 |      |             |                          |                   |
 | 0x09 | HTML        | See HTML Frame Types     | Message Specific  |
 |      |             |                          |                   |
 | 0x0A | Comfort     | Level in -dBov of        | None              |
 |      | Noise       | comfort noise            |                   |
 +------+-------------+--------------------------+-------------------+

 Refer to the IANA Registry for additional IAX Frame Type values.

 */

public enum FrameType {
    DTMF(0x01, "DTMF"),
    VOICE(0x02, "Voice"),
    VIDEO(0x03, "Video"),
    CONTROL(0x04, "Control"),
    NULL(0x05, "Null"),
    IAX(0x06, "IAX Control"),
    TEXT(0x07, "Text"),
    IMAGE(0x08, "Image"),
    HTML(0x09, "HTML"),
    COMFORT(0x0A, "Comfort Noise");

    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: FrameType.java,v 1.1 2011/02/03 14:33:15 uid1003 Exp $ Copyright Westhawk Ltd";

    private int _value;
    private String _name;

    private FrameType(int value, String name) {
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
        String str = "FrameType: " + _name + "(0x"
                + Integer.toHexString(_value) + ")";
        return str;
    }
}
