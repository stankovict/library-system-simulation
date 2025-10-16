package test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import manager.PosebnaKategorijaMetode;
import modeli.PosebnaKategorija;

class PosebnaKategorijaMetodeTest {

    @TempDir
    Path tempDir;

    Path testFile;
    PosebnaKategorijaMetode metode;

    @BeforeEach
    void setup() throws Exception {

        testFile = tempDir.resolve("posebnekategorije.csv");
        Files.writeString(testFile, "", StandardCharsets.UTF_8);

        metode = new PosebnaKategorijaMetode();

        Field fajlField = PosebnaKategorijaMetode.class.getDeclaredField("fajl");
        fajlField.setAccessible(true);
        fajlField.set(metode, testFile.toString());
    }

    @Test
    void testDodajKategorijuAndUcitaj() {
        metode.dodajKategoriju("Student", 20.0);
        metode.dodajKategoriju("Penzioner", 15.5);

        List<PosebnaKategorija> lista = metode.ucitajKategorije();
        assertEquals(2, lista.size());
        assertEquals("Student", lista.get(0).getKategorija());
        assertEquals(20.0, lista.get(0).getPopust(), 1e-9);
    }

    @Test
    void testIzmeniPopustSuccess() {
        metode.dodajKategoriju("VIP", 30.0);
        boolean updated = metode.izmeniPopust("VIP", 50.0);
        assertTrue(updated);

        List<PosebnaKategorija> lista = metode.ucitajKategorije();
        assertEquals(1, lista.size());
        assertEquals(50.0, lista.get(0).getPopust(), 1e-9);
    }

    @Test
    void testIzmeniPopustNotFound() {
        metode.dodajKategoriju("Standard", 5.0);
        boolean updated = metode.izmeniPopust("NonExistent", 10.0);
        assertFalse(updated);

        List<PosebnaKategorija> lista = metode.ucitajKategorije();
        assertEquals(1, lista.size());
        assertEquals(5.0, lista.get(0).getPopust(), 1e-9);
    }

    @Test
    void testUcitajKategorijeEmptyFile() throws IOException {
        Files.writeString(testFile, "", StandardCharsets.UTF_8);
        List<PosebnaKategorija> lista = metode.ucitajKategorije();
        assertTrue(lista.isEmpty());
    }
}
