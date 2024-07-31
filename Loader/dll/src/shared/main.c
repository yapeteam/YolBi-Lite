#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <dirent.h>
#include <wchar.h>
#include <locale.h>
#include "Native.c"
#include "unzip.h"

#if __APPLE__
#include "../jvm/darwin/jni.h"
#include "../jvm/darwin/jvmti.h"
#include <unistd.h>
#elif _WIN64
#include "../jvm/windows/jni.h"
#include "../jvm/windows/jvmti.h"
#endif

jvmtiEnv *jvmti;

struct Callback
{
    const unsigned char *array;
    jint length;
    int success;
};

struct TransformCallback
{
    jclass clazz;
    struct Callback *callback;
    struct TransformCallback *next;
};

static struct TransformCallback *callback_list = NULL;

void replaceFirst(char *str1, char *str2, char *str3)
{
    char str4[strlen(str1) + 1];
    char *p;
    strcpy(str4, str1);
    if ((p = strstr(str1, str2)) != NULL) /*p指向str2在str1中第一次出现的位置*/
    {
        while (str1 != p && str1 != NULL) /*将str1指针移动到p的位置*/
        {
            str1++;
        }
        str1[0] = '\0';                                  /*将str1指针指向的值变成/0,以此来截断str1,舍弃str2及以后的内容，只保留str2以前的内容*/
        strcat(str1, str3);                              /*在str1后拼接上str3,组成新str1*/
        strcat(str1, strstr(str4, str2) + strlen(str2)); /*strstr(str4,str2)是指向str2及以后的内容(包括str2),strstr(str4,str2)+strlen(str2)就是将指针向前移动strlen(str2)位，跳过str2*/
    }
}
/*将str1出现的所有的str2都替换为str3*/
void replace(char *str1, char *str2, char *str3)
{
    while (strstr(str1, str2) != NULL)
    {
        replaceFirst(str1, str2, str3);
    }
}

