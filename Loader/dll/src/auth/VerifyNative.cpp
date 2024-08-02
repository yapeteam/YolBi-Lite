#include "cn_yapeteam_injector_Main.h"
#include "verify.cpp"
#include <string>
#include <fstream>
#include <wchar.h>
#include <locale.h>
#include <windows.h>
using namespace std;

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

JNIEXPORT jboolean JNICALL Java_cn_yapeteam_injector_Main_login(JNIEnv *env, jclass cls, jstring username, jstring password)
{
    setlocale(LC_ALL, "");
    wchar_t *filePath;
    const char *u = env->GetStringUTFChars(username, 0);
    const char *p = env->GetStringUTFChars(password, 0);
    wchar_t userProfile[MAX_PATH];
    GetEnvironmentVariableW(L"USERPROFILE", userProfile, MAX_PATH);
    filePath = format_wchar(L"%ls\\.yolbi\\auth.txt", userProfile);
    ofstream file(filePath, fstream::out);
    if (!file.is_open())
    {
        file.close();
        free(filePath);
        return false;
    }
    file << u << endl;
    file << p << endl;
    return verifyUser(string(u), string(p), env, cls);
}

JNIEXPORT void JNICALL Java_cn_yapeteam_injector_Main_active(JNIEnv *env, jclass cls, jstring username, jstring cdk)
{
    activateUser(string(env->GetStringUTFChars(username, 0)), string(env->GetStringUTFChars(cdk, 0)), env, cls);
}