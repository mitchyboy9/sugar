package com.orm.util.classdiscovery;

import android.util.Log;

import com.orm.helper.ManifestHelper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class ResourcesClassScanner {
    private final ClassLoader classLoader;

    public ResourcesClassScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Set<String> getAllFullyQualifiedClassNamesInPackage(String packageName) {
        Set<String> classesInDomainPackage = new HashSet<>();

        try {
            Enumeration<URL> urls = classLoader.getResources("");
            if (urls == null) {
                return classesInDomainPackage;
            }

            while (urls.hasMoreElements()) {
                File resource = new File(urls.nextElement().getFile());
                if (isDirectoryThatProbablyContainsClasses(resource)) {
                    Set<File> allFiles = recursivelyListFilesDiscardingDirectories(resource);

                    Set<String> qualifiedClassNames = transformFilesToQualifiedClassNames(allFiles, resource);
                    Set<String> filteredQualifiedClassNames = filterOnDomainPackage(qualifiedClassNames, packageName);
                    classesInDomainPackage.addAll(filteredQualifiedClassNames);
                }
            }
        } catch (IOException e) {
            if (ManifestHelper.isDebugEnabled()) {
                Log.e("Sugar", e.getMessage(), e);
            }
        }

        return classesInDomainPackage;
    }

    private Set<String> filterOnDomainPackage(Set<String> qualifiedClassNames, String packageName) {
        Set<String> filteredClassNames = new HashSet<>();

        for (String qualifiedClassName : qualifiedClassNames) {
            if (qualifiedClassName.startsWith(packageName)) {
                filteredClassNames.add(qualifiedClassName);
            }
        }

        return filteredClassNames;
    }

    private Set<String> transformFilesToQualifiedClassNames(Set<File> files, File baseDirectory) {
        Set<String> qualifiedClassNames = new HashSet<>();

        for (File file : files) {
            String pathWithoutBaseDirectory = file.getPath().substring(baseDirectory.getPath().length() + 1);
            String qualifiedClassName = pathWithoutBaseDirectory.replace(File.separatorChar, '.');

            qualifiedClassName = removeClassSuffixIfPresent(qualifiedClassName);
            qualifiedClassNames.add(qualifiedClassName);
        }

        return qualifiedClassNames;
    }

    private String removeClassSuffixIfPresent(String qualifiedClassName) {
        String classSuffix = ".class";
        if (qualifiedClassName.endsWith(classSuffix)) {
            qualifiedClassName = qualifiedClassName.substring(0, qualifiedClassName.length() - classSuffix.length());
        }
        return qualifiedClassName;
    }

    private Set<File> recursivelyListFilesDiscardingDirectories(File resource) {
        Set<File> files = new HashSet<>();

        if (resource.isFile()) {
            files.add(resource);
        }
        else {
            String[] list = resource.list();
            for (String subPathName : list) {
                File subFile = new File(resource, subPathName);
                files.addAll(recursivelyListFilesDiscardingDirectories(subFile));
            }
        }

        return files;
    }

    private boolean isDirectoryThatProbablyContainsClasses(File classDirectory) {
        return classDirectory.isDirectory() &&
                (classDirectory.getName().contains("bin") ||
                        classDirectory.getName().contains("classes") ||
                        classDirectory.getName().contains("retrolambda"));
    }
}
