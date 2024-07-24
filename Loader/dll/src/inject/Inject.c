
// Copyright (c) 2012, Stephen Fewer of Harmony Security (www.harmonysecurity.com)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are permitted
// provided that the following conditions are met:
//
//     * Redistributions of source code must retain the above copyright notice, this list of
// conditions and the following disclaimer.
//
//     * Redistributions in binary form must reproduce the above copyright notice, this list of
// conditions and the following disclaimer in the documentation and/or other materials provided
// with the distribution.
//
//     * Neither the name of Harmony Security nor the names of its contributors may be used to
// endorse or promote products derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

#include <windows.h>
#include <stdio.h>
#include <wchar.h>
#include <locale.h>
#include "LoadLibraryR.h"
#include "cn_yapeteam_injector_Utils.h"

#pragma comment(lib, "Advapi32.lib")

#define RETURN_WITH_ERROR(e)                                  \
    {                                                         \
        printf("[-] %s. Error=%d\n", e, (int)GetLastError()); \
        return;                                               \
    }

const wchar_t *jstringToWchar(JNIEnv *env, jstring jstr)
{
    const jchar *str = (*env)->GetStringChars(env, jstr, NULL);
    if (str == NULL)
    {
        return NULL; // 处理异常情况
    }
    size_t len = (*env)->GetStringLength(env, jstr);
    wchar_t *wstr = (wchar_t *)malloc((len + 1) * sizeof(wchar_t));
    if (wstr == NULL)
    {
        (*env)->ReleaseStringChars(env, jstr, str);
        return NULL; // 处理异常情况
    }
    memcpy(wstr, str, len * sizeof(wchar_t));
    wstr[len] = L'\0';
    (*env)->ReleaseStringChars(env, jstr, str);
    return wstr;
}

#pragma clang diagnostic push
#pragma ide diagnostic ignored "OCUnusedGlobalDeclarationInspection"

JNIEXPORT void JNICALL
#pragma clang diagnostic push
#pragma ide diagnostic ignored "UnusedParameter"
Java_cn_yapeteam_injector_Utils_injectDLL(JNIEnv *env, jclass _, jint dwProcessId, jstring path)
{
#pragma clang diagnostic pop
    setlocale(LC_ALL, "");
    HANDLE hFile = NULL;
    LPVOID lpBuffer = NULL;
    DWORD dwBytesRead = 0;
    DWORD dwLength;
    const wchar_t *cpDllFile = jstringToWchar(env, path); // 使用宽字符类型
    hFile = CreateFileW(cpDllFile, GENERIC_READ, 0, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
    if (hFile == INVALID_HANDLE_VALUE)
        RETURN_WITH_ERROR("Failed to open the DLL file")
    dwLength = GetFileSize(hFile, NULL);
    if (dwLength == INVALID_FILE_SIZE || dwLength == 0)
        RETURN_WITH_ERROR("Failed to get the DLL file size")
    lpBuffer = HeapAlloc(GetProcessHeap(), 0, dwLength);
    if (!lpBuffer)
        RETURN_WITH_ERROR("Failed to get the DLL file size")
    if (ReadFile(hFile, lpBuffer, dwLength, &dwBytesRead, NULL) == FALSE)
    {
        RETURN_WITH_ERROR("Failed to alloc a buffer!")
    }
    HANDLE hModule = NULL;
    HANDLE hProcess = NULL;
    HANDLE hToken = NULL;
    TOKEN_PRIVILEGES priv = {0};

    if (OpenProcessToken(GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES | TOKEN_QUERY, &hToken))
    {
        priv.PrivilegeCount = 1;
        priv.Privileges[0].Attributes = SE_PRIVILEGE_ENABLED;

        if (LookupPrivilegeValue(NULL, SE_DEBUG_NAME, &priv.Privileges[0].Luid))
            AdjustTokenPrivileges(hToken, FALSE, &priv, 0, NULL, NULL);

        CloseHandle(hToken);
    }

    hProcess = OpenProcess(
        PROCESS_CREATE_THREAD | PROCESS_QUERY_INFORMATION | PROCESS_VM_OPERATION | PROCESS_VM_WRITE |
            PROCESS_VM_READ,
        FALSE, dwProcessId);
    if (!hProcess)
        RETURN_WITH_ERROR("Failed to open the target process")

    hModule = LoadRemoteLibraryR(hProcess, lpBuffer, dwLength, NULL);
    if (!hModule)
        RETURN_WITH_ERROR("Failed to inject the DLL")

    printf("[+] Injected the DLL into process %lu.\n", dwProcessId);

    WaitForSingleObject(hModule, -1);

    HeapFree(GetProcessHeap(), 0, lpBuffer);
    CloseHandle(hProcess);
}
#pragma clang diagnostic pop
