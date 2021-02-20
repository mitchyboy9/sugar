package com.orm.util.classdiscovery;

import com.google.common.collect.ImmutableSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

public class JarClassScannerTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void constructWithNullThrowsException() {
        Exception exception = assertThrows(NullPointerException.class, () -> new JarClassScanner(null));

        assertThat(exception.getMessage(), is("classpathEntries must be supplied"));
    }

    @Test
    public void getAllFullyQualifiedClassNamesInPackageWithEmptyClasspathReturnsEmptySet() {
        String[] emptyArray = new String[0];
        JarClassScanner jarClassScanner = new JarClassScanner(emptyArray);

        Set<String> actualClassNames = jarClassScanner.getAllFullyQualifiedClassNamesInPackage("");
        assertThat(actualClassNames, is(Collections.emptySet()));
    }

    @Test
    public void getAllFullyQualifiedClassNamesInPackageIgnoresNonJarClasspathEntries() throws IOException {
        testFolder.newFile("IgnoredClass.class");

        String[] classpath = {testFolder.getRoot().getAbsolutePath()};
        JarClassScanner jarClassScanner = new JarClassScanner(classpath);

        Set<String> actualClassNames = jarClassScanner.getAllFullyQualifiedClassNamesInPackage("");
        assertThat(actualClassNames, is(Collections.emptySet()));
    }

    @Test
    public void getAllFullyQualifiedClassNamesInPackageWithEmptyJarReturnsEmptySet() throws IOException {
        File emptyJar = new File(testFolder.getRoot(), "empty.jar");
        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(emptyJar));
        zipOutputStream.close();

        String[] classpath = {emptyJar.getAbsolutePath()};
        JarClassScanner jarClassScanner = new JarClassScanner(classpath);

        Set<String> actualClassNames = jarClassScanner.getAllFullyQualifiedClassNamesInPackage("");
        assertThat(actualClassNames, is(Collections.emptySet()));
    }

    @Test
    public void getAllFullyQualifiedClassNamesInPackageWithPopulatedJarReturnsAllClasses() throws Exception {
        Path dirToZip = Paths.get(testFolder.getRoot().getAbsolutePath(),"zip_root");

        String anyPackage = "anypackage";
        String anyOtherPackage = anyPackage + "different";

        createFileInDirectoryStructure(dirToZip, "MyClass.class", "com", "test", anyPackage);
        createFileInDirectoryStructure(dirToZip, "MyOtherClass.class", "com", "test", anyOtherPackage);

        Path populatedJarPath = Paths.get(testFolder.getRoot().getAbsolutePath(), "populated.jar");

        zipDirectory(dirToZip, populatedJarPath);

        String[] classpath = {populatedJarPath.toString()};
        JarClassScanner jarClassScanner = new JarClassScanner(classpath);

        String basePackage = "com.test";
        Set<String> actualClassNames = jarClassScanner.getAllFullyQualifiedClassNamesInPackage(basePackage);
        Set<String> expectedClassNames = ImmutableSet.of(basePackage + ".anypackage.MyClass", basePackage + ".anypackagedifferent.MyOtherClass");

        assertThat(actualClassNames, is(expectedClassNames));
    }

    private void createFileInDirectoryStructure(Path parentDirectory, String fileName, String... directoryNames) throws IOException {
        Path directoryHierarchy = Paths.get(parentDirectory.toString(), directoryNames);
        Files.createDirectories(directoryHierarchy);

        Path filePath = Paths.get(directoryHierarchy.toString(), fileName);
        Files.createFile(filePath);
    }

    private void zipDirectory(Path sourceFolderPath, Path outputZipPath) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputZipPath.toFile()));
        Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString()));
                Files.copy(file, zos);
                zos.closeEntry();
                return FileVisitResult.CONTINUE;
            }
        });
        zos.close();
    }
}