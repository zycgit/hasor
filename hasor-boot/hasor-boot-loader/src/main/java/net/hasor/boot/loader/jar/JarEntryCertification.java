/*
 * Copyright 2015-2022 the original author or authors.
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.boot.loader.jar;
import java.security.CodeSigner;
import java.security.cert.Certificate;

/**
 * {@link Certificate} and {@link CodeSigner} details for a {@link JarEntry} from a signed
 * {@link JarFile}.
 * @author Phillip Webb
 */
record JarEntryCertification(Certificate[] certificates, CodeSigner[] codeSigners) {
    static final JarEntryCertification NONE = new JarEntryCertification(null, null);

    @Override
    public Certificate[] certificates() {
        return (this.certificates != null) ? this.certificates.clone() : null;
    }

    @Override
    public CodeSigner[] codeSigners() {
        return (this.codeSigners != null) ? this.codeSigners.clone() : null;
    }

    static JarEntryCertification from(java.util.jar.JarEntry certifiedEntry) {
        Certificate[] certificates = (certifiedEntry != null) ? certifiedEntry.getCertificates() : null;
        CodeSigner[] codeSigners = (certifiedEntry != null) ? certifiedEntry.getCodeSigners() : null;
        if (certificates == null && codeSigners == null) {
            return NONE;
        }
        return new JarEntryCertification(certificates, codeSigners);
    }
}
