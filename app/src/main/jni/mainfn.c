#include"my_flite_hts.h"
#include "com_example_vinay_epubscreenreader_TextRendererActivity.h"

/*
 * Class:     com_speechlabssn_ssnflitehtstamil_MainActivity
 * Method:    mainfn
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_example_vinay_epubscreenreader_TextRendererActivity_mainfn
  (JNIEnv *env, jobject obj, jstring Usertext_cont,jstring path,jstring wpath)
  {
        //jclass userClass = (*env)->GetObjectClass(env, userobject);
        //jmethodID getUsertext_cont = (*env)->GetMethodID(env, userClass, "getUsertext", "()I");
        //char* text =  (char*)(*env)->CallIntMethod(env, userobject, getUsertext_cont);
        const char *aa = (*env)->GetStringUTFChars(env, Usertext_cont, 0);
        const char *bb = (*env)->GetStringUTFChars(env, path, 0);
        const char *cc = (*env)->GetStringUTFChars(env, wpath, 0);
        char * text = strdup(aa);
        char * filepath = strdup(bb);
        char * wavpath = strdup(cc);
        int y = main_syn(text,filepath,wavpath);
        int x = num_return();
        if(y==-1)
        {
            (*env)->NewStringUTF(env,"got -1");
        }
        else
        {
            (*env)->NewStringUTF(env,aa);
        }

  }
