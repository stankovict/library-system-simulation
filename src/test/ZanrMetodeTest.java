package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import modeli.Zanr;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import manager.ZanrMetode;

class ZanrMetodeTest {

    @TempDir
    Path tempDir;

    Path zanroviFile;

    @BeforeEach
    void setup() {
        zanroviFile = tempDir.resolve("zanrovi.csv");

        ZanrMetode.setFilePath(zanroviFile.toString());
    }

    @Test
    void testDodajZanrAndUcitajZanrove() throws IOException {

        ZanrMetode.dodajZanr(new Zanr("Fantastika"));
        ZanrMetode.dodajZanr(new Zanr("Drama"));

        List<String> lines = Files.readAllLines(zanroviFile, StandardCharsets.UTF_8);
        assertEquals(2, lines.size());
        assertEquals("Fantastika", lines.get(0));
        assertEquals("Drama", lines.get(1));

        List<Zanr> ucitani = ZanrMetode.ucitajZanrove();
        assertEquals(2, ucitani.size());
        assertEquals("Fantastika", ucitani.get(0).getNaziv());
        assertEquals("Drama", ucitani.get(1).getNaziv());
    }
}
