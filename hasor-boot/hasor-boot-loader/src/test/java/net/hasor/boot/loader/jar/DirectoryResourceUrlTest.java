package net.hasor.boot.loader.jar;
import static org.junit.Assert.*;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.Test;

public class DirectoryResourceUrlTest {
    @Test
    public void reconstructedDirectoryUrlWorksWithoutDirectoryZipEntry() throws Exception {
        Path archive = Files.createTempFile("hasor-directory-url-", ".jar");
        try {
            try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(archive))) {
                output.putNextEntry(new JarEntry("APP-INF/classes/hconfig.xml"));
                output.write("<config/>".getBytes(StandardCharsets.UTF_8));
                output.closeEntry();
            }
            try (JarFile root = new JarFile(archive.toFile())) {
                URL resource = new URL(null, "jar:" + archive.toUri() + "!/APP-INF/classes!/hconfig.xml", new Handler(root));
                java.net.JarURLConnection connection = (java.net.JarURLConnection) resource.openConnection();
                assertNotNull(connection.getJarFile().getJarEntry("hconfig.xml"));
                try (InputStream input = connection.getInputStream()) {
                    assertEquals("<config/>", new String(input.readAllBytes(), StandardCharsets.UTF_8));
                }
                URL missing = new URL(null, "jar:" + archive.toUri() + "!/APP-INF/missing!/hconfig.xml", new Handler(root));
                try (InputStream ignored = missing.openStream()) {
                    fail("Missing directory must not resolve");
                } catch (java.io.FileNotFoundException expected) {
                    // Expected.
                }
            }
        } finally {
            Files.deleteIfExists(archive);
        }
    }
}
