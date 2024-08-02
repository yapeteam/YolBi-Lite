@echo off
g++ -shared verifyNative.cpp -Iinclude -Llib libcrypto-3-x64.dll libcurl.dll libjsoncpp.dll C:\Windows\System32\IPHLPAPI.DLL -o libauth.dll
pause