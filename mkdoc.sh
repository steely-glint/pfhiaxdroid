#!/bin/sh
cd src
javadoc -bootclasspath /Users/tim/android-sdk-mac_x86/platforms/android-7/android.jar -d ../javadoc com/phonefromhere/android/iax/AndroidPhoneIax.java com/phonefromhere/android/audio/AndroidAudio.java  com/phonefromhere/softphone/PhoneListener.java com/phonefromhere/softphone/AudioFace.java
