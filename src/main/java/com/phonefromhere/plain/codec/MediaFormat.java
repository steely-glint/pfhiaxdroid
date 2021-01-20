/*
 * Copyright 2011 Westhawk Ltd .
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
package com.phonefromhere.plain.codec;

/*
 * http://www.rfc-editor.org/rfc/rfc5456.txt
 *
 *
 8.7.  Media Formats

 Media Format Values

 +------------+-----------------+------------------------------------+
 | SUBCLASS   | DESCRIPTION     | LENGTH CALCULATION                 |
 +------------+-----------------+------------------------------------+
 | 0x00000001 | G.723.1         | 4-, 20-, and 24-byte frames of 240 |
 |            |                 | samples                            |
 |            |                 |                                    |
 | 0x00000002 | GSM Full Rate   | 33-byte chunks of 160 samples or   |
 |            |                 | 65-byte chunks of 320 samples      |
 |            |                 |                                    |
 | 0x00000004 | G.711 mu-law    | 1 byte per sample                  |
 |            |                 |                                    |
 | 0x00000008 | G.711 a-law     | 1 byte per sample                  |
 |            |                 |                                    |
 | 0x00000010 | G.726           |                                    |
 |            |                 |                                    |
 | 0x00000020 | IMA ADPCM       | 1 byte per 2 samples               |
 |            |                 |                                    |
 | 0x00000040 | 16-bit linear   | 2 bytes per sample                 |
 |            | little-endian   |                                    |
 |            |                 |                                    |
 | 0x00000080 | LPC10           | Variable size frame of 172 samples |
 |            |                 |                                    |
 | 0x00000100 | G.729           | 20-byte chunks of 172 samples      |
 |            |                 |                                    |
 | 0x00000200 | Speex           | Variable                           |
 |            |                 |                                    |
 | 0x00000400 | ILBC            | 50 bytes per 240 samples           |
 |            |                 |                                    |
 | 0x00000800 | G.726 AAL2      |                                    |
 |            |                 |                                    |
 | 0x00001000 | G.722           | 16 kHz ADPCM                       |
 |            |                 |                                    |
 | 0x00002000 | AMR             | Variable                           |
 |            |                 |                                    |
 | 0x00010000 | JPEG            |                                    |
 |            |                 |                                    |
 | 0x00020000 | PNG             |                                    |
 |            |                 |                                    |
 | 0x00040000 | H.261           |                                    |
 |            |                 |                                    |
 | 0x00080000 | H.263           |                                    |
 |            |                 |                                    |
 | 0x00100000 | H.263p          |                                    |
 |            |                 |                                    |
 | 0x00200000 | H.264           |                                    |
 +------------+-----------------+------------------------------------+
 *
 */

public enum MediaFormat {
    G723_1(0x00000001, "G.723.1"),
    GSM(0x00000002, "GSM Full Rate"),
    ULAW(0x00000004, "G.711 mu-law"),
    ALAW(0x00000008, "G.711 a-law"),
    G726(0x00000010, "G.726"),
    IMA_ADPCMX(0x00000020, "IMA ADPCM"),
    LIN_16B(0x00000040, "16-bit linear little-endian"),
    LCP10(0x00000080, "LPC10"),
    G729(0x00000100, "G.729"),
    SPEEX(0x00000200, "Speex"),
    ILBC(0x00000400, "ILBC"),
    G726_AAL2(0x00000800, "G.726 AAL2"),
    G722(0x00001000, "G.722"),
    AMR(0x00002000, "AMR"),
    JPEG(0x00010000, "JPEG"),
    PNG(0x00020000, "PNG"),
    H261(0x00040000, "H.261"),
    H263(0x00080000, "H.263"),
    H263P(0x00100000, "H.263p"),
    H264(0x00200000, "H.264"),
    OPUS((1L << 34),"OPUS");

    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: MediaFormat.java,v 1.1 2011/02/03 14:33:14 uid1003 Exp $ Copyright Westhawk Ltd";

    private long _value;
    private String _name;

    private MediaFormat(long value, String name) {
        _value = value;
        _name = name;
    }

    public long getValue() {
        return _value;
    }

    public String getName() {
        return _name;
    }

    @Override
    public String toString() {
        String str = "MediaFormat: " + _name + "(0x" + Long.toHexString(_value)
                + ")";
        return str;
    }
}
