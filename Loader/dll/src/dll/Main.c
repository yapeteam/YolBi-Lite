//
// Created by zqq23 on 2024/5/1.
//

#include <windows.h>

#if __APPLE__
#include "../jvm/darwin/jni.h"
#include "../jvm/darwin/jvmti.h"
#include <unistd.h>
#elif _WIN64
#include "../jvm/windows/jni.h"
#include "../jvm/windows/jvmti.h"
#endif

#include "../shared/main.c"
#include "utils.h"

PVOID UnLoad(PVOID arg)
{
    HMODULE hModule = NULL;
    GetModuleHandleExW(GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT, (LPWSTR)&UnLoad, &hModule);
    FreeLibraryAndExitThread(hModule, 0);
}

BOOL hooked = FALSE;

void HookMain(JNIEnv *env)
{
    if (hooked)
        return;
    hooked = TRUE;
    setlocale(LC_ALL, "");

    JavaVM *jvm;
    HMODULE jvmHandle = GetModuleHandle(("jvm.dll"));
    if (!jvmHandle)
        return;
    typedef jint(JNICALL * fnJNI_GetCreatedJavaVMs)(JavaVM **, jsize, jsize *);
    fnJNI_GetCreatedJavaVMs JNI_GetCreatedJavaVMs = (fnJNI_GetCreatedJavaVMs)GetProcAddress(jvmHandle, "JNI_GetCreatedJavaVMs");
    jint num = JNI_GetCreatedJavaVMs(&jvm, 1, NULL);
    jint num1 = (*jvm)->GetEnv(jvm, (void **)(&jvmti), JVMTI_VERSION);
    wchar_t userProfile[MAX_PATH];
    GetEnvironmentVariableW(L"USERPROFILE", userProfile, MAX_PATH);
    yolbiPath = format_wchar(L"%ls\\.yolbi", userProfile);
    wprintf(L"yolbiPath: %ls\n", yolbiPath);

    Inject_fla_bcf_(env, jvmti);
    if ((*env)->ExceptionCheck(env))
    {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }
    (*jvm)->DetachCurrentThread(jvm);
    CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)UnLoad, NULL, 0, NULL);
}

BYTE OldCode[12] = {0x00};
BYTE HookCode[12] = {0x48, 0xB8, 0x90, 0x90, 0x90, 0x90, 0x90, 0x90, 0x90, 0x90, 0xFF, 0xE0};

void UnHookFuncAddress64(UINT64 FuncAddress)
{
    DWORD OldProtect = 0;
    if (VirtualProtect((LPVOID)FuncAddress, 12, PAGE_EXECUTE_READWRITE, &OldProtect))
    {
        memcpy((LPVOID)FuncAddress, OldCode, sizeof(OldCode));
    }
    VirtualProtect((LPVOID)FuncAddress, 12, OldProtect, &OldProtect);
}

void HookFunction64(char *lpModule, LPCSTR lpFuncName, LPVOID lpFunction)
{
    DWORD_PTR FuncAddress = (UINT64)GetProcAddressPeb(GetModuleHandle(lpModule), lpFuncName);
    DWORD OldProtect = 0;

    if (VirtualProtect((LPVOID)FuncAddress, 12, PAGE_EXECUTE_READWRITE, &OldProtect))
    {
        memcpy(OldCode, (LPVOID)FuncAddress, 12);     // 拷贝原始机器码指令
        *(PINT64)(HookCode + 2) = (UINT64)lpFunction; // 填充90为指定跳转地址
    }
    memcpy((LPVOID)FuncAddress, &HookCode, sizeof(HookCode)); // 拷贝Hook机器指令
    VirtualProtect((LPVOID)FuncAddress, 12, OldProtect, &OldProtect);
}
void UnHookFunction64(char *lpModule, LPCSTR lpFuncName)
{
    DWORD OldProtect = 0;
    UINT64 FuncAddress = (UINT64)GetProcAddressPeb(GetModuleHandleA(lpModule), lpFuncName);
    if (VirtualProtect((LPVOID)FuncAddress, 12, PAGE_EXECUTE_READWRITE, &OldProtect))
    {
        memcpy((LPVOID)FuncAddress, OldCode, sizeof(OldCode));
    }
    VirtualProtect((LPVOID)FuncAddress, 12, OldProtect, &OldProtect);
}

jvmtiEnv *jvmti;

__int64 __fastcall Hook_JVM_EnqueueOperation(int a1, int a2, int a3, int a4, __int64 a5)
{
    // MessageBoxW(NULL, L"jmap以打死", "Hooked", 0);
    return -1;
}

void HookFuncAddress64(DWORD_PTR FuncAddress, LPVOID lpFunction)
{
    DWORD OldProtect = 0;

    if (VirtualProtect((LPVOID)FuncAddress, 12, PAGE_EXECUTE_READWRITE, &OldProtect))
    {
        memcpy(OldCode, (LPVOID)FuncAddress, 12);     // 拷贝原始机器码指令
        *(PINT64)(HookCode + 2) = (UINT64)lpFunction; // 填充90为指定跳转地址
    }
    memcpy((LPVOID)FuncAddress, &HookCode, sizeof(HookCode)); // 拷贝Hook机器指令
    VirtualProtect((LPVOID)FuncAddress, 12, OldProtect, &OldProtect);
}

// typedef void (*JVM_MonitorNotify)(JNIEnv *env, jobject obj);
//
// JVM_MonitorNotify MonitorNotify = NULL;
//
// void MonitorNotify_Hook(JNIEnv *env, jobject obj)
// {
//     UnHookFunction64("jvm.dll", "JVM_MonitorNotify");
//     MonitorNotify(env, obj);
//     HookMain(env);
// }

typedef jlong (*JVM_NanoTime)(JNIEnv *env, jclass ignored);

JVM_NanoTime NanoTime = NULL;

jvmtiError HookGetLoadedClasses(jvmtiEnv *jvmti_env, jint *class_count_ptr, jclass **classes_ptr)
{
    // MessageBoxW(NULL, L"Hooked", L"以打死GetLoadedClasses", MB_OK);
    // UnHookFuncAddress64((*jvmti)->GetLoadedClasses);
    // jvmtiError err = (*jvmti_env)->GetLoadedClasses(jvmti_env, class_count_ptr, classes_ptr);
    *class_count_ptr = 0;
    return 0;
}

jlong NanoTime_Hook(JNIEnv *env, jclass ignored)
{
    UnHookFunction64("jvm.dll", "JVM_NanoTime");
    jlong time = NanoTime(env, ignored);
    HookMain(env);
    HookFuncAddress64((DWORD_PTR)(*jvmti)->GetLoadedClasses, (LPVOID)HookGetLoadedClasses);
    HookFunction64("jvm.dll", "JVM_EnqueueOperation", (PROC)Hook_JVM_EnqueueOperation); // 你妈的jmap以打死
    return time;
}

PVOID WINAPI remote()
{
    HookFunction64("jvm.dll", "JVM_NanoTime", (PROC)NanoTime_Hook);
    HMODULE jvm = GetModuleHandleW(L"jvm.dll");
    // MonitorNotify = (JVM_MonitorNotify)GetProcAddressPeb(jvm, "JVM_MonitorNotify");
    NanoTime = (JVM_NanoTime)GetProcAddressPeb(jvm, "JVM_NanoTime");
    return NULL;
}

void entry()
{
    printf("entry\n");
    CreateThread(NULL, 4096, (LPTHREAD_START_ROUTINE)(&remote), NULL, 0, NULL);
}
