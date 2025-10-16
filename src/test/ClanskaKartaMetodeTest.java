package test;

import enumi.Clanarina;
import manager.ClanskaKartaMetode;
import modeli.ClanskaKarta;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClanskaKartaMetodeTest {

    private static final String TEST_FILE = "test_clanske_karte.csv";

    @BeforeEach
    void setUp() throws IOException {

        Files.deleteIfExists(Paths.get(TEST_FILE));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(TEST_FILE))) {
            bw.write("pera;mesečna;2025-09-01;2025-10-01\n");
            bw.write("mika;godišnja;2025-01-01;2026-01-01\n");
        }
    }

    @AfterEach
    void cleanUp() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_FILE));
    }

    @Test
    void testUcitajKarte() {
        List<ClanskaKarta> karte = ClanskaKartaMetode.ucitajKarte(TEST_FILE);
        assertEquals(2, karte.size());
        assertEquals("pera", karte.get(0).getKorisnickoIme());
        assertEquals(Clanarina.mesečna, karte.get(0).getTipClanarine());
    }

    @Test
    void testSacuvajKarte() throws IOException {
        List<ClanskaKarta> karte = ClanskaKartaMetode.ucitajKarte(TEST_FILE);
        assertFalse(karte.isEmpty());

        karte.get(0).setDatumIsteka(LocalDate.of(2025, 12, 31));
        ClanskaKartaMetode.sacuvajKarte(TEST_FILE, karte);

        List<String> lines = Files.readAllLines(Paths.get(TEST_FILE));
        assertTrue(lines.get(0).contains("2025-12-31"));
    }

    @Test
    void testDodajKartu() {
        ClanskaKarta nova = new ClanskaKarta("zika", Clanarina.mesečna, LocalDate.of(2025, 9, 30));
        ClanskaKartaMetode.dodajKartu(TEST_FILE, nova);

        List<ClanskaKarta> karte = ClanskaKartaMetode.ucitajKarte(TEST_FILE);
        assertEquals(3, karte.size());
        assertNotNull(
                karte.stream().filter(k -> k.getKorisnickoIme().equals("zika")).findFirst().orElse(null)
        );
    }

    @Test
    void testPronadjiPoKorisniku() {
        ClanskaKarta karta = ClanskaKartaMetode.pronadjiPoKorisniku(TEST_FILE, "mika");
        assertNotNull(karta);
        assertEquals(Clanarina.godišnja, karta.getTipClanarine());

        ClanskaKarta nepostojeci = ClanskaKartaMetode.pronadjiPoKorisniku(TEST_FILE, "neko");
        assertNull(nepostojeci);
    }

    @Test
    void testKorisnikImaVazecu() {
        assertTrue(ClanskaKartaMetode.korisnikImaVazecu(TEST_FILE, "mika"));
        assertFalse(ClanskaKartaMetode.korisnikImaVazecu(TEST_FILE, "neko"));
    }
}
