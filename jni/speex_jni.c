#include "com_phonefromhere_android_codec_speex_NativeSpeexCodec.h"
#include <speex/speex_callbacks.h>
#include <speex/speex.h>


struct codec {
	void * speexDec;
	void * speexEnc;
	SpeexBits eBits;
	SpeexBits dBits;
}  ;

struct codec *getCodec(JNIEnv *env, jbyteArray jcodec){

     struct codec  *co;

     co = (struct codec *) (*env)->GetByteArrayElements(env, jcodec, 0);
     return (co);

}

void * releaseCodec(JNIEnv * env, jbyteArray jcodec, struct codec * co){
     (*env)->ReleaseByteArrayElements(env, jcodec, (jbyte *) co, 0);
}

/*
 * Class:     com_phonefromhere_android_codec_speex_NativeSpeexCodec
 * Method:    initCodec
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_phonefromhere_android_codec_speex_NativeSpeexCodec_initCodec
(JNIEnv *env, jobject this){
	
	jbyteArray jcodec;
	struct codec *co;
	
	
	jcodec = (*env)->NewByteArray(env, sizeof(struct codec));
	co = getCodec(env,jcodec);
	co->speexDec = speex_decoder_init(&speex_nb_mode);
	co->speexEnc = speex_encoder_init(&speex_nb_mode);
	int tmp=0;
	speex_encoder_ctl(co->speexEnc, SPEEX_SET_VBR, &tmp);
	tmp=5;
	speex_encoder_ctl(co->speexEnc, SPEEX_SET_QUALITY, &tmp);
	tmp=2;
	speex_encoder_ctl(co->speexEnc, SPEEX_SET_COMPLEXITY, &tmp);
	tmp = 8000;
	speex_encoder_ctl(co->speexEnc,SPEEX_SET_SAMPLING_RATE,&tmp);
	speex_bits_init(&(co->eBits));
	speex_bits_init(&(co->dBits));
	
	releaseCodec(env,jcodec,co);
	
	return jcodec;
}

/*
 * Class:     com_phonefromhere_android_codec_speex_NativeSpeexCodec
 * Method:    speexEncode
 * Signature: ([B[S[B)I
 */
JNIEXPORT jint JNICALL Java_com_phonefromhere_android_codec_speex_NativeSpeexCodec_speexEncode
  (JNIEnv * env, jobject this, jbyteArray jcodec, jshortArray jaudio, jbyteArray jwire){
	
	
	  struct codec *co;
	  jint nbBits = 0;
	  jshort *ip;
	  jbyte *offs;
	  
	  // memory faffing
	  co = getCodec(env,jcodec);
	  ip =  (*env)->GetShortArrayElements(env, jaudio, 0);
	  offs = (*env)->GetByteArrayElements(env, jwire, 0);
	  
	  speex_bits_reset(&(co->eBits));
	  speex_encode_int(co->speexEnc, ip, &(co->eBits));
	  nbBits = speex_bits_write(&(co->eBits),(char *) offs, 160);

	  // memory unfaffing
	  
	  (*env)->ReleaseShortArrayElements(env, jaudio, ip, 0);
	  (*env)->ReleaseByteArrayElements(env, jwire, offs, 0);
	  releaseCodec(env,jcodec,co);
	  return(nbBits);
}

/*
 * Class:     com_phonefromhere_android_codec_speex_NativeSpeexCodec
 * Method:    speexDecode
 * Signature: ([B[B[S)V
 */
JNIEXPORT void JNICALL Java_com_phonefromhere_android_codec_speex_NativeSpeexCodec_speexDecode
  (JNIEnv *env, jobject this, jbyteArray jcodec, jbyteArray jwire, jshortArray jaudio){
	  
	  struct codec *co;
	  jint nbBits = 0;
	  jshort *op;
	  jbyte *offs;
	  int len = 0;
	  
	  len = (*env)->GetArrayLength(env, jwire);
	  
	  // memory faffing
	  co = getCodec(env,jcodec);
	  op =  (*env)->GetShortArrayElements(env, jaudio, 0);
	  offs = (*env)->GetByteArrayElements(env, jwire, 0);
	  
	  speex_bits_reset(&(co->dBits));
		  
	  speex_bits_read_from(&(co->dBits), (char *) offs, len);
		  
	  speex_decode_int(co->speexDec, &(co->dBits), op);
	  
	  // memory unfaffing
	  
	  (*env)->ReleaseShortArrayElements(env, jaudio, op, 0);
	  (*env)->ReleaseByteArrayElements(env, jwire, offs, 0);
	  releaseCodec(env,jcodec,co);
}

/*
 * Class:     com_phonefromhere_android_codec_speex_NativeSpeexCodec
 * Method:    freeCodec
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_com_phonefromhere_android_codec_speex_NativeSpeexCodec_freeCodec
  (JNIEnv *env, jobject this, jbyteArray jcodec){
	  // dispose of the codec .....
}
