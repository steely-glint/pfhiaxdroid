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
package com.phonefromhere.android.codec;

/**
 * EncoderFace
 * 
 * This interface is to be implemented by the encoder instance of each codec.
 * 
 * @see CodecFace
 * @see DecoderFace
 * 
 * @author <a href="mailto:birgit@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.1 $ $Date: 2011/02/03 14:33:14 $
 */
public interface EncoderFace {

    static final String version_id = "@(#)$Id: EncoderFace.java,v 1.1 2011/02/03 14:33:14 uid1003 Exp $ Copyright Westhawk Ltd";

    /**
     * Encodes a (decoded) signal.
     * 
     * @param original_signal
     *            The original signal to encode
     * @return The encoded frame(s)
     */
    public byte[] encode_frame(short original_signal[]);
}
