package test;

import manager.BibliotekarMetode;
import enumi.Pol;
import enumi.StrucnaSprema;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BibliotekarTest {

    private static final String TEST_FILE = "test_bibliotekari.csv";

    @BeforeEach
    void setUp() throws IOException {

        Files.deleteIfExists(Paths.get(TEST_FILE));
        Files.createFile(Paths.get(TEST_FILE));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE));
    }

    @Test
    void testDodajBibliotekara() throws IOException {
        BibliotekarMetode.dodajBibliotekara(
                TEST_FILE,
                "Marko",
                "Markovic",
                Pol.M,
                LocalDate.of(1990, 1, 1),
                "123456",
                "Adresa 1",
                "korisnik1",
                "lozinka",
                StrucnaSprema.VI,
                5
        );

        String content = Files.readString(Paths.get(TEST_FILE));
        assertTrue(content.contains("korisnik1"));
        assertTrue(content.contains("Marko"));
    }

    @Test
    void testObrisiBibliotekara() throws IOException {

        String line = "Marko;Markovic;M;1990-01-01;123456;Adresa 1;korisnik1;lozinka;VI;5";
        Files.writeString(Paths.get(TEST_FILE), line + System.lineSeparator());

        ByteArrayInputStream in = new ByteArrayInputStream("korisnik1\n".getBytes());
        System.setIn(in);

        BibliotekarMetode.obrisiBibliotekara(TEST_FILE);

        String content = Files.readString(Paths.get(TEST_FILE));
        assertFalse(content.contains("korisnik1"));
    }

    @Test
    void testObrisiNepostojeciBibliotekar() throws IOException {
        String line = "Pera;Peric;M;1992-02-02;654321;Adresa 2;peraUser;pass;VI;3";
        Files.writeString(Paths.get(TEST_FILE), line + System.lineSeparator());

        ByteArrayInputStream in = new ByteArrayInputStream("nepostoji\n".getBytes());
        System.setIn(in);

        BibliotekarMetode.obrisiBibliotekara(TEST_FILE);

        String content = Files.readString(Paths.get(TEST_FILE));
        assertTrue(content.contains("peraUser"));
    }
}