jclass JNICALL loadClass(JNIEnv *jniEnv, const char *name, jobject classloader)
{
    jmethodID loadClass = (*jniEnv)->GetMethodID(jniEnv, (*jniEnv)->GetObjectClass(jniEnv, classloader), "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    return (jclass)(*jniEnv)->CallObjectMethod(jniEnv, classloader, loadClass, (*jniEnv)->NewStringUTF(jniEnv, name));
}

jclass findClass(JNIEnv *jniEnv, const char *name, jobject classLoader)
{
    char *temp_name = strdup(name);
    jclass result = NULL;
    replace(temp_name, "/", ".");
    jclass Class = (*jniEnv)->FindClass(jniEnv, "java/lang/Class");
    jmethodID forName = (*jniEnv)->GetStaticMethodID(jniEnv, Class, "forName", "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;");
    jstring className = (*jniEnv)->NewStringUTF(jniEnv, temp_name);
    result = (*jniEnv)->CallStaticObjectMethod(jniEnv, Class, forName, className, JNI_TRUE, classLoader);
    if ((*jniEnv)->ExceptionCheck(jniEnv))
    {
        (*jniEnv)->ExceptionDescribe(jniEnv);
        (*jniEnv)->ExceptionClear(jniEnv);
    }
    if (result)
    {
        free(temp_name);
        return result;
    }
    replace(temp_name, ".", "/");
    result = (*jniEnv)->FindClass(jniEnv, temp_name);
    if ((*jniEnv)->ExceptionCheck(jniEnv))
    {
        (*jniEnv)->ExceptionDescribe(jniEnv);
        (*jniEnv)->ExceptionClear(jniEnv);
    }
    if (result)
    {
        free(temp_name);
        return result;
    }
    free(temp_name);
    return NULL;
}

unsigned char *jbyteArrayToUnsignedCharArray(JNIEnv *env, jbyteArray byteArray)
{
    jsize length = (*env)->GetArrayLength(env, byteArray);
    jbyte *elements = (*env)->GetByteArrayElements(env, byteArray, NULL);

    unsigned char *unsignedCharArray = (unsigned char *)malloc(length * sizeof(unsigned char));
    if (unsignedCharArray != NULL)
    {
        for (int i = 0; i < length; i++)
        {
            unsignedCharArray[i] = (unsigned char)elements[i];
        }
    }

    (*env)->ReleaseByteArrayElements(env, byteArray, elements, 0);

    return unsignedCharArray;
}

jbyteArray unsignedCharArrayToJByteArray(JNIEnv *env, const unsigned char *unsignedCharArray, jsize length)
{
    jbyteArray byteArray = (*env)->NewByteArray(env, length);

    if (byteArray != NULL)
    {
        jbyte *elements = (*env)->GetByteArrayElements(env, byteArray, NULL);

        for (int i = 0; i < length; i++)
        {
            elements[i] = (jbyte)unsignedCharArray[i];
        }

        (*env)->ReleaseByteArrayElements(env, byteArray, elements, 0);
    }

    return byteArray;
}

void js2w(JNIEnv *env, jstring jstr, wchar_t *dst)
{
    // 获取java字符串的长度
    jsize jstr_len = (*env)->GetStringLength(env, jstr);
    // 获取java字符串的jchar指针
    const jchar *pjstr = (*env)->GetStringChars(env, jstr, 0);

    wchar_t *ptmp = (wchar_t *)malloc((jstr_len + 1) * sizeof(wchar_t));
    if (ptmp == NULL)
    {
        return; // 处理内存分配失败的情况
    }
    memset(ptmp, 0, (jstr_len + 1) * sizeof(wchar_t));

    // 转换 以数组的形式把 jchar转换到wchar_t
    for (int i = 0; i < jstr_len; i++)
    {
        memcpy(&ptmp[i], &pjstr[i], sizeof(wchar_t));
    }

    wcscpy(dst, ptmp);
    free(ptmp);
    (*env)->ReleaseStringChars(env, jstr, pjstr);
}

// wchar_t 转换成 jstring
// env :JNIEnv jni操作 不可或缺的
// src：wchar_t 源字符 四字节似乎linux专用
// return :  转换完成以后的结果jstring
jstring w2js(JNIEnv *env, wchar_t *src)
{
    int src_len = wcslen(src);
    jchar *dest = (jchar *)malloc((src_len + 1) * sizeof(jchar));
    if (dest == NULL)
    {
        return NULL; // 处理内存分配失败的情况
    }
    memset(dest, 0, (src_len + 1) * sizeof(jchar));

    for (int i = 0; i < src_len; i++)
    {
        memcpy(&dest[i], &src[i], sizeof(jchar));
    }

    jstring dst = (*env)->NewString(env, dest, src_len);
    free(dest);
    return dst;
}

wchar_t *char2wchar(const char *src)
{
    if (src == NULL)
    {
        return NULL;
    }

    // 计算宽字符字符串的长度
    size_t len = mbstowcs(NULL, src, 0) + 1;
    if (len == (size_t)-1)
    {
        perror("mbstowcs() error");
        return NULL;
    }

    // 分配足够的内存来存储宽字符字符串
    wchar_t *dst = (wchar_t *)malloc(len * sizeof(wchar_t));
    if (dst == NULL)
    {
        perror("malloc() error");
        return NULL;
    }

    // 将多字节字符串转换为宽字符字符串
    if (mbstowcs(dst, src, len) == (size_t)-1)
    {
        perror("mbstowcs() error");
        free(dst);
        return NULL;
    }

    return dst;
}

wchar_t *get_current_directory_w()
{
    // 获取当前工作目录的多字节字符串
    char buffer[1024];
    if (getcwd(buffer, sizeof(buffer)) == NULL)
    {
        perror("getcwd() error");
        return NULL;
    }

    // 计算宽字符字符串的长度
    size_t len = mbstowcs(NULL, buffer, 0) + 1;
    if (len == (size_t)-1)
    {
        perror("mbstowcs() error");
        return NULL;
    }

    // 分配足够的内存来存储宽字符字符串
    wchar_t *wbuffer = (wchar_t *)malloc(len * sizeof(wchar_t));
    if (wbuffer == NULL)
    {
        perror("malloc() error");
        return NULL;
    }

    // 将多字节字符串转换为宽字符字符串
    if (mbstowcs(wbuffer, buffer, len) == (size_t)-1)
    {
        perror("mbstowcs() error");
        free(wbuffer);
        return NULL;
    }

    return wbuffer;
}

#pragma clang diagnostic push
#pragma ide diagnostic ignored "UnusedParameter"

void JNICALL classFileLoadHook(jvmtiEnv *jvmti_env,
                               JNIEnv *env,
                               jclass
                                   class_being_redefined,
                               jobject loader,
                               const char *name, jobject protection_domain,
                               jint class_data_len,
                               const unsigned char *class_data,
                               jint
                                   *new_class_data_len,
                               unsigned char **new_class_data)
{
    *new_class_data = NULL;

    if (class_being_redefined)
    {
        struct TransformCallback *current = callback_list;
        struct TransformCallback *previous = NULL;

        while (current != NULL)
        {
            if (!(*env)->IsSameObject(env, current->clazz, class_being_redefined))
            {
                previous = current;
                current = current->next;
                continue;
            }

            if (previous == NULL)
            {
                callback_list = current->next;
            }
            else
            {
                previous->next = current->next;
            }

            current->callback->array = class_data;
            current->callback->length = class_data_len;
            current->callback->success = 1;

            free(current);
            break;
        }
    }
}

#pragma clang diagnostic pop

void *allocate(jlong size)
{
    void *resultBuffer = malloc(size);
    return resultBuffer;
}

JNIEXPORT jclass JNICALL FindClass(JNIEnv *env, jclass _, jstring name, jobject loader)
{
    return findClass(env, jstringToChar(env, name), loader);
}

JNIEXPORT jbyteArray JNICALL GetClassBytes(JNIEnv *env, jclass _, jclass clazz)
{
    struct Callback *retransform_callback = (struct Callback *)allocate(sizeof(struct Callback));
    retransform_callback->success = 0;

    struct TransformCallback *new_node = (struct TransformCallback *)allocate(sizeof(struct TransformCallback));
    new_node->clazz = clazz;
    new_node->callback = retransform_callback;
    new_node->next = callback_list;
    callback_list = new_node;

    jclass *classes = (jclass *)
        allocate(sizeof(jclass));
    classes[0] = clazz;

    jint err = (*jvmti)->RetransformClasses((jvmtiEnv *)jvmti, 1, classes);

    if (err > 0)
    {
        printf(("jvmti error while getting class bytes: %ld\n"), err);
        return NULL;
    }

    jbyteArray output = (*env)->NewByteArray(env, retransform_callback->length);
    (*env)->SetByteArrayRegion(env, output, 0, retransform_callback->length, (jbyte *)retransform_callback->array);

    free(classes);
    return output;
}

JNIEXPORT jint JNICALL RedefineClass(JNIEnv *env, jclass _, jclass clazz, jbyteArray classBytes)
{
    jbyte *classByteArray = (*env)->GetByteArrayElements(env, classBytes, NULL);
    struct Callback *retransform_callback = (struct Callback *)allocate(sizeof(struct Callback));
    retransform_callback->success = 0;
    struct TransformCallback *new_node = (struct TransformCallback *)allocate(sizeof(struct TransformCallback));
    new_node->clazz = clazz;
    new_node->callback = retransform_callback;
    new_node->next = callback_list;
    callback_list = new_node;
    jvmtiClassDefinition *definitions = (jvmtiClassDefinition *)allocate(sizeof(jvmtiClassDefinition));
    definitions->klass = clazz;
    definitions->class_byte_count = (*env)->GetArrayLength(env, classBytes);
    definitions->class_bytes = (unsigned char *)classByteArray;
    jint error = (jint)(*jvmti)->RedefineClasses((jvmtiEnv *)jvmti, 1, definitions);
    (*env)->ReleaseByteArrayElements(env, classBytes, classByteArray, 0);
    free(definitions);
    return error;
}

JNIEXPORT jclass JNICALL DefineClass(JNIEnv *env, jclass _, jobject classLoader, jbyteArray bytes)
{
    jclass clClass = (*env)->FindClass(env, "java/lang/ClassLoader");
    jmethodID defineClass = (*env)->GetMethodID(env, clClass, "defineClass", "([BII)Ljava/lang/Class;");
    jobject classDefined = (*env)->CallObjectMethod(env, classLoader, defineClass, bytes, 0,
                                                    (*env)->GetArrayLength(env, bytes));
    return (jclass)classDefined;
}

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
    {
        printf("jni\n");
        (*env)->CallVoidMethod(env, loader, addURL, url);
    }
    else
    {
        char mbPath[1024];
        wcstombs(mbPath, path, 1024);
        printf("jvmti:%d\n", (*jvmti)->AddToSystemClassLoaderSearch(jvmti, mbPath));
    }
}

