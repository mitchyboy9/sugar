package com.orm.util.classdiscovery;

import com.google.common.collect.ImmutableSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

import static java.util.Collections.enumeration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResourcesClassScannerTest {
    private static final String MY_CLASS = "MyClass";
    @Mock
    ClassLoader mockClassLoader;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    private ResourcesClassScanner resourcesClassScanner;

    @Test
    public void getAllClassesWhenNoResourcesPresentReturnsEmptyList() throws IOException {
        Enumeration<URL> emptyEnumeration = Collections.emptyEnumeration();
        when(mockClassLoader.getResources(anyString())).thenReturn(emptyEnumeration);

        resourcesClassScanner = new ResourcesClassScanner(mockClassLoader);

        assertThat(resourcesClassScanner.getAllFullyQualifiedClassNamesInPackage(""), is(Collections.emptySet()));
    }

    @Test
    public void getAllClassesWhenResourcesAreNullReturnsEmptyList() throws IOException {
        when(mockClassLoader.getResources(anyString())).thenReturn(null);

        resourcesClassScanner = new ResourcesClassScanner(mockClassLoader);

        assertThat(resourcesClassScanner.getAllFullyQualifiedClassNamesInPackage(""), is(Collections.emptySet()));
    }

    @Test
    public void getAllClassesWithPathToEmptyDirectoryReturnsEmptyList() throws IOException {
        Enumeration<URL> resourcesEnumeration = enumeration(ImmutableSet.of(testFolder.getRoot().toURL()));
        when(mockClassLoader.getResources("")).thenReturn(resourcesEnumeration);

        resourcesClassScanner = new ResourcesClassScanner(mockClassLoader);

        assertThat(resourcesClassScanner.getAllFullyQualifiedClassNamesInPackage(""), is(Collections.emptySet()));
    }

    @Test
    public void getAllClassesWithClassInNonClassDirectoryReturnsEmptyList() throws IOException {
        Enumeration<URL> resourcesEnumeration = enumeration(ImmutableSet.of(testFolder.getRoot().toURL()));
        when(mockClassLoader.getResources("")).thenReturn(resourcesEnumeration);

        testFolder.newFile("MyClass.class");

        resourcesClassScanner = new ResourcesClassScanner(mockClassLoader);

        assertThat(resourcesClassScanner.getAllFullyQualifiedClassNamesInPackage(""), is(Collections.emptySet()));
    }

    @Test
    public void getAllClassesWithClassInBinDirectoryReturnsThatClass() throws IOException {
        File binDir = createClassFileWithinDirectoryHierarchyInTestFolder("bin");

        Enumeration<URL> resourcesEnumeration = enumeration(ImmutableSet.of(binDir.toURL()));
        when(mockClassLoader.getResources("")).thenReturn(resourcesEnumeration);

        resourcesClassScanner = new ResourcesClassScanner(mockClassLoader);

        assertThat(resourcesClassScanner.getAllFullyQualifiedClassNamesInPackage(""), is(ImmutableSet.of(MY_CLASS)));
    }

    @Test
    public void getAllClassesWithClassInClassesDirectoryReturnsThatClass() throws IOException {
        File binDir = createClassFileWithinDirectoryHierarchyInTestFolder("classes");

        Enumeration<URL> resourcesEnumeration = enumeration(ImmutableSet.of(binDir.toURL()));
        when(mockClassLoader.getResources("")).thenReturn(resourcesEnumeration);

        resourcesClassScanner = new ResourcesClassScanner(mockClassLoader);

        assertThat(resourcesClassScanner.getAllFullyQualifiedClassNamesInPackage(""), is(ImmutableSet.of(MY_CLASS)));
    }

    @Test
    public void getAllClassesWithClassInRetrolambdaDirectoryReturnsThatClass() throws IOException {
        File binDir = createClassFileWithinDirectoryHierarchyInTestFolder("retrolambda");

        Enumeration<URL> resourcesEnumeration = enumeration(ImmutableSet.of(binDir.toURL()));
        when(mockClassLoader.getResources("")).thenReturn(resourcesEnumeration);

        resourcesClassScanner = new ResourcesClassScanner(mockClassLoader);

        assertThat(resourcesClassScanner.getAllFullyQualifiedClassNamesInPackage(""), is(ImmutableSet.of(MY_CLASS)));
    }

    @Test
    public void getAllClassesWithClassInPackageHierarchyReturnsThatClass() throws IOException {
        File binDir = createClassFileWithinDirectoryHierarchyInTestFolder("bin/com/test");

        Enumeration<URL> resourcesEnumeration = enumeration(ImmutableSet.of(binDir.toURL()));
        when(mockClassLoader.getResources("")).thenReturn(resourcesEnumeration);

        resourcesClassScanner = new ResourcesClassScanner(mockClassLoader);

        assertThat(resourcesClassScanner.getAllFullyQualifiedClassNamesInPackage(""), is(ImmutableSet.of("com.test." + MY_CLASS)));
    }

    @Test
    public void getAllClassesWithPackageNameExcludesClassesFromOtherPackages() throws IOException {
        File classesDir = createClassFileWithinDirectoryHierarchyInTestFolder("classes/com/wanted");
        createClassFileWithinDirectoryHierarchyInTestFolder("classes/not/wanted");

        Enumeration<URL> resourcesEnumeration = enumeration(ImmutableSet.of(classesDir.toURL()));
        when(mockClassLoader.getResources("")).thenReturn(resourcesEnumeration);

        resourcesClassScanner = new ResourcesClassScanner(mockClassLoader);

        assertThat(resourcesClassScanner.getAllFullyQualifiedClassNamesInPackage("com.wanted"), is(ImmutableSet.of("com.wanted." + MY_CLASS)));
    }

    @Test
    public void getAllClassesWhenResourceReturnedIsAFileReturnsEmptyList() throws IOException {
        File fileNotDirectory = testFolder.newFile("classes");
        Enumeration<URL> resourcesEnumeration = enumeration(ImmutableSet.of(fileNotDirectory.toURL()));

        when(mockClassLoader.getResources("")).thenReturn(resourcesEnumeration);

        resourcesClassScanner = new ResourcesClassScanner(mockClassLoader);

        assertThat(resourcesClassScanner.getAllFullyQualifiedClassNamesInPackage(""), is(Collections.emptySet()));
    }

    private File createClassFileWithinDirectoryHierarchyInTestFolder(String directoryPath) throws IOException {
        File parentDir = new File(testFolder.getRoot(), directoryPath);
        assertTrue(parentDir.mkdirs());

        File classFile = new File(parentDir, MY_CLASS + ".class");
        assertTrue(classFile.createNewFile());

        return new File(testFolder.getRoot(), testFolder.getRoot().list()[0]);
    }
}