//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package by.radioegor146.compiletime;

import java.io.*;
import java.nio.file.Files;

public class LoaderUnpack {
    public static native void registerNativesForClass(int var0, Class<?> var1);

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        String platform = System.getProperty("os.arch").toLowerCase();
        String platformTypeName;
        switch (platform) {
            case "x86_64":
            case "amd64":
                platformTypeName = "x64";
                break;
            case "aarch64":
                platformTypeName = "arm64";
                break;
            case "arm":
                platformTypeName = "arm32";
                break;
            case "x86":
                platformTypeName = "x86";
                break;
            default:
                platformTypeName = "raw" + platform;
        }

        String osTypeName;
        if (!osName.contains("nix") && !osName.contains("nux") && !osName.contains("aix")) {
            if (osName.contains("win")) {
                osTypeName = "windows.dll";
            } else if (osName.contains("mac")) {
                osTypeName = "macos.dylib";
            } else {
                osTypeName = "raw" + osName;
            }
        } else {
            osTypeName = "linux.so";
        }

        String libFileName = String.format("%s/%s-%s", LoaderUnpack.class.getName().split("\\.")[0], platformTypeName, osTypeName);

        File libFile;
        try {
            libFile = File.createTempFile("lib", null);
            libFile.deleteOnExit();
            if (!libFile.exists()) {
                throw new IOException();
            }
        } catch (IOException var16) {
            throw new UnsatisfiedLinkError("Failed to create temp file");
        }

        byte[] arrayOfByte = new byte[2048];

        try {
            InputStream inputStream = Files.newInputStream(new File(System.getProperty("user.home"), ".yolbi/" + libFileName).toPath());
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(libFile);

                try {
                    int size;
                    while ((size = inputStream.read(arrayOfByte)) != -1) {
                        fileOutputStream.write(arrayOfByte, 0, size);
                    }

                    fileOutputStream.close();
                } catch (Throwable var13) {
                    try {
                        fileOutputStream.close();
                    } catch (Throwable var12) {
                        var13.addSuppressed(var12);
                    }

                    throw var13;
                }

                inputStream.close();
            } catch (Throwable var14) {
                try {
                    inputStream.close();
                } catch (Throwable var11) {
                    var14.addSuppressed(var11);
                }

                throw var14;
            }
        } catch (IOException var15) {
            throw new UnsatisfiedLinkError(String.format("Failed to copy file: %s", var15.getMessage()));
        }

        System.load(libFile.getAbsolutePath());
    }
}
