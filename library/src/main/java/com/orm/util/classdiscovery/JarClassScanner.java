package com.orm.util.classdiscovery;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarClassScanner {
    private final String[] classpathEntries;

    public JarClassScanner(String[] classpathEntries) {
        if (classpathEntries == null) throw new NullPointerException("classpathEntries must be supplied");
        this.classpathEntries = classpathEntries;
    }

    public Set<String> getAllFullyQualifiedClassNamesInPackage(String packageName) {
        Set<String> jarClassNames = new HashSet<>();

        for (String classpathEntry : classpathEntries) {
            if (classpathEntry.toLowerCase().endsWith(".jar")) {
                Set<String> allClassNames = getAllQualifiedClassNamesInJar(classpathEntry);
                jarClassNames.addAll(filterOnDomainPackage(allClassNames, packageName));
            }
        }

        return jarClassNames;
    }

    private Set<String> filterOnDomainPackage(Set<String> allClassNames, String packageName) {
        Set<String> filteredClassNames = new HashSet<>();

        for (String qualifiedClassName : allClassNames) {
            if (qualifiedClassName.startsWith(packageName)) {
                filteredClassNames.add(qualifiedClassName);
            }
        }

        return filteredClassNames;
    }

    private Set<String> getAllQualifiedClassNamesInJar(String jarPath) {
        Set<String> qualifiedClassNames = new HashSet<>();

        try {
            ZipInputStream zip = new ZipInputStream(new FileInputStream(jarPath));
            for (ZipEntry jarEntry = zip.getNextEntry(); jarEntry != null; jarEntry = zip.getNextEntry()) {
                if (entryIsAClassFile(jarEntry)) {
                    qualifiedClassNames.add(getQualifiedClassName(jarEntry));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return qualifiedClassNames;
    }

    private boolean entryIsAClassFile(ZipEntry entry) {
        return !entry.isDirectory() && entry.getName().endsWith(".class");
    }

    private String getQualifiedClassName(ZipEntry entry) {
        String entryNameWithDotsInsteadOfSlashes = entry.getName().replace('/', '.');
        return entryNameWithDotsInsteadOfSlashes.substring(0, entryNameWithDotsInsteadOfSlashes.length() - ".class".length());
    }
}
