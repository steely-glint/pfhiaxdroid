/*
 * Copyright 2011 Voxeo Corp.
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

/**
 * DecoderFace
 * 
 * This interface is to be implemented by the decoder instance of each codec.
 * 
 * @see CodecFace
 * @see EncoderFace
 * 
 * @author <a href="mailto:birgit@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.1 $ $Date: 2011/02/03 14:33:14 $
 */
public interface DecoderFace {

    static final String version_id = "@(#)$Id: DecoderFace.java,v 1.1 2011/02/03 14:33:14 uid1003 Exp $ Copyright Westhawk Ltd";

    /**
     * Decodes an (encoded) frame.
     * 
     * @param encoded_signal
     *            The encoded frame(s)
     * @return The decoded frame
     */
    public short[] decode_frame(byte encoded_signal[]);

    /**
     * Try to magic up a lost frame from what we have
     * 
     * @param current_frame
     * @param next_frame
     * @return A made up frame, that is still encoded.
     */
    public byte[] lost_frame(byte current_frame[], byte next_frame[]);
}
