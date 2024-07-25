#include "cn_yapeteam_agent_Agent.h"

void loadJar2URL(JNIEnv *env, wchar_t *path, jobject loader)
{
    jclass urlClassLoader = (*env)->FindClass(env, "java/net/URLClassLoader");
    jclass fileClass = (*env)->FindClass(env, "java/io/File");
    jmethodID init = (*env)->GetMethodID(env, fileClass, "<init>", "(Ljava/lang/String;)V");
    jmethodID addURL = (*env)->GetMethodID(env, urlClassLoader, "addURL", "(Ljava/net/URL;)V");
    jstring filePath = w2js(env, path);
    jobject file = (*env)->NewObject(env, fileClass, init, filePath);
    jmethodID toURI = (*env)->GetMethodID(env, fileClass, "toURI", "()Ljava/net/URI;");
    jobject uri = (*env)->CallObjectMethod(env, file, toURI);
    jclass URIClass = (*env)->FindClass(env, "java/net/URI");
    jmethodID toURL = (*env)->GetMethodID(env, URIClass, "toURL", "()Ljava/net/URL;");
    jobject url = (*env)->CallObjectMethod(env, uri, toURL);
    if ((*env)->IsInstanceOf(env, loader, urlClassLoader))
        (*env)->CallVoidMethod(env, loader, addURL, url);
}

jclass findClass(JNIEnv *jniEnv, const char *name, jobject classLoader)
{
    jclass result = NULL;
    replace(name, "/", ".");
    jclass Class = (*jniEnv)->FindClass(jniEnv, "java/lang/Class");
    jmethodID forName = (*jniEnv)->GetStaticMethodID(jniEnv, Class, "forName", "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;");
    jstring className = (*jniEnv)->NewStringUTF(jniEnv, name);
    result = (*jniEnv)->CallStaticObjectMethod(jniEnv, Class, forName, className, JNI_TRUE, classLoader);
    if (result)
        return result;
    replace(name, ".", "/");
    result = (*jniEnv)->FindClass(jniEnv, name);
    if ((*jniEnv)->ExceptionCheck(jniEnv))
    {
        (*jniEnv)->ExceptionDescribe(jniEnv);
        (*jniEnv)->ExceptionClear(jniEnv);
    }
    if (result)
        return result;
    return NULL;
}

JNIEXPORT jclass JNICALL FindClass(JNIEnv *env, jclass _, jstring name, jobject loader)
{
    return findClass(env, jstringToChar(env, name), loader);
}

JNIEXPORT void JNICALL Java_cn_yapeteam_agent_Agent_loadNative(JNIEnv *env, jclass clz)
{
    JNINativeMethod Methods[] = {
        {"loadJar2URL", "(Ljava/lang/String;Ljava/lang/ClassLoader;)V", (void *)&loadJar2URL},
        {"findClass", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/Class;", (void *)&FindClass},
    };
    (*env)->RegisterNatives(env, clz, Methods, 2);
}
