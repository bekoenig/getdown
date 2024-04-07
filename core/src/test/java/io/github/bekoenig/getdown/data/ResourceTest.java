package io.github.bekoenig.getdown.data;

import io.github.bekoenig.getdown.util.ProgressObserver;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceTest {

    static final ProgressObserver PROGRESS_OBSERVER = x -> {};

    @Test
    void testComputeDigest_ReadableDat() throws IOException {
        // GIVEN
        int version = 2;
        File target = new File(getClass().getClassLoader().getResource("io/github/bekoenig/getdown/data/readable.dat").getFile());
        MessageDigest md = Digest.getMessageDigest(version);

        // WHEN
        String digest = Resource.computeDigest(version, target, md, PROGRESS_OBSERVER);

        // THEN
        assertEquals("dc633dd8ca1e1dbb0c158e28393470b490047d230b14e65b0b6a76f50856f77f", digest);
    }

    @Test
    void testComputeDigest_ReadableZip_Version1() throws IOException {
        // GIVEN
        int version = 1;
        File target = new File(getClass().getClassLoader().getResource("io/github/bekoenig/getdown/data/readable.zip").getFile());
        MessageDigest md = Digest.getMessageDigest(version);

        // WHEN
        String digest = Resource.computeDigest(version, target, md, PROGRESS_OBSERVER);

        // THEN
        assertEquals("55986f63c9d781f308adc449de66e1c6", digest);
    }

    @Test
    void testComputeDigest_ReadableZip_Version2() throws IOException {
        // GIVEN
        int version = 2;
        File target = new File(getClass().getClassLoader().getResource("io/github/bekoenig/getdown/data/readable.zip").getFile());
        MessageDigest md = Digest.getMessageDigest(version);

        // WHEN
        String digest = Resource.computeDigest(version, target, md, PROGRESS_OBSERVER);

        // THEN
        assertEquals("8b4320de6414562973ae82f332caca93677d95ef4655cb3f4de25770ecbad694", digest);
    }

    @Test
    void testComputeDigest_InvalidZip() throws IOException {
        // GIVEN
        int version = 2;
        File target = new File(getClass().getClassLoader().getResource("io/github/bekoenig/getdown/data/invalid.zip").getFile());
        MessageDigest md = Digest.getMessageDigest(version);

        // WHEN
        String digest = Resource.computeDigest(version, target, md, PROGRESS_OBSERVER);

        // THEN
        assertEquals("817504a0e322027ceb285737954f16fa244d3660a20be040035868f41d65357b", digest);
    }

    @Test
    void testComputeDigest_EncryptedZip() throws IOException {
        // GIVEN
        int version = 2;
        File target = new File(getClass().getClassLoader().getResource("io/github/bekoenig/getdown/data/encrypted.zip").getFile());
        MessageDigest md = Digest.getMessageDigest(version);

        // WHEN
        String digest = Resource.computeDigest(version, target, md, PROGRESS_OBSERVER);

        // THEN
        assertEquals("b2b2fe8f25268a3f3040f2d2026dded5a5de77649d5700540e2be52304f636ed", digest);
    }

    @Test
    void testComputeDigest_StrongEncryptionZip() throws IOException {
        // GIVEN
        int version = 2;
        File target = new File(getClass().getClassLoader().getResource("io/github/bekoenig/getdown/data/strong-encryption.zip").getFile());
        MessageDigest md = Digest.getMessageDigest(version);

        // WHEN
        String digest = Resource.computeDigest(version, target, md, PROGRESS_OBSERVER);

        // THEN
        assertEquals("b88a53f07c9210de6706d7dbc632d9cde6468ad301390c93c41c823ae58ee28d", digest);
    }

}
