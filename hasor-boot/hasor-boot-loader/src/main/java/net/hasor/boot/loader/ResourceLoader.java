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
package net.hasor.boot.loader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.jar.Manifest;
/**
 * Minimal resource abstraction used by the executable jar launcher.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2026-06-19
 */
public interface ResourceLoader extends Closeable {
    URL getResource(String resource) throws IOException;

    InputStream getResourceAsStream(String resource) throws IOException;

    long getResourceSize(String resource) throws IOException;

    List<URL> getResources(String resource) throws IOException;

    boolean exist(String resource);

    Manifest getManifest(String resource) throws IOException;

    @Override
    default void close() throws IOException {
        // no-op
    }
}
