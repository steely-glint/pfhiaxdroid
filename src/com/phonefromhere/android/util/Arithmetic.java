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
package com.phonefromhere.android.util;

public class Arithmetic {
    @SuppressWarnings("unused")
    private static final String version_id = "@(#)$Id: Arithmetic.java,v 1.3 2011/02/17 13:15:30 uid1003 Exp $ Copyright Westhawk Ltd";

    /**
     * sResult = iVar:<br/>
     * Extract the 16 LSB bits of iVar to put in sResult.
     */
    public static short toShort(int iVar) {
        short sResult = (short) (iVar & 0x0000FFFF);
        return sResult;
    }

    public static byte toByte(char c) {
        byte b = (byte) (c & 0xFF);
        return b;
    }

    public static char toChar(byte b) {
        char c = (char) (b & 0xFF);
        return c;
    }

    public static short toShort(byte b) {
        short s = b;
        if (b < 0) {
            s += 128;
        }
        return s;
    }

    public static int log2(long x) {
        int log2 = (int) (Math.log(x) / Math.log(2));
        return log2;
    }

    /**
     * Copies a number of bits from input to output. Copy bits from left to
     * right (MSB - LSB).
     * 
     * @param input
     *            The input value to read from
     * @param in_noLSB
     *            The number of LSB bits in input to copy
     * @param output
     *            The output array to copy the bits to
     * @param out_pos
     *            The start position (from the left (MSB) in output
     * @return the updated out_pos
     */
    public static int copyBits(long input, int in_noLSB, byte output[],
            int out_pos) {
        long res;
        long value = input;

        // start with the left most bit I've got to copy over:
        long mask = ((long) 0x1) << (in_noLSB - 1);

        for (int i = 0; i < in_noLSB; i++) {
            // see if that bit is one or zero
            res = (value & mask);
            if (res > 0) {
                setBit(output, out_pos);
            }

            // shift the mask to the next position
            // shift with zero extension
            mask = mask >>> 1;
            out_pos++;
        }
        return out_pos;
    }

    /**
     * Copy a number of bits from input into a short. Copy bits from left to
     * right (MSB - LSB).
     * 
     * @param input
     *            The input array to read from
     * @param in_pos
     *            The position of the bit (in input) to start from
     * @param no_bits
     *            The number of bits to copy
     * @return The new value as a short
     */
    public static short copyBitsToShort(byte input[], int in_pos, int no_bits) {
        // a short is 16 bits
        short out_value = 0;

        if (no_bits <= Short.SIZE) {
            if (input != null) {
                // LSB is on the right hand side

                // start with the left most bit I've got to copy into:
                int out_value_mask = 0x1 << (no_bits - 1);

                int myBit;
                for (int b = 0; b < no_bits; b++) {
                    myBit = getBit(input, in_pos);
                    if (myBit > 0) {
                        // OR the bit into place, so the other bits remain
                        // undisturbed.
                        out_value |= out_value_mask;
                    }

                    // move to the next bit of input
                    in_pos++;

                    // get ready for the next bit of output
                    // shift with zero extension
                    out_value_mask = (short) (out_value_mask >>> 1);
                }
            }
        } else {
            throw new IllegalArgumentException("Trying to copy " + no_bits
                    + " into a short (" + Short.SIZE + "-bit)");
        }
        return out_value;
    }

    /**
     * Copy a number of bits from input into a short. Copy bits from left to
     * right (MSB - LSB).
     * 
     * @param input
     *            The input array to read from
     * @param in_pos
     *            The position of the bit (in input) to start from
     * @param no_bits
     *            The number of bits to copy
     * @return The new value as a short
     */
    public static int copyBitsToInt(byte input[], int in_pos, int no_bits) {
        // a short is 16 bits
        int out_value = 0;

        if (no_bits <= Integer.SIZE) {
            if (input != null) {
                // LSB is on the right hand side

                // start with the left most bit I've got to copy into:
                int out_value_mask = 0x1 << (no_bits - 1);

                int myBit;
                for (int b = 0; b < no_bits; b++) {
                    myBit = getBit(input, in_pos);
                    if (myBit > 0) {
                        // OR the bit into place, so the other bits remain
                        // undisturbed.
                        out_value |= out_value_mask;
                    }

                    // move to the next bit of input
                    in_pos++;

                    // get ready for the next bit of output
                    // shift with zero extension
                    out_value_mask = (short) (out_value_mask >>> 1);
                }
            }
        } else {
            throw new IllegalArgumentException("Trying to copy " + no_bits
                    + " into a int (" + Integer.SIZE + "-bit)");
        }
        return out_value;
    }

