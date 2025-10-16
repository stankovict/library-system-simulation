package test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import enumi.Pol;
import enumi.StrucnaSprema;
import manager.AdminMetode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;

class AdminTest {

    private static final String TEST_FILE = "test_admini.csv";

    @BeforeEach
    void setup() throws IOException {

        Files.writeString(Paths.get(TEST_FILE), "", StandardCharsets.UTF_8);
    }

    @AfterEach
    void cleanup() throws IOException {

        Files.deleteIfExists(Paths.get(TEST_FILE));
    }

    @Test
    void testDodajAdmina() throws IOException {
        AdminMetode.dodajAdmina(
            TEST_FILE,
            "Milan",
            "Petrovic",
            Pol.M,
            LocalDate.of(1990, 5, 12),
            "0651234567",
            "Beograd",
            "milanp",
            "1234",
            StrucnaSprema.III,
            5
        );


        String content = Files.readString(Paths.get(TEST_FILE));
        assertTrue(content.contains("Milan"));
        assertTrue(content.contains("Petrovic"));
        assertTrue(content.contains("milanp"));
    }

    @Test
    void testObrisiAdmina() throws IOException {

        Files.writeString(Paths.get(TEST_FILE),
                "Milan;Petrovic;M;1990-05-12;0651234567;Beograd;milanp;1234;FAKULTET;5\n",
                StandardCharsets.UTF_8);

        InputStream sysInBackup = System.in;
        ByteArrayInputStream in = new ByteArrayInputStream("milanp\n".getBytes());
        System.setIn(in);

        AdminMetode.obrisiAdmina(TEST_FILE);

        System.setIn(sysInBackup);

        String content = Files.readString(Paths.get(TEST_FILE));
        assertFalse(content.contains("milanp"));
    }

}
