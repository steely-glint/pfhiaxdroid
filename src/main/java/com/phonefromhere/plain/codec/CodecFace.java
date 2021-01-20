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

import com.phonefromhere.plain.codec.DecoderFace;
import com.phonefromhere.plain.codec.EncoderFace;

/**
 * CodecFace
 * 
 * This is the interface to be implemented by each codec to be supported by this
 * IAX implementation.
 * 
 * @see DecoderFace
 * @see EncoderFace
 * 
 * @author <a href="mailto:birgit@westhawk.co.uk">Birgit Arkesteijn</a>
 * @version $Revision: 1.1 $ $Date: 2011/02/03 14:33:14 $
 */
public interface CodecFace {

    static final String version_id = "@(#)$Id: CodecFace.java,v 1.1 2011/02/03 14:33:14 uid1003 Exp $ Copyright Westhawk Ltd";

    /**
     * Returns "Media Format Subclass Values" of this Codec. This should be one
     * of the constants above.
     * 
     * @return The codec, as specified in the IAX RFC
     */
    public MediaFormat getCodec();

    /**
     * Returns the optimum size (in bytes) of a frame. eg:
     * <ol>
     * <li>33 bytes for GSM</li>
     * <li>320 for SLIN</li>
     * <li>160 for [ua]law</li>
     * </ol>
     * 
     * @return The frame size in bytes
     */
    public int getFrameSize();

    /**
     * Returns the frame interval in milliseconds, normally 20.
     * 
     * @return The frame interval in milliseconds
     */
    public int getFrameInterval();

    /**
     * Returns the sample rate, for example "8000.0F" for SLin.
     * 
     * @return The sample rate
     */
    public float getSampleRate();

    /**
     * Returns an instance of the Decoder class of this Codec.
     * 
     * @return An instance of the appropriate Decoder class
     */
    public DecoderFace getDecoder();

    /**
     * Returns an instance of the Encoder class of this Codec.
     * 
     * @return An instance of the appropriate Encoder class
     */
    public EncoderFace getEncoder();

    /**
     * Returns a string representation.
     * 
     * @return The string, representing this object
     */
    @Override
    public String toString();
}
