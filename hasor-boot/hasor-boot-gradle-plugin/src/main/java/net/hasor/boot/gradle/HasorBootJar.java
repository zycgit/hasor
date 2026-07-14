/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.boot.gradle;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;

/**
 * Builds a Hasor Boot executable archive from a regular jar and runtime classpath.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-07-14
 */
public abstract class HasorBootJar extends DefaultTask {
    private static final String MANIFEST_MAIN  = "Main-Class";
    private static final String MANIFEST_START = "Hasor-Main-Class";
    private static final String APP_CLASSES    = "APP-INF/classes/";
    private static final String APP_LIB        = "APP-INF/lib/";
    private static final String JAR_LAUNCHER   = "net.hasor.boot.loader.JarLauncher";

    private final ConfigurableFileCollection runtimeClasspath = getProject().files();
    private final ConfigurableFileCollection loaderClasspath  = getProject().files();

    @InputFile
    public abstract RegularFileProperty getSourceJar();

    @Input
    @Optional
    public abstract Property<String> getMainClass();

    @Input
    public abstract Property<String> getArchiveClassifier();

    @Internal
    public abstract DirectoryProperty getDestinationDirectory();

    @Classpath
    public ConfigurableFileCollection getRuntimeClasspath() {
        return this.runtimeClasspath;
    }

    @Classpath
    public ConfigurableFileCollection getLoaderClasspath() {
        return this.loaderClasspath;
    }

    @OutputFile
    public File getArchiveFile() {
        File sourceJar = getSourceJar().getAsFile().get();
        String classifier = getArchiveClassifier().getOrElse("boot").trim();
        String sourceName = sourceJar.getName();
        String targetName = sourceName.endsWith(".jar") ? sourceName.substring(0, sourceName.length() - 4) : sourceName;
        if (!classifier.isEmpty()) {
            targetName = targetName + "-" + classifier;
        }
        return new File(getDestinationDirectory().getAsFile().get(), targetName + ".jar");
    }

    @TaskAction
    public void buildArchive() {
        File sourceJar = getSourceJar().getAsFile().get();
        if (!sourceJar.isFile()) {
            throw new GradleException("Project artifact does not exist: " + sourceJar);
        }

        File targetJar = getArchiveFile();
        File tempJar = new File(targetJar.getParentFile(), targetJar.getName() + ".tmp");
        targetJar.getParentFile().mkdirs();

        try {
            repackage(sourceJar, tempJar, resolveMainClass(sourceJar));
            if (targetJar.exists() && !targetJar.delete()) {
                throw new IOException("Cannot delete " + targetJar);
            }
            if (!tempJar.renameTo(targetJar)) {
                throw new IOException("Cannot move " + tempJar + " to " + targetJar);
            }
        } catch (IOException e) {
            throw new GradleException("Failed to repackage Hasor Boot archive.", e);
        } finally {
            if (tempJar.exists() && !tempJar.delete()) {
                getLogger().warn("Cannot delete temp archive: {}", tempJar);
            }
        }
    }

    private String resolveMainClass(File sourceJar) {
        String configured = getMainClass().getOrNull();
        if (configured != null && !configured.trim().isEmpty()) {
            return configured.trim();
        }
        try (JarFile jarFile = new JarFile(sourceJar)) {
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                String value = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
        } catch (IOException e) {
            throw new GradleException("Cannot read source jar manifest: " + sourceJar, e);
        }
        throw new GradleException("Missing start class. Configure bootJar.mainClass or set Main-Class in the project jar.");
    }

    private void repackage(File sourceJar, File targetJar, String startClass) throws IOException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.putValue(MANIFEST_MAIN, JAR_LAUNCHER);
        attributes.putValue(MANIFEST_START, startClass);

