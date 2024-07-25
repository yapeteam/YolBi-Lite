//
// Created by zqq23 on 2024/5/1.
//

#include <windows.h>
#include "../jvm/jni.h"
#include "../jvm/jvmti.h"
#include "../shared/main.c"

extern JavaVM *jvm;
extern jvmtiEnv *jvmti;
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
    printf("1\n");
    HMODULE jvmHandle = GetModuleHandle(("jvm.dll"));
    if (!jvmHandle)
        return;
    printf("2\n");
    typedef jint(JNICALL * fnJNI_GetCreatedJavaVMs)(JavaVM **, jsize, jsize *);
    fnJNI_GetCreatedJavaVMs JNI_GetCreatedJavaVMs = (fnJNI_GetCreatedJavaVMs)GetProcAddress(jvmHandle, "JNI_GetCreatedJavaVMs");
    jint num = JNI_GetCreatedJavaVMs(&jvm, 1, NULL);
    jint num1 = (*jvm)->GetEnv(jvm, (void **)(&jvmti), JVMTI_VERSION);
    printf("3\n");
    printf("%d\n", num);
    printf("%d\n", num1);
    wchar_t userProfile[MAX_PATH];
    GetEnvironmentVariableW(L"USERPROFILE", userProfile, MAX_PATH);
    yolbiPath = format_wchar(L"%ls\\.yolbi", userProfile);
    wprintf(L"yolbiPath: %ls\n", yolbiPath);
    Inject_fla_bcf_(env);
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

typedef void (*JVM_MonitorNotify)(JNIEnv *env, jobject obj);

JVM_MonitorNotify MonitorNotify = NULL;

void MonitorNotify_Hook(JNIEnv *env, jobject obj)
{
    UnHookFunction64("jvm.dll", "JVM_MonitorNotify");
    MonitorNotify(env, obj);
    HookMain(env);
}

typedef jlong (*JVM_NanoTime)(JNIEnv *env, jclass ignored);

JVM_NanoTime NanoTime = NULL;

jlong NanoTime_Hook(JNIEnv *env, jclass ignored)
{
    UnHookFunction64("jvm.dll", "JVM_NanoTime");
    jlong time = NanoTime(env, ignored);
    HookMain(env);
    return time;
}

PVOID WINAPI remote()
{
    // HookFunction64("jvm.dll", "JVM_MonitorNotify", (PROC)MonitorNotify_Hook);
    HookFunction64("jvm.dll", "JVM_NanoTime", (PROC)NanoTime_Hook);
    HMODULE jvm = GetModuleHandleW(L"jvm.dll");
    MonitorNotify = (JVM_MonitorNotify)GetProcAddressPeb(jvm, "JVM_MonitorNotify");
    NanoTime = (JVM_NanoTime)GetProcAddressPeb(jvm, "JVM_NanoTime");

    return NULL;
}

void entry()
{
    CreateThread(NULL, 4096, (LPTHREAD_START_ROUTINE)(&remote), NULL, 0, NULL);
}
