#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <wchar.h>
#include <locale.h>

#include "../jvm/jni.h"
#include "../jvm/jvmti.h"
#include "main.c"
#include "agent.h"

JNIEXPORT void JNICALL Java_cn_yapeteam_agent_Agent_loadNative(JNIEnv *env, jclass _)
{
    JavaVM *vm;
    (*env)->GetJavaVM(env, &vm);
    if (!vm)
        return;
    jvmtiEnv *jvmti;
    if ((*vm)->GetEnv(vm, (void **)&jvmti, JVMTI_VERSION) != JNI_OK)
        return;
    jclass SystemClass = (*env)->FindClass(env, "java/lang/System");
    jmethodID getPropertyMethod = (*env)->GetStaticMethodID(env, SystemClass, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
    jstring userDir = (jstring)(*env)->CallStaticObjectMethod(env, SystemClass, getPropertyMethod, (*env)->NewStringUTF(env, "user.home"));
    jsize jstr_len = (*env)->GetStringLength(env, userDir);
    wchar_t *dst = (wchar_t *)malloc((jstr_len + 1) * sizeof(wchar_t));
    if (!dst)
        return;
    memset(dst, 0, (jstr_len + 1) * sizeof(wchar_t));
    js2w(env, userDir, dst);
    yolbiPath = format_wchar(L"%ls\\.yolbi", dst);
    Inject_fla_bcf_(env, jvmti);
}