        try (JarOutputStream output = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(targetJar)), manifest)) {
            Set<String> names = new HashSet<>();
            names.add("META-INF/MANIFEST.MF");
            writeLoaderClasses(output, names);
            writeApplicationClasses(sourceJar, output, names);
            writeRuntimeClasspath(sourceJar, output, names);
        }
    }

    private void writeLoaderClasses(JarOutputStream output, Set<String> names) throws IOException {
        if (getLoaderClasspath().isEmpty()) {
            throw new GradleException("Missing hasor-boot-loader. Configure bootJar.loaderClasspath.");
        }
        for (File loaderLocation : getLoaderClasspath().getFiles()) {
            if (loaderLocation.isDirectory()) {
                writeDirectory(loaderLocation, "", output, names);
            } else if (loaderLocation.getName().endsWith(".jar")) {
                writeJar(loaderLocation, "", output, names);
            }
        }
    }

    private void writeApplicationClasses(File sourceJar, JarOutputStream output, Set<String> names) throws IOException {
        writeJar(sourceJar, APP_CLASSES, output, names);
    }

    private void writeRuntimeClasspath(File sourceJar, JarOutputStream output, Set<String> names) throws IOException {
        for (File file : getRuntimeClasspath().getFiles()) {
            if (!file.exists() || file.equals(sourceJar)) {
                continue;
            }
            if (file.isDirectory()) {
                writeDirectory(file, APP_CLASSES, output, names);
            } else if (file.getName().endsWith(".jar")) {
                writeNestedJar(file, output, names);
            }
        }
    }

    private void writeJar(File source, String prefix, JarOutputStream output, Set<String> names) throws IOException {
        try (JarFile jarFile = new JarFile(source)) {
            jarFile.stream().sorted(Comparator.comparing(JarEntry::getName)).forEach(entry -> {
                try {
                    if (entry.isDirectory() || "META-INF/MANIFEST.MF".equals(entry.getName())) {
                        return;
                    }
                    writeJarEntry(jarFile, entry, prefix + entry.getName(), output, names);
                } catch (IOException e) {
                    throw new EntryWriteException(e);
                }
            });
        } catch (EntryWriteException e) {
            throw e.getCause();
        }
    }

    private void writeDirectory(File root, String prefix, JarOutputStream output, Set<String> names) throws IOException {
        getProject().fileTree(root).getFiles().stream().sorted().forEach(file -> {
            try {
                String relativePath = root.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/');
                writeFileEntry(file, prefix + relativePath, output, names);
            } catch (IOException e) {
                throw new EntryWriteException(e);
            }
        });
    }

    private void writeNestedJar(File file, JarOutputStream output, Set<String> names) throws IOException {
        String entryName = APP_LIB + file.getName();
        if (!names.add(entryName)) {
            return;
        }
        JarEntry entry = new JarEntry(entryName);
        entry.setTime(file.lastModified());
        entry.setMethod(JarEntry.STORED);
        entry.setSize(file.length());
        entry.setCompressedSize(file.length());
        entry.setCrc(crc(file));
        output.putNextEntry(entry);
        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            copy(input, output);
        }
        output.closeEntry();
    }

    private static long crc(File file) throws IOException {
        CRC32 crc = new CRC32();
        byte[] buffer = new byte[8192];
        try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
            int len;
            while ((len = input.read(buffer)) != -1) {
                crc.update(buffer, 0, len);
            }
        }
        return crc.getValue();
    }

    private static void writeJarEntry(JarFile jarFile, JarEntry sourceEntry, String targetName, JarOutputStream output, Set<String> names) throws IOException {
        if (!names.add(targetName)) {
            return;
        }
        JarEntry targetEntry = new JarEntry(targetName);
        targetEntry.setTime(sourceEntry.getTime());
        output.putNextEntry(targetEntry);
        try (InputStream input = jarFile.getInputStream(sourceEntry)) {
            copy(input, output);
        }
        output.closeEntry();
    }

    private static void writeFileEntry(File source, String targetName, JarOutputStream output, Set<String> names) throws IOException {
        if (!names.add(targetName)) {
            return;
        }
        JarEntry targetEntry = new JarEntry(targetName);
        targetEntry.setTime(source.lastModified());
        output.putNextEntry(targetEntry);
        try (InputStream input = new BufferedInputStream(new FileInputStream(source))) {
            copy(input, output);
        }
        output.closeEntry();
    }

    private static void copy(InputStream input, JarOutputStream output) throws IOException {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = input.read(buffer)) != -1) {
            output.write(buffer, 0, len);
        }
    }

    private static class EntryWriteException extends RuntimeException {
        EntryWriteException(IOException cause) {
            super(cause);
        }

        @Override
        public synchronized IOException getCause() {
            return (IOException) super.getCause();
        }
    }
}
