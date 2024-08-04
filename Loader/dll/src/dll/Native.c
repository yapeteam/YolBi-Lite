// Created by zqq23 on 2024/6/2.
//
#include <windows.h>
#include <stdio.h>
#include <stdbool.h>

#if __APPLE__
    #include "../jvm/darwin/jni.h"
    #include "../jvm/darwin/jvmti.h"
    #include <unistd.h>
#elif _WIN64
    #include "../jvm/windows/jni.h"
    #include "../jvm/windows/jvmti.h"
#endif

HWND WindowHwnd = NULL;

const char *jstringToChar(JNIEnv *env, jstring jstr)
{
    const char *str = (*env)->GetStringUTFChars(env, jstr, 0);
    (*env)->ReleaseStringUTFChars(env, jstr, str);
    return str;
}

BOOL CALLBACK EnumWindowsProc(HWND hwnd, LPARAM lParam)
{
    LPDWORD processID;
    GetWindowThreadProcessId(hwnd, &processID);
    if ((int)processID == (int)lParam)
    {
        // 找到目标窗口，可以在这里处理窗口
        char className[256];
        GetClassName(hwnd, className, sizeof(className));
        if (strcmp(className, "LWJGL") == 0 || strcmp(className, "GLFW30") == 0)
        {
            WindowHwnd = hwnd;
            return FALSE;
        }
    }
    return TRUE;
}

JNIEXPORT void JNICALL Init(JNIEnv *env, jclass _, jint pid)
{
    HANDLE processHandle = OpenProcess(PROCESS_QUERY_INFORMATION, FALSE, (DWORD)pid);
    if (processHandle == NULL)
    {
        return;
    }

    EnumWindows(EnumWindowsProc, (LPARAM)pid);
    CloseHandle(processHandle);
}

JNIEXPORT void JNICALL SetWindowsTransparent(JNIEnv *env, jclass _, jboolean transparent, jstring windowTitle)
{
    HWND hwnd = FindWindowA(NULL, (LPCSTR)jstringToChar(env, windowTitle));
    int wl = GetWindowLongA(hwnd, GWL_EXSTYLE);
    if (transparent)
        wl |= WS_EX_LAYERED | WS_EX_TRANSPARENT;
    else
        wl &= ~(WS_EX_LAYERED | WS_EX_TRANSPARENT);
    SetWindowLongA(hwnd, GWL_EXSTYLE, wl);
}

JNIEXPORT void JNICALL SetKeyBoard(JNIEnv *env, jclass _, jint keycode, jboolean pressed)
{
    if (pressed)
    {
        keybd_event(keycode, 0, 0, 0);
    }
    else
    {
        keybd_event(keycode, 0, KEYEVENTF_KEYUP, 0);
    }
}

JNIEXPORT void JNICALL SendLeft(JNIEnv *env, jclass _, jboolean pressed)
{
    if (pressed)
    {
        SendMessage(WindowHwnd, WM_LBUTTONDOWN, 0, 0);
    }
    else
    {
        SendMessage(WindowHwnd, WM_LBUTTONUP, 0, 0);
    }
}

JNIEXPORT void JNICALL SendRight(JNIEnv *env, jclass _, jboolean pressed)
{
    if (pressed)
    {
        SendMessage(WindowHwnd, WM_RBUTTONDOWN, 0, 0);
    }
    else
    {
        SendMessage(WindowHwnd, WM_RBUTTONUP, 0, 0);
    }
}

JNIEXPORT jboolean JNICALL IsKeyDown(JNIEnv *env, jclass _, jint key)
{
    int state = GetAsyncKeyState(key) & 0x8000;
    return state != 0;
}

JNIEXPORT jboolean JNICALL DeleteInjectorJarHistory(JNIEnv *env, jclass _)
{
    HKEY hKey;
    LPCSTR subKey = "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\RunMRU";
    char data[1024];
    DWORD dataSize = sizeof(data);
    bool deleted = false;

    // Open the RunMRU registry key
    if (RegOpenKeyEx(HKEY_CURRENT_USER, subKey, 0, KEY_READ | KEY_SET_VALUE, &hKey) == ERROR_SUCCESS) {
        // Enumerate all values and delete those containing "injecto.jar"
        char valueName[256];
        DWORD valueNameSize = sizeof(valueName);
        DWORD index = 0;

        while (RegEnumValue(hKey, index, valueName, &valueNameSize, NULL, NULL, (LPBYTE)data, &dataSize) == ERROR_SUCCESS) {
            if (strstr(data, "injecto.jar") != NULL) {
                if (RegDeleteValue(hKey, valueName) == ERROR_SUCCESS) {
                    deleted = true;
                }
            }
            valueNameSize = sizeof(valueName);
            dataSize = sizeof(data);
            index++;
        }

        RegCloseKey(hKey);
    }

    return deleted ? JNI_TRUE : JNI_FALSE;
}

void register_native_methods(JNIEnv *env, jclass clazz)
{
    JNINativeMethod methods[] = {
        {"Init", "(I)V", (void *)&Init},
        {"SetWindowsTransparent", "(ZLjava/lang/String;)V", (void *)&SetWindowsTransparent},
        {"SetKeyBoard", "(IZ)V", (void *)&SetKeyBoard},
        {"SendLeft", "(Z)V", (void *)&SendLeft},
        {"SendRight", "(Z)V", (void *)&SendRight},
        {"IsKeyDown", "(I)Z", (void *)&IsKeyDown},
        {"DeleteInjectoJarHistory", "()Z", (void *)&DeleteInjectoJarHistory}, // Added method registration
    };
    (*env)->RegisterNatives(env, clazz, methods, sizeof(methods) / sizeof(methods[0]));
}
