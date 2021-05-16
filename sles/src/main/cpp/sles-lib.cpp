#include "SLES/OpenSLES.h"
#include "SLES/OpenSLES_Android.h"
#include <android/log.h>
#include <cmath>
#include <jni.h>


#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, "sles-lib", __VA_ARGS__))


#define SLEEP(x)        // Client system sleep function to sleep x milliseconds would replace SLEEP macro
/*******************************************************************/
#define MAX_NUMBER_INTERFACES 2
#define CIRCLE_RADIUS       1000      /* 1.0 meters */
/* we move the source by this angle (in radians) at each step */
#define CIRCLE_STEP        (float) ( TWOPI / 180.0 / 2.0 )
#define      PI                  3.1415926535f
#define TWOPI        ( 2.0f * PI )

/* Checks for error. If any errors exit the application! */
void CheckErr(SLresult res) {
    if (res != SL_RESULT_SUCCESS) { // Debug printing to be placed here
//        exit(1);
        LOGI("error");
    }
}

/* Play create a 3D source and spin it around the listener  */ void
TestSimple3D(SLObjectItf sl) {
    SLEngineItf EngineItf;
    SLresult res;
    SLDataSource audioSource;
    SLDataLocator_URI uri;
    SLDataFormat_MIME mime;
    SLDataSink audioSink;
    SLDataLocator_OutputMix locator_outputmix;
    SLObjectItf player;
    SLPlayItf playItf;
    SL3DLocationItf locationItf;
    SLObjectItf listener;
    SLObjectItf OutputMix;
    int i;
    SLboolean required[MAX_NUMBER_INTERFACES];
    SLInterfaceID iidArray[MAX_NUMBER_INTERFACES];
    /* Get the SL Engine Interface which is implicit */
    res = (*sl)->GetInterface(sl, SL_IID_ENGINE, (void *) &EngineItf);
    CheckErr(res);
    /* Initialize arrays required[] and iidArray[] */
    for (i = 0; i < MAX_NUMBER_INTERFACES; i++) {
        required[i] = SL_BOOLEAN_FALSE;
        iidArray[i] = SL_IID_NULL;
    }
    /* Create Output Mix object to be used by player - no interfaces required */
    res = (*EngineItf)->CreateOutputMix(EngineItf, &OutputMix, 0, iidArray, required);
    CheckErr(res);
    /* Realizing the Output Mix object in synchronous mode. */
    res = (*OutputMix)->Realize(OutputMix, SL_BOOLEAN_FALSE);
    CheckErr(res);
    /* Create 3D listener - no interfaces requires as the listener will remain stationary */
    res = (*EngineItf)->CreateListener(EngineItf, &listener, 0, iidArray, required);
    CheckErr(res);
    /* Realizing the listener object in synchronous mode. */
    res = (*listener)->Realize(listener, SL_BOOLEAN_FALSE);
    CheckErr(res);
    /* Setup the data source structure for the player */
    uri.locatorType = SL_DATALOCATOR_URI;
    uri.URI = (SLchar *) "file:///buzzingbee.wav";
    mime.formatType = SL_DATAFORMAT_MIME;
    mime.mimeType = (SLchar *) "audio/x-wav";
    mime.containerType = SL_CONTAINERTYPE_WAV;
    audioSource.pLocator = (void *) &uri;
    audioSource.pFormat = (void *) &mime;
/* Setup the data sink structure */
    locator_outputmix.locatorType = SL_DATALOCATOR_OUTPUTMIX;
    locator_outputmix.outputMix = OutputMix;
    audioSink.pLocator = (void *) &locator_outputmix;
    audioSink.pFormat = NULL;
    /* Set arrays required[] and iidArray[] for 3DLocationItf interface (PlayItf is implicit) */
    required[0] = SL_BOOLEAN_TRUE;
    iidArray[0] = SL_IID_3DLOCATION;
    /* Create the 3D player */
    res = (*EngineItf)->CreateAudioPlayer(EngineItf, &player, &audioSource, &audioSink, 1, iidArray,
                                          required);
    CheckErr(res);
    /* Realizing the player in synchronous mode. */
    res = (*player)->Realize(player, SL_BOOLEAN_FALSE);
    CheckErr(res);
    /* Get the play and 3D location interfaces */
    res = (*player)->GetInterface(player, SL_IID_PLAY, (void *) &playItf);
    CheckErr(res);
    res = (*player)->GetInterface(player, SL_IID_3DLOCATION, (void *) &locationItf);
    CheckErr(res);
    {
        SLVec3D coords;
        SLuint32 playState;
        float angle = 0.0f; /* Position the 3D source in front the listener */
        coords.x = (SLuint32) (CIRCLE_RADIUS * sinf(angle));
        coords.y = 0;
        coords.z = (SLuint32) (CIRCLE_RADIUS * cosf(angle));
        (*locationItf)->SetLocationCartesian(locationItf, &coords);
        CheckErr(res);
        /* Start playing the 3D source (buzzing bee) */
        res = (*playItf)->SetPlayState(playItf, SL_PLAYSTATE_PLAYING);
        CheckErr(res);
        do {
            angle += CIRCLE_STEP;
            if (angle >= TWOPI) {
                angle = 0.0f;
            }
            /* move source in horizontal circle (clockwise direction) */
            coords.x = (SLuint32) (CIRCLE_RADIUS * sinf(angle));
            coords.z = (SLuint32) (CIRCLE_RADIUS * cosf(angle));
            (*locationItf)->SetLocationCartesian(locationItf, &coords);
            CheckErr(res);
            SLEEP(10);  // Sleep for 10ms
            res = (*playItf)->GetPlayState(playItf, &playState);
            CheckErr(res);
        } while (playState != SL_PLAYSTATE_STOPPED);
    }
    /* Destroy the player */
    (*player)->Destroy(player);     /* Destroy Output Mix object */
    (*OutputMix)->Destroy(OutputMix);
}

int sl_main() {
    SLresult res;
    SLObjectItf sl;
    SLEngineOption EngineOption[] = {(SLuint32) SL_ENGINEOPTION_THREADSAFE,
                                     (SLuint32) SL_BOOLEAN_TRUE};
    res = slCreateEngine(&sl, 1, EngineOption, 0, NULL, NULL);
    CheckErr(res);
    /* Realizing the SL Engine in synchronous mode. */
    res = (*sl)->Realize(sl, SL_BOOLEAN_FALSE);
    CheckErr(res);
    TestSimple3D(sl);    /* Shutdown OpenSL ES */
    (*sl)->Destroy(sl);
//    exit(0);
}

extern "C" {
JNIEXPORT void JNICALL Java_com_hustunique_sles_SLESNative_test(JNIEnv *env, jobject instance) {
    LOGI("test");
    sl_main();
}
}