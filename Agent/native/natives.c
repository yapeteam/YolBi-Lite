#include "cn_yapeteam_agent_Agent.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "../../Loader/dll/src/jvm/jvmti.h"
#include "../../Loader/dll/src/shared/main.c"

JNIEXPORT void JNICALL Java_cn_yapeteam_agent_Agent_loadNative(JNIEnv *env, jclass _)
{
    JavaVM *vmPtr;
    jint vmCount;
    jvmtiEnv *jvmti;
    if (JNI_GetCreatedJavaVMs(&vmPtr, 1, &vmCount) != JNI_OK || vmCount < 1 ||
        (*vmPtr)->GetEnv(vmPtr, (void **)&jvmti, JVMTI_VERSION) != JNI_OK)
        return;
    Inject_fla_bcf_(env, jvmti);
}