int vscwprintf(const wchar_t *format, va_list argptr)
{
    int buf_size = 1024; // 初始缓冲区大小
    while (buf_size < 1024 * 1024)
    {
        va_list args;
        va_copy(args, argptr);
        wchar_t buffer[buf_size];
        int fmt_size = vswprintf(buffer, sizeof(buffer) / sizeof(buffer[0]), format, args);
        if (fmt_size >= 0)
            return fmt_size; // 成功，返回格式化后字符串的长度
        buf_size *= 2;       // 扩大缓冲区大小
    }
    return -1; // 失败
}

wchar_t *format_wchar(const wchar_t *format, ...)
{
    va_list args;
    va_start(args, format);

    // Calculate the required buffer size
    int size = vscwprintf(format, args);
    if (size < 0)
    {
        va_end(args);
        return NULL; // Error
    }

    // Allocate memory for the formatted string
    wchar_t *buffer = (wchar_t *)malloc((size + 1) * sizeof(wchar_t));
    if (buffer == NULL)
    {
        va_end(args);
        return NULL; // Memory allocation failed
    }

    // Format the string
    vswprintf(buffer, size + 1, format, args);
    va_end(args);

    return buffer;
}

jobject classLoader;
jobject systemClassLoader;
wchar_t *yolbiPath;