    /**
     * Copy a number of bits from input into a short. Copy bits from left to
     * right (MSB - LSB).
     * 
     * @param input
     *            The input array to read from
     * @param in_pos
     *            The position of the bit (in input) to start from
     * @param no_bits
     *            The number of bits to copy
     * @return The new value as a short
     */
    public static long copyBitsToLong(byte input[], int in_pos, int no_bits) {
        // a short is 16 bits
        long out_value = 0;

        if (no_bits <= Long.SIZE) {
            if (input != null) {
                // LSB is on the right hand side

                // start with the left most bit I've got to copy into:
                long out_value_mask = ((long) 0x1) << (no_bits - 1);

                int myBit;
                for (int b = 0; b < no_bits; b++) {
                    myBit = getBit(input, in_pos);
                    if (myBit > 0) {
                        // OR the bit into place, so the other bits remain
                        // undisturbed.
                        out_value |= out_value_mask;
                    }

                    // move to the next bit of input
                    in_pos++;

                    // get ready for the next bit of output
                    // shift with zero extension
                    out_value_mask = (out_value_mask >>> 1);
                }
            }
        } else {
            throw new IllegalArgumentException("Trying to copy " + no_bits
                    + " into a long (" + Long.SIZE + "-bit)");
        }
        return out_value;
    }

    /**
     * Returns zero or one.
     * 
     * @param input
     *            The input array to read from
     * @param bitno
     *            The position (from the left(MSB)) of the bit (in output)
     * @return one or zero
     */
    public static int getBit(byte input[], int bitno)
            throws java.lang.ArrayIndexOutOfBoundsException {
        // bit 0 is on the left hand side, if bit 0 should be set to '1'
        // this would show as: 1000 0000 = 0x80

        // each byte is 8 bits
        int index = bitno / 8;
        int index_bitno = bitno % 8;

        byte onebyte = input[index];

        // shift the '1' into the right place
        // shift with zero extension
        byte mask = (byte) (0x80 >>> index_bitno);

        // mask (AND) it so see if the bit is one or zero
        int res = (onebyte & mask);
        if (res < 0) {
            // it can be negative when testing the signed bit (bit zero
            // in this case)
            res = 1;
        }
        return res;
    }

    /**
     * Set bit number (bitno) to one. The bit numbering pretends the byte array
     * is one very long word.
     * 
     * @param output
     *            The output array to set the bit
     * @param bitno
     *            The position (from the left(MSB)) of the bit (in output)
     */
    public static void setBit(byte output[], int bitno)
            throws java.lang.ArrayIndexOutOfBoundsException {
        // bit 0 is on the left hand side, if bit 0 should be set to '1'
        // this would show as: 1000 0000 = 0x80

        // each byte is 8 bits
        int index = bitno / 8;
        int index_bitno = bitno % 8;

        // shift the '1' into the right place
        // shift with zero extension
        byte mask = (byte) (0x80 >>> index_bitno);

        // OR the bit into the byte, so the other bits remain
        // undisturbed.
        output[index] |= mask;
    }

    public static short[] bytesToShorts(byte byteBuffer[]) {
        int len = byteBuffer.length / 2;
        short[] output = new short[len];
        int j = 0;

        for (int i = 0; i < len; i++) {
            output[i] = (short) (byteBuffer[j++] << 8);
            output[i] |= (byteBuffer[j++] & 0xff);
        }
        return output;
    }

    public static byte[] shortsToBytes(short shortBuffer[]) {
        int len = shortBuffer.length;
        byte[] output = new byte[len * 2];
        int j = 0;

        for (int i = 0; i < len; i++) {
            output[j++] = (byte) (shortBuffer[i] >>> 8);
            output[j++] = (byte) (0xff & shortBuffer[i]);
        }
        return output;
    }

    public static void printData(byte[] data) {
        int octets = data.length;
        for (int i = 0; i < octets; i++) {
            printByte(data, i);
        }
    }

    public static String toHex(int val) {
        int val1, val2;

        val1 = (val >> 4) & 0x0F;
        val2 = (val & 0x0F);

        return ("" + HEX_DIGIT[val1] + HEX_DIGIT[val2]);
    }

    final static char[] HEX_DIGIT = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static void printByte(byte[] data, int pos) {
        byte b = data[pos];
        char c = toChar(b);
        String str = "";
        if (Character.isLetterOrDigit(c)) {
            str = " == " + c;
        }
        IaxLog.getLog().iax("data[" + pos + "] = 0x" + toHex(b) + str);
    }
    
    
}
