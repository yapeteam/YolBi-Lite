@echo off
g++ -s ddos.cpp -Iinclude -Lthread -Llib libcrypto-3-x64.dll libcurl.dll libjsoncpp.dll C:\Windows\System32\IPHLPAPI.DLL -o ddos.exe -finput-charset=UTF-8 -fexec-charset=GBK
pause