jclass JarLoader;
jmethodID loadJarMethod;

bool hasLaunchClassLoader = false;
bool defineMode = false;

void loadJar(JNIEnv *env, wchar_t *path, jobject loader)
{
    if (JarLoader == NULL)
    {
        wchar_t *definderPath = format_wchar(L"%ls\\definer.jar", yolbiPath);
        wprintf(L"Loading definder.jar from %ls\n", definderPath);
        loadJar2URL(env, definderPath, systemClassLoader);
        JarLoader = findClass(env, "cn.yapeteam.definer.JarLoader", systemClassLoader);
        loadJarMethod = (*env)->GetStaticMethodID(env, JarLoader, "loadJar", "(Ljava/lang/String;Ljava/lang/ClassLoader;)V");
        JNINativeMethod HookerMethods[] = {
            {"defineClass", "(Ljava/lang/ClassLoader;[B)Ljava/lang/Class;", (void *)&DefineClass},
        };
        (*env)->RegisterNatives(env, JarLoader, HookerMethods, 1);
    }
    (*env)->CallStaticVoidMethod(env, JarLoader, loadJarMethod, w2js(env, path), loader);
}

JNIEXPORT void JNICALL loadInjection(JNIEnv *env, jclass _)
{
    wchar_t *injectionOutPath = format_wchar(L"%ls\\injection.jar", yolbiPath);
    if (!defineMode)
        loadJar2URL(env, injectionOutPath, classLoader);
    else
    {
        loadJar(env, injectionOutPath, classLoader);
        loadJar2URL(env, injectionOutPath, systemClassLoader);
    }
    jclass Start = findClass(env, "cn.yapeteam.yolbi.Loader", classLoader);
    if (!Start)
    {
        printf(("Failed to find Loader class\n"));
        return;
    }
    jmethodID start = (*env)->GetStaticMethodID(env, Start, ("start"), ("()V"));
    (*env)->CallStaticVoidMethod(env, Start, start);
    printf(("Start method called\n"));
}

