package io.github.bekoenig.getdown.tests;

import io.github.bekoenig.getdown.data.Application;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ApplicationIT {

    @Test
    public void testIsVerify() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        // GIVEN
        KeyStore sr = KeyStore.getInstance("JKS");

        InputStream keystore = Files.newInputStream(new File("src/it/resources/testapp-keystore.jks").toPath());
        sr.load(keystore, ("abcde123").toCharArray());
        X509Certificate cert = (X509Certificate)sr.getCertificate("testdomain");
        List<Certificate> certs = Collections.singletonList(cert);

        int sigVersion = 2;

        Path signatureFile = Paths.get("src/it/resources/digest2.txt.sig");
        Files.write(signatureFile, Arrays.asList(
            "DBcOqsMjyE2syPIqJzJuYLf5gj41sSz5trsRzeLdJNN8H2eyXFU8BatFvpLY2OiLr+SZrnFZMARi",
            "rcGTk6n8IdxiJJD4qK9PRqw0jPbh7EYfP/f0D48n4QsgnTiRekCND4RvMaqxXjVopnjrh5NQr/ZM",
            "IV6V8bl9HkeiVSOkw/gwRZOG9ljElqEZ4NzIeo79y4XljPGjQW9CoYz5cAfVyW9OHkbFi+Gp4u0l",
            "fQTjI0XHNTFBv1Ccs7hhy18C7t99dKUKVYIgZMAzqSmrodltCY6JxLqVbdAvnW5Z3A45lZD9PHCL",
            "Ne5NivlUmq9V6tczcqcurL3mzWMinFEpuTLNtA=="));

        Path target = Paths.get("src/it/resources/digest2.txt");
        Files.write(target, Arrays.asList(
            "getdown.txt = 1efecfae2a189002a6658f17d162b1922c7bde978944949276dc038a0df2461f",
            "testapp.jar = c9cb1906afbf48f8654b416c3f831046bd3752a76137e5bf0a9af2f790bf48e0",
            "funny%test dir/some=file.txt = f2ca1bb6c7e907d06dafe4687e579fce76b37e4e93b7605022da52e6ccc26fd2",
            "crazyhashfile#txt = 6816889f922de38f145db215a28ad7c5e1badf7354b5cdab225a27486789fa3b",
            "foo.jar = ea188b872e0496debcbe00aaadccccb12a8aa9b025bb62c130cd3d9b8540b062",
            "script.sh = cca1c5c7628d9bf7533f655a9cfa6573d64afb8375f81960d1d832dc5135c988",
            "digest2.txt = 41eacdabda8909bdbbf61e4f980867f4003c16a12f6770e6fc619b6af100e05b"
        ));

        // WHEN
        Application.verifySignature(certs, sigVersion, signatureFile.toFile(), target.toFile());

        // THEN
        Files.deleteIfExists(signatureFile);
        Files.deleteIfExists(target);
    }

}
