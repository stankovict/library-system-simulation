package test;

import org.junit.jupiter.api.*;

import manager.DugMetode;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

class DugTest {

    private static final String TEST_DATA_DIR = "test_podaci";
    private static final String DUGOVI_FILE = TEST_DATA_DIR + "/dugovi.csv";
    private static final String ORIGINAL_FILE = "podaci/dugovi.csv";
    private static final String BACKUP_FILE = "podaci/dugovi_backup.csv";

    @BeforeAll
    public static void setUpClass() throws IOException {

        Files.createDirectories(Paths.get(TEST_DATA_DIR));
        Files.createDirectories(Paths.get("podaci"));

        if (Files.exists(Paths.get(ORIGINAL_FILE))) {
            Files.copy(Paths.get(ORIGINAL_FILE), Paths.get(BACKUP_FILE), 
                      StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @BeforeEach
    public void setUp() throws IOException {

        createTestDugoviFile();
    }

    @AfterEach
    public void tearDown() throws IOException {

        Files.deleteIfExists(Paths.get(ORIGINAL_FILE));
    }

    @AfterAll
    public static void tearDownClass() throws IOException {

        if (Files.exists(Paths.get(BACKUP_FILE))) {
            Files.move(Paths.get(BACKUP_FILE), Paths.get(ORIGINAL_FILE), 
                      StandardCopyOption.REPLACE_EXISTING);
        }
        

        Files.deleteIfExists(Paths.get(DUGOVI_FILE));
        Files.deleteIfExists(Paths.get(TEST_DATA_DIR));
    }

    private void createTestDugoviFile() throws IOException {
        String content = "korisnik1;1500.50\n" +
                        "korisnik2;2300.00\n" +
                        "korisnik3;0.00\n" +
                        "korisnik4;500.25\n";
        Files.write(Paths.get(ORIGINAL_FILE), content.getBytes(StandardCharsets.UTF_8));
    }


    @Test

    public void testUcitajDugExistingUser() {
        double dug = DugMetode.ucitajDug("korisnik1");
        assertEquals(1500.50, dug, 0.001);
    }

    @Test

    public void testUcitajDugZeroDebt() {
        double dug = DugMetode.ucitajDug("korisnik3");
        assertEquals(0.00, dug, 0.001);
    }

    @Test

    public void testUcitajDugNonExistingUser() {
        double dug = DugMetode.ucitajDug("nepostojeci");
        assertEquals(0.00, dug, 0.001);
    }

    @Test

    public void testUcitajDugDecimalValue() {
        double dug = DugMetode.ucitajDug("korisnik4");
        assertEquals(500.25, dug, 0.001);
    }

    @Test

    public void testUcitajDugNullUsername() {
        double dug = DugMetode.ucitajDug(null);
        assertEquals(0.00, dug, 0.001);
    }

    @Test

    public void testUcitajDugEmptyUsername() {
        double dug = DugMetode.ucitajDug("");
        assertEquals(0.00, dug, 0.001);
    }


    @Test

    public void testSacuvajDugExistingUser() {
        DugMetode.sacuvajDug("korisnik1", 2000.00);
        double dug = DugMetode.ucitajDug("korisnik1");
        assertEquals(2000.00, dug, 0.001);
    }



    @Test

    public void testOtplatiDugFull() {
        DugMetode.otplatiDug("korisnik1", 1500.50);
        double dug = DugMetode.ucitajDug("korisnik1");
        assertEquals(0.00, dug, 0.001);
    }


    @Test
 
    public void testOtplatiDugMultiplePayments() {
        DugMetode.otplatiDug("korisnik2", 300.00);
        assertEquals(2000.00, DugMetode.ucitajDug("korisnik2"), 0.001);
        
        DugMetode.otplatiDug("korisnik2", 500.00);
        assertEquals(1500.00, DugMetode.ucitajDug("korisnik2"), 0.001);
        
        DugMetode.otplatiDug("korisnik2", 1500.00);
        assertEquals(0.00, DugMetode.ucitajDug("korisnik2"), 0.001);
    }

 
}