int starts_with(const char *str, const char *prefix)
{
    return strncmp(str, prefix, strlen(prefix)) == 0;
}

int str_endwith(const char *str, const char *reg)
{
    int l1 = strlen(str), l2 = strlen(reg);
    if (l1 < l2)
        return 0;
    str += l1 - l2;
    while (*str && *reg && *str == *reg)
    {
        str++;
        reg++;
    }
    if (!*str && !*reg)
        return 1;
    return 0;
}

jobject getThreadByName(JNIEnv *env, const char *name)
{
    jclass threadClass = (*env)->FindClass(env, "java/lang/Thread");
    jmethodID getName = (*env)->GetMethodID(env, threadClass, "getName", "()Ljava/lang/String;");
    jmethodID getAllStackTraces = (*env)->GetStaticMethodID(env, threadClass, "getAllStackTraces", "()Ljava/util/Map;");
    jobject threadMap = (*env)->CallStaticObjectMethod(env, threadClass, getAllStackTraces);
    jclass Set = (*env)->FindClass(env, "java/util/Set");
    jclass Map = (*env)->FindClass(env, "java/util/Map");
    jmethodID keySet = (*env)->GetMethodID(env, Map, "keySet", "()Ljava/util/Set;");
    jobject keySetObj = (*env)->CallObjectMethod(env, threadMap, keySet);
    jmethodID toArray = (*env)->GetMethodID(env, Set, "toArray", "()[Ljava/lang/Object;");
    jobjectArray array = (jobjectArray)(*env)->CallObjectMethod(env, keySetObj, toArray);
    jsize length = (*env)->GetArrayLength(env, array);
    for (jsize i = 0; i < length; i++)
    {
        jobject thread = (*env)->GetObjectArrayElement(env, array, i);
        jstring threadName = (jstring)(*env)->CallObjectMethod(env, thread, getName);
        const char *threadNameChars = jstringToChar(env, threadName);
        if (!strcmp(threadNameChars, name))
        {
            return thread;
        }
    }
    return NULL;
}

