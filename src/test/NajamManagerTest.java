package test;

import org.junit.jupiter.api.*;

import manager.NajamManager;

import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class NajamManagerTest {

    private static final String TEST_FILE = "podaci/najam.csv";
    private static Path originalFile;

    @BeforeAll
    static void backupFile() throws IOException {

        Path path = Paths.get(TEST_FILE);
        if (Files.exists(path)) {
            originalFile = Files.createTempFile("najam_backup", ".csv");
            Files.copy(path, originalFile, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.createDirectories(path.getParent());
        }
    }

    @AfterAll
    static void restoreFile() throws IOException {

        if (originalFile != null && Files.exists(originalFile)) {
            Files.copy(originalFile, Paths.get(TEST_FILE), StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(originalFile);
        } else {
            Files.deleteIfExists(Paths.get(TEST_FILE));
        }
    }

    @Test
    void testUcitajDefaultNajam_withValidFile() throws IOException {
        Files.write(Paths.get(TEST_FILE), "10".getBytes());

        int result = NajamManager.ucitajDefaultNajam();
        assertEquals(10, result, "Treba da čita 10");
    }

    @Test
    void testUcitajDefaultNajam_withMissingFile() throws IOException  {
        Files.deleteIfExists(Paths.get(TEST_FILE));

        int result = NajamManager.ucitajDefaultNajam();
        assertEquals(7, result, "Ako nema fajla vrati 7");
    }

    @Test
    void testPostaviDefaultNajam_andThenRead() {
        boolean saved = NajamManager.postaviDefaultNajam(15);
        assertTrue(saved, "Treba da sačuva");

        int result = NajamManager.ucitajDefaultNajam();
        assertEquals(15, result, "Treba da učita 15 nakon upisa");
    }
}
