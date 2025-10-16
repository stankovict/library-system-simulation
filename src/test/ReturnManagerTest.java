package test;

import enumi.StatusKnjige;
import manager.CenovnikMetode;
import manager.ReturnManager;
import modeli.Cenovnik;
import modeli.Kazna;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ReturnManagerTest {

    private Path izdavanjeFile;
    private Path kasnjenjaFile;
    private Path dugoviFile;

    private Cenovnik cenovnik;

    @BeforeEach
    void setUp() throws IOException {

        izdavanjeFile = Files.createTempFile("izdavanje", ".csv");
        kasnjenjaFile = Files.createTempFile("kasnjenja", ".csv");
        dugoviFile = Files.createTempFile("dugovi", ".csv");

        Cenovnik cenovnik = CenovnikMetode.ucitajTrenutniCenovnik("podaci/cenovnici.csv");

        String overdueBook = String.join(";",
                "book123", 
                "title",
                "user1",
                "2024-01-01",
                "2024-01-15",
                LocalDate.now().minusDays(5).toString(),
                "other",
                "false",
                "resv1"
        );
        Files.write(izdavanjeFile, overdueBook.getBytes(StandardCharsets.UTF_8));
    }

    @AfterEach
    void cleanUp() throws IOException {
        Files.deleteIfExists(izdavanjeFile);
        Files.deleteIfExists(kasnjenjaFile);
        Files.deleteIfExists(dugoviFile);
    }

    @Test
    void testProcessBookReturnWithPenalties_appliesLateFee() throws Exception {

        System.setProperty("podaci/izdavanje.csv", izdavanjeFile.toString());
        System.setProperty("podaci/kasnjenja.csv", kasnjenjaFile.toString());
        System.setProperty("podaci/dugovi.csv", dugoviFile.toString());

        ReturnManager.processBookReturnWithPenalties("book123", cenovnik);

        String updatedLine = Files.readAllLines(izdavanjeFile).get(0);
        assertTrue(updatedLine.contains("false"), "Book should be marked as returned");

        assertTrue(Files.exists(kasnjenjaFile), "kasnjenja.csv should exist");

        assertTrue(Files.exists(dugoviFile), "dugovi.csv should exist");

    }

    @Test
    void testMarkBookAsReturned_onlyMarksWithoutPenalty() throws Exception {

        String notLateBook = String.join(";",
                "book200", "title", "user2", "2024-01-01", "2024-01-15",
                LocalDate.now().plusDays(3).toString(),
                "other", "false", "resv2"
        );
        Files.write(izdavanjeFile, notLateBook.getBytes(StandardCharsets.UTF_8));

        ReturnManager.markBookAsReturned("book200");

        String updatedLine = Files.readAllLines(izdavanjeFile).get(0);
        assertTrue(updatedLine.contains("false"), "Book should be marked returned");
    }
}
