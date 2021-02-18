package com.orm.util.classdiscovery;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.orm.helper.ManifestHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dalvik.system.DexFile;

import static com.orm.util.ContextUtil.getPackageManager;
import static com.orm.util.ContextUtil.getPackageName;
import static com.orm.util.ContextUtil.getSharedPreferences;

public class DexClassScanner {
    private static final String PREFS_FILE = "multidex.version";
    private static final String KEY_DEX_NUMBER = "dex.number";
    private static final String EXTRACTED_NAME_EXT = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";
    private static final String INSTANT_RUN_DEX_DIR_PATH = "files/instant-run/dex/";
    private static final String SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes";

    public Set<String> getAllFullyQualifiedClassNamesInPackage(String packageName) {
        Set<String> classNames = new HashSet<>();
        for (String path : getDexSourceAndInstantRunDirs()) {
            try {
                DexFile dexfile;
                if (path.endsWith(EXTRACTED_SUFFIX)) {
                    //NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
                    dexfile = DexFile.loadDex(path, path + ".tmp", 0);
                } else {
                    dexfile = new DexFile(path);
                }
                Enumeration<String> dexEntries = dexfile.entries();
                while (dexEntries.hasMoreElements()) {
                    String className = dexEntries.nextElement();
                    if (className.startsWith(packageName)) {
                        classNames.add(className);
                    }
                }
                dexfile.close();
            } catch (IOException e) {
                logError("Error at loading dex file '" + path + "'", e);
            }
        }
        return classNames;
    }

    private static List<String> getDexSourceAndInstantRunDirs() {
        List<String> sourcePaths = new ArrayList<>();

        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), 0);
            String baseApkPath = applicationInfo.sourceDir;
            File dexDir = new File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME);
            File instantRunDir = new File(applicationInfo.dataDir, INSTANT_RUN_DEX_DIR_PATH); //default instant-run dir

            sourcePaths.add(baseApkPath);

            if (instantRunDir.exists()) { //check if app using instant run
                for (final File dexFile : instantRunDir.listFiles()) { //add all sources from instan-run
                    sourcePaths.add(dexFile.getAbsolutePath());
                }
            }

            File sourceApk = new File(baseApkPath);
            //the prefix of extracted file, ie: test.classes
            String extractedFilePrefix = sourceApk.getName() + EXTRACTED_NAME_EXT;

            int totalDexCount = getMultiDexPreferences().getInt(KEY_DEX_NUMBER, 1);

            for (int secondaryNumber = 2; secondaryNumber <= totalDexCount; secondaryNumber++) {
                //for each dex file, ie: test.classes2.zip, test.classes3.zip...
                String fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX;
                File extractedFile = new File(dexDir, fileName);
                if (extractedFile.isFile()) {
                    sourcePaths.add(extractedFile.getAbsolutePath());
                    //we ignore the verify zip part
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            logError("Package name not found: " + getPackageName(), e);
        }

        return sourcePaths;
    }

    private static void logError(String message, Exception e) {
        if (ManifestHelper.isDebugEnabled()) {
            Log.e("Sugar", message, e);
        }
    }

    private static SharedPreferences getMultiDexPreferences() {
        return getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    }
}
