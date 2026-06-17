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
package net.hasor.boot.maven;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.CodeSource;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import net.hasor.boot.loader.HasorBootLayout;
import net.hasor.boot.loader.JarLauncher;

/**
 * Repackage a regular jar as a Hasor Boot executable archive.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-16
 */
@Mojo(name = "repackage", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME, threadSafe = true)
public class RepackageMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;
    @Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
    private File         outputDirectory;
    @Parameter(defaultValue = "${project.build.finalName}", readonly = true, required = true)
    private String       finalName;
    @Parameter(property = "hasor.boot.mainClass")
    private String       mainClass;
    @Parameter(property = "hasor.boot.classifier")
    private String       classifier;

    @Override
    public void execute() throws MojoExecutionException {
        File sourceJar = getSourceJar();
        if (!sourceJar.isFile()) {
            throw new MojoExecutionException("Project artifact does not exist: " + sourceJar);
        }
        String startClass = resolveMainClass(sourceJar);
        File targetJar = getTargetJar(sourceJar);
        File tempJar = new File(targetJar.getParentFile(), targetJar.getName() + ".tmp");

        try {
            repackage(sourceJar, tempJar, startClass);
            if (targetJar.exists() && !targetJar.delete()) {
                throw new IOException("Cannot delete " + targetJar);
            }
            if (!tempJar.renameTo(targetJar)) {
                throw new IOException("Cannot move " + tempJar + " to " + targetJar);
            }
            this.project.getArtifact().setFile(targetJar);
            getLog().info("Repackaged Hasor Boot archive: " + targetJar);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to repackage Hasor Boot archive.", e);
        } finally {
            if (tempJar.exists() && !tempJar.delete()) {
                getLog().warn("Cannot delete temp archive: " + tempJar);
            }
        }
    }

    private File getSourceJar() {
        Artifact artifact = this.project.getArtifact();
        if (artifact != null && artifact.getFile() != null) {
            return artifact.getFile();
        }
        return new File(this.outputDirectory, this.finalName + ".jar");
    }

    private File getTargetJar(File sourceJar) {
        if (this.classifier == null || this.classifier.trim().isEmpty()) {
            return sourceJar;
        }
        return new File(sourceJar.getParentFile(), this.finalName + "-" + this.classifier.trim() + ".jar");
    }

    private String resolveMainClass(File sourceJar) throws MojoExecutionException {
        if (this.mainClass != null && !this.mainClass.trim().isEmpty()) {
            return this.mainClass.trim();
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
            throw new MojoExecutionException("Cannot read source jar manifest: " + sourceJar, e);
        }
        throw new MojoExecutionException("Missing start class. Configure hasor.boot.mainClass or set Main-Class in the project jar.");
    }

    private void repackage(File sourceJar, File targetJar, String startClass) throws IOException, MojoExecutionException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.putValue(HasorBootLayout.MANIFEST_MAIN, JarLauncher.class.getName());
        attributes.putValue(HasorBootLayout.MANIFEST_START, startClass);

        try (JarOutputStream output = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(targetJar)), manifest)) {
            writeLoaderClasses(output);
            writeApplicationClasses(sourceJar, output);
            writeDependencies(output);
        }
    }

    private void writeLoaderClasses(JarOutputStream output) throws IOException, MojoExecutionException {
        File loaderJar = locateLoaderJar();
        try (JarFile jarFile = new JarFile(loaderJar)) {
            jarFile.stream().sorted(Comparator.comparing(JarEntry::getName)).forEach(entry -> {
                try {
                    if (entry.isDirectory() || "META-INF/MANIFEST.MF".equals(entry.getName())) {
                        return;
                    }
                    writeJarEntry(jarFile, entry, entry.getName(), output);
                } catch (IOException e) {
                    throw new EntryWriteException(e);
                }
            });
        } catch (EntryWriteException e) {
            throw e.getCause();
        }
    }

    private File locateLoaderJar() throws MojoExecutionException {
        CodeSource codeSource = JarLauncher.class.getProtectionDomain().getCodeSource();
        if (codeSource == null || codeSource.getLocation() == null) {
            throw new MojoExecutionException("Cannot locate hasor-boot-loader artifact.");
        }
        try {
            return new File(codeSource.getLocation().toURI());
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot locate hasor-boot-loader artifact.", e);
        }
    }

    private void writeApplicationClasses(File sourceJar, JarOutputStream output) throws IOException {
        try (JarFile jarFile = new JarFile(sourceJar)) {
            jarFile.stream().sorted(Comparator.comparing(JarEntry::getName)).forEach(entry -> {
                try {
                    if (entry.isDirectory() || "META-INF/MANIFEST.MF".equals(entry.getName())) {
                        return;
                    }
                    writeJarEntry(jarFile, entry, HasorBootLayout.APP_CLASSES + entry.getName(), output);
                } catch (IOException e) {
                    throw new EntryWriteException(e);
                }
            });
        } catch (EntryWriteException e) {
            throw e.getCause();
        }
    }

    private void writeDependencies(JarOutputStream output) throws IOException {
        Set<Artifact> artifacts = new LinkedHashSet<>(this.project.getArtifacts());
        for (Artifact artifact : artifacts) {
            if (!isRuntimeArtifact(artifact)) {
                continue;
            }
            File file = artifact.getFile();
            if (file == null || !file.isFile()) {
                continue;
            }
            String entryName = HasorBootLayout.APP_LIB + file.getName();
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

    private boolean isRuntimeArtifact(Artifact artifact) {
        String scope = artifact.getScope();
        return scope == null || Artifact.SCOPE_COMPILE.equals(scope) || Artifact.SCOPE_RUNTIME.equals(scope);
    }

    private static void writeJarEntry(JarFile jarFile, JarEntry sourceEntry, String targetName, JarOutputStream output) throws IOException {
        JarEntry targetEntry = new JarEntry(targetName);
        targetEntry.setTime(sourceEntry.getTime());
        output.putNextEntry(targetEntry);
        try (InputStream input = jarFile.getInputStream(sourceEntry)) {
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