void Inject_fla_bcf_(JNIEnv *jniEnv, jvmtiEnv *ti)
{
    printf(".--------------------------------------------------------------------------------------------------------------------.\n");
    printf("|            :::   :::     :::     :::::::::  :::::::::: ::::::::::: ::::::::::     :::     ::::    ::::             |\n");
    printf("|            :+:   :+:   :+: :+:   :+:    :+: :+:            :+:     :+:          :+: :+:   +:+:+: :+:+:+            |\n");
    printf("|             +:+ +:+   +:+   +:+  +:+    +:+ +:+            +:+     +:+         +:+   +:+  +:+ +:+:+ +:+            |\n");
    printf("|              +#++:   +#++:++#++: +#++:++#+  +#++:++#       +#+     +#++:++#   +#++:++#++: +#+  +:+  +#+            |\n");
    printf("|               +#+    +#+     +#+ +#+        +#+            +#+     +#+        +#+     +#+ +#+       +#+            |\n");
    printf("|               #+#    #+#     #+# #+#        #+#            #+#     #+#        #+#     #+# #+#       #+#            |\n");
    printf("|               ###    ###     ### ###        ##########     ###     ########## ###     ### ###       ###            |\n");
    printf("|  :::::::::  :::::::::   ::::::::  :::::::::  :::    :::  ::::::::  ::::::::::: :::::::::::  ::::::::  ::::    :::  |\n");
    printf("|  :+:    :+: :+:    :+: :+:    :+: :+:    :+: :+:    :+: :+:    :+:     :+:         :+:     :+:    :+: :+:+:   :+:  |\n");
    printf("|  +:+    +:+ +:+    +:+ +:+    +:+ +:+    +:+ +:+    +:+ +:+            +:+         +:+     +:+    +:+ :+:+:+  +:+  |\n");
    printf("|  +#++:++#+  +#++:++#:  +#+    +:+ +#+    +:+ +#+    +:+ +#+            +#+         +#+     +#+    +:+ +#+ +:+ +#+  |\n");
    printf("|  +#+        +#+    +#+ +#+    +#+ +#+    +#+ +#+    +#+ +#+            +#+         +#+     +#+    +#+ +#+  +#+#+#  |\n");
    printf("|  #+#        #+#    #+# #+#    #+# #+#    #+# #+#    #+# #+#    #+#     #+#         #+#     #+#    #+# #+#   #+#+#  |\n");
    printf("|  ###        ###    ###  ########  #########   ########   ########      ###     ###########  ########  ###    ####  |\n");
    printf("|====================================================================================================================|\n");
    printf("|                 **    **     **     *******  ******** ********** ********     **     ****     ****                 |\n");
    printf("|                //**  **     ****   /**////**/**///// /////**/// /**/////     ****   /**/**   **/**                 |\n");
    printf("|                 //****     **//**  /**   /**/**          /**    /**         **//**  /**//** ** /**                 |\n");
    printf("|                  //**     **  //** /******* /*******     /**    /*******   **  //** /** //***  /**                 |\n");
    printf("|                   /**    **********/**////  /**////      /**    /**////   **********/**  //*   /**                 |\n");
    printf("|                   /**   /**//////**/**      /**          /**    /**      /**//////**/**   /    /**                 |\n");
    printf("|                   /**   /**     /**/**      /********    /**    /********/**     /**/**        /**                 |\n");
    printf("|                   //    //      // //       ////////     //     //////// //      // //         //                  |\n");
    printf("|         *******  *******     *******   *******   **     **   ******  ********** **   *******   ****     **         |\n");
    printf("|        /**////**/**////**   **/////** /**////** /**    /**  **////**/////**/// /**  **/////** /**/**   /**         |\n");
    printf("|        /**   /**/**   /**  **     //**/**    /**/**    /** **    //     /**    /** **     //**/**//**  /**         |\n");
    printf("|        /******* /*******  /**      /**/**    /**/**    /**/**           /**    /**/**      /**/** //** /**         |\n");
    printf("|        /**////  /**///**  /**      /**/**    /**/**    /**/**           /**    /**/**      /**/**  //**/**         |\n");
    printf("|        /**      /**  //** //**     ** /**    ** /**    /**//**    **    /**    /**//**     ** /**   //****         |\n");
    printf("|        /**      /**   //** //*******  /*******  //*******  //******     /**    /** //*******  /**    //***         |\n");
    printf("|        //       //     //   ///////   ///////    ///////    //////      //     //   ///////   //      ///          |\n");
    printf("*--------------------------------------------------------------------------------------------------------------------*\n");

    jvmti = ti;
    jclass ClassLoader = (*jniEnv)->FindClass(jniEnv, ("java/lang/ClassLoader"));
    jmethodID getSystemClassLoader = (*jniEnv)->GetStaticMethodID(jniEnv, ClassLoader, ("getSystemClassLoader"), ("()Ljava/lang/ClassLoader;"));
    systemClassLoader = (*jniEnv)->CallStaticObjectMethod(jniEnv, ClassLoader, getSystemClassLoader);

    wchar_t *zip_path = format_wchar(L"%ls\\resources\\deps.zip", yolbiPath);
    wchar_t *zip_out = get_current_directory_w();
    jstring jzip_out = w2js(jniEnv, zip_out);
    jstring jzip_path = w2js(jniEnv, zip_path);
    wprintf(L"zip_path:%ls\n", zip_path);
    wprintf(L"zip_out:%ls\n", zip_out);
    jclass unzipClz = (*jniEnv)->DefineClass(jniEnv, "cn/yapeteam/builder/Unzip", systemClassLoader, (jbyte *)unzip_data, unzip_data_size);
    if (!unzipClz)
    {
        printf("Failed to define Unzip class\n");
        return;
    }
    jmethodID unzip = (*jniEnv)->GetStaticMethodID(jniEnv, unzipClz, "unzip", "(Ljava/lang/String;Ljava/lang/String;)V");
    (*jniEnv)->CallStaticVoidMethod(jniEnv, unzipClz, unzip, jzip_path, jzip_out);

    jobject clientThread = NULL;
    clientThread = getThreadByName(jniEnv, "Client thread");
    if (!clientThread)
        clientThread = getThreadByName(jniEnv, "Render thread");
    if (!clientThread)
    {
        printf(("Failed to find target thread\n"));
        return;
    }

    classLoader = (*jniEnv)->CallObjectMethod(jniEnv, clientThread, (*jniEnv)->GetMethodID(jniEnv, (*jniEnv)->GetObjectClass(jniEnv, clientThread), ("getContextClassLoader"), ("()Ljava/lang/ClassLoader;")));
    if (!classLoader)
        return;
    else
        printf(("classLoader found\n"));

    jclass Class = (*jniEnv)->FindClass(jniEnv, ("java/lang/Class"));
    jclass Object = (*jniEnv)->FindClass(jniEnv, ("java/lang/Object"));
    jmethodID getClassLoader = (*jniEnv)->GetMethodID(jniEnv, Class, ("getClassLoader"), ("()Ljava/lang/ClassLoader;"));
    jmethodID getClass = (*jniEnv)->GetMethodID(jniEnv, Object, ("getClass"), ("()Ljava/lang/Class;"));
    jobject classObject = (*jniEnv)->CallObjectMethod(jniEnv, classLoader, getClass);
    jobject classLoaderLoader = (*jniEnv)->CallObjectMethod(jniEnv, classObject, getClassLoader);
    jclass loaderClz = findClass(jniEnv, "cpw.mods.cl.ModuleClassLoader", classLoaderLoader);
    defineMode = loaderClz != NULL;
    loaderClz = findClass(jniEnv, "net.minecraft.launchwrapper.LaunchClassLoader", classLoaderLoader);
    hasLaunchClassLoader = !defineMode && loaderClz != NULL;
    if (hasLaunchClassLoader)
        printf("LaunchClassLoader found\n");
    if (defineMode)
        printf("Define Mode\n");

    wchar_t *jarPath = format_wchar(L"%ls\\dependencies\\asm-all-9.2.jar", yolbiPath);
    loadJar2URL(jniEnv, jarPath, systemClassLoader);
    if (hasLaunchClassLoader)
        loadJar2URL(jniEnv, jarPath, classLoaderLoader);
    if (defineMode)
        loadJar(jniEnv, jarPath, classLoader);

    jvmtiCapabilities capabilities = {0};
    memset(&capabilities, 0, sizeof(jvmtiCapabilities));

    capabilities.can_get_bytecodes = 1;
    capabilities.can_redefine_classes = 1;
    capabilities.can_redefine_any_class = 1;
    capabilities.can_generate_all_class_hook_events = 1;
    capabilities.can_retransform_classes = 1;
    capabilities.can_retransform_any_class = 1;

    (*jvmti)->AddCapabilities((jvmtiEnv *)jvmti, &capabilities);

    jvmtiEventCallbacks callbacks = {0};
    memset(&callbacks, 0, sizeof(jvmtiEventCallbacks));

    callbacks.ClassFileLoadHook = &classFileLoadHook;

    (*jvmti)->SetEventCallbacks((jvmtiEnv *)jvmti, &callbacks, sizeof(jvmtiEventCallbacks));
    (*jvmti)->SetEventNotificationMode((jvmtiEnv *)jvmti, JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, NULL);

    if (!defineMode)
    {
        wchar_t *hookerPath = format_wchar(L"%ls\\hooker.jar", yolbiPath);

        if (hasLaunchClassLoader)
            loadJar2URL(jniEnv, hookerPath, classLoaderLoader);
        else
            loadJar2URL(jniEnv, hookerPath, systemClassLoader);
        jclass Hooker = findClass(jniEnv, "cn/yapeteam/hooker/Hooker", hasLaunchClassLoader ? classLoaderLoader : systemClassLoader);
        JNINativeMethod HookerMethods[] = {
            {("getClassBytes"), ("(Ljava/lang/Class;)[B"), (void *)&GetClassBytes},
            {("defineClass"), ("(Ljava/lang/ClassLoader;[B)Ljava/lang/Class;"), (void *)&DefineClass},
            {("redefineClass"), ("(Ljava/lang/Class;[B)I"), (void *)&RedefineClass},
        };
        if (!Hooker)
        {
            printf(("Failed to find Hooker class\n"));
            return;
        }

        printf(("Hooker class found\n"));
        (*jniEnv)->RegisterNatives(jniEnv, Hooker, HookerMethods, 3);

        jmethodID hook = (*jniEnv)->GetStaticMethodID(jniEnv, Hooker, "hook", "()V");
        (*jniEnv)->CallStaticVoidMethod(jniEnv, Hooker, hook);
    }

    wchar_t *depsPath = format_wchar(L"%ls\\dependencies", yolbiPath);

    char mbPath[1024];
    wcstombs(mbPath, depsPath, 1024);
    DIR *dir = opendir(mbPath);
    struct dirent *entry;
    while ((entry = readdir(dir)) != NULL)
    {
        if (str_endwith(entry->d_name, ".jar"))
        {
            wchar_t *jarPath = format_wchar(L"%ls\\%ls", depsPath, char2wchar(entry->d_name));
            if (defineMode)
                loadJar(jniEnv, jarPath, classLoader);
            loadJar2URL(jniEnv, jarPath, systemClassLoader);
            wprintf(L"loaded: %ls\n", jarPath);
        }
    }
    closedir(dir);

    wchar_t *ymixinPath = format_wchar(L"%ls\\ymixin.jar", yolbiPath);
    if (!defineMode)
        loadJar2URL(jniEnv, ymixinPath, classLoader);
    else
    {
        loadJar(jniEnv, ymixinPath, classLoader);
        loadJar2URL(jniEnv, ymixinPath, systemClassLoader);
    }

    wchar_t *loaderPath = format_wchar(L"%ls\\loader.jar", yolbiPath);
    if (!defineMode)
        loadJar2URL(jniEnv, loaderPath, classLoader);
    else
    {
        loadJar(jniEnv, loaderPath, classLoader);
        loadJar2URL(jniEnv, loaderPath, systemClassLoader);
    }

    printf(("All jars loaded\n"));
    jclass wrapperClass = findClass(jniEnv, "cn.yapeteam.loader.NativeWrapper", classLoader);
    printf(("NativeWrapper\n"));
    if (!wrapperClass)
    {
        printf(("Failed to find NativeWrapper class\n"));
        return;
    }
    printf(("NativeWrapper class found\n"));
    JNINativeMethod methods[] = {
        {("getClassBytes"), ("(Ljava/lang/Class;)[B"), (void *)&GetClassBytes},
        {("redefineClass"), ("(Ljava/lang/Class;[B)I"), (void *)&RedefineClass},
        {("defineClass"), ("(Ljava/lang/ClassLoader;[B)Ljava/lang/Class;"), (void *)&DefineClass},
        {("FindClass"), ("(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Class;"), (void *)&FindClass},
    };
    (*jniEnv)->RegisterNatives(jniEnv, wrapperClass, methods, 4);
    jclass natvieClass = findClass(jniEnv, "cn.yapeteam.loader.Natives", classLoader);
    if (!natvieClass)
    {
        printf(("Failed to find Natives class\n"));
        return;
    }
    register_native_methods(jniEnv, natvieClass);
    printf(("Native methods registered\n"));

    jclass BootStrap = findClass(jniEnv, "cn.yapeteam.loader.BootStrap", classLoader);
    if (!BootStrap)
    {
        printf(("Failed to find BootStrap class\n"));
        return;
    }
    JNINativeMethod BootMethods[] = {
        {("loadInjection"), ("()V"), (void *)&loadInjection},
    };
    (*jniEnv)->RegisterNatives(jniEnv, BootStrap, BootMethods, 1);
    jmethodID entryPoint = (*jniEnv)->GetStaticMethodID(jniEnv, BootStrap, ("entry"), ("()V"));
    (*jniEnv)->CallStaticVoidMethod(jniEnv, BootStrap, entryPoint);
}