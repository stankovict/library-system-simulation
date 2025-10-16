package test;

import org.junit.jupiter.api.*;

import manager.LoginManager;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

class LoginManagerTest {

    private LoginManager loginManager;
    private static final String TEST_DATA_DIR = "test_podaci";
    private static final String ADMIN_FILE = TEST_DATA_DIR + "/admini.csv";
    private static final String BIBLIOTEKAR_FILE = TEST_DATA_DIR + "/bibliotekari.csv";
    private static final String CLAN_FILE = TEST_DATA_DIR + "/clanovi.csv";

    @BeforeAll
    public static void setUpClass() throws IOException {

        Files.createDirectories(Paths.get(TEST_DATA_DIR));
    }

    @BeforeEach
    public void setUp() throws IOException {
        loginManager = new LoginManager();
        

        createTestAdminFile();
        createTestBibliotekarFile();
        createTestClanFile();
    }

    @AfterEach
    public void tearDown() throws IOException {

        Files.deleteIfExists(Paths.get(ADMIN_FILE));
        Files.deleteIfExists(Paths.get(BIBLIOTEKAR_FILE));
        Files.deleteIfExists(Paths.get(CLAN_FILE));
    }

    @AfterAll
    public static void tearDownClass() throws IOException {

        Files.deleteIfExists(Paths.get(TEST_DATA_DIR));
    }

    private void createTestAdminFile() throws IOException {
        String content = "Marko;Markovic;1234567890123;MUSKI;1990-01-01;Novi Sad;admin@biblioteka.rs;admin123\n" +
                        "Ana;Anic;9876543210987;ZENSKI;1985-05-15;Beograd;ana.admin@biblioteka.rs;ana456\n";
        Files.write(Paths.get(ADMIN_FILE), content.getBytes(StandardCharsets.UTF_8));
    }

    private void createTestBibliotekarFile() throws IOException {
        String content = "Petar;Petrovic;1111111111111;MUSKI;1988-03-20;Nis;petar.bib@biblioteka.rs;petar789\n" +
                        "Jovana;Jovanovic;2222222222222;ZENSKI;1992-07-10;Subotica;jovana.bib@biblioteka.rs;jovana321\n";
        Files.write(Paths.get(BIBLIOTEKAR_FILE), content.getBytes(StandardCharsets.UTF_8));
    }

    private void createTestClanFile() throws IOException {
        String content = "Milan;Milanovic;3333333333333;MUSKI;1995-11-25;Kragujevac;milan.clan@email.rs;milan555\n" +
                        "Jelena;Jelenic;4444444444444;ZENSKI;1998-02-14;Leskovac;jelena.clan@email.rs;jelena888\n";
        Files.write(Paths.get(CLAN_FILE), content.getBytes(StandardCharsets.UTF_8));
    }

    @Test

    public void testValidAdminLogin() {

        LoginManager.TipKorisnika result = loginManager.proveriLogin("admin@biblioteka.rs", "admin123");
        

        assertNotNull(result);
    }

    @Test

    public void testValidBibliotekarLogin() {
        LoginManager.TipKorisnika result = loginManager.proveriLogin("petar.bib@biblioteka.rs", "petar789");
        assertNotNull(result);
    }

    @Test

    public void testValidClanLogin() {
        LoginManager.TipKorisnika result = loginManager.proveriLogin("milan.clan@email.rs", "milan555");
        assertNotNull(result);
    }

    @Test

    public void testInvalidEmail() {
        LoginManager.TipKorisnika result = loginManager.proveriLogin("nepostoji@email.rs", "lozinka123");
        assertEquals(LoginManager.TipKorisnika.NEPOSTOJI, result);
    }

    @Test

    public void testInvalidPassword() {
        LoginManager.TipKorisnika result = loginManager.proveriLogin("admin@biblioteka.rs", "wrongpassword");
        assertEquals(LoginManager.TipKorisnika.NEPOSTOJI, result);
    }

    @Test

    public void testEmptyEmail() {
        LoginManager.TipKorisnika result = loginManager.proveriLogin("", "admin123");
        assertEquals(LoginManager.TipKorisnika.NEPOSTOJI, result);
    }

    @Test

    public void testEmptyPassword() {
        LoginManager.TipKorisnika result = loginManager.proveriLogin("admin@biblioteka.rs", "");
        assertEquals(LoginManager.TipKorisnika.NEPOSTOJI, result);
    }

    @Test

    public void testNullEmail() {
        LoginManager.TipKorisnika result = loginManager.proveriLogin(null, "admin123");
        assertEquals(LoginManager.TipKorisnika.NEPOSTOJI, result);
    }

    @Test

    public void testNullPassword() {
        LoginManager.TipKorisnika result = loginManager.proveriLogin("admin@biblioteka.rs", null);
        assertEquals(LoginManager.TipKorisnika.NEPOSTOJI, result);
    }


}