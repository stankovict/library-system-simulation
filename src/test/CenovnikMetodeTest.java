package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import manager.CenovnikMetode;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import modeli.Kazna;
import modeli.Usluga;
import modeli.Cenovnik;

class CenovnikMetodeTest {

    @TempDir
    Path tempDir;

    @Test
    void testUcitajKazne() throws Exception {
        Path f = tempDir.resolve("kazne.csv");
        String csv = "penal;5.5\nkazna;10.0\n";
        Files.writeString(f, csv, StandardCharsets.UTF_8);

        List<Kazna> kazne = CenovnikMetode.ucitajKazne(f.toString());
        assertEquals(2, kazne.size());
        assertEquals("penal", kazne.get(0).getOpis());
        assertEquals(5.5, kazne.get(0).getIznos(), 1e-9);
    }

    @Test
    void testSacuvajKazneAndUcitajBack() throws Exception {
        Path f = tempDir.resolve("kazne2.csv");
        List<Kazna> list = List.of(new Kazna("x", 1.1), new Kazna("y", 2.2));

        CenovnikMetode.sacuvajKazne(f.toString(), list);
        List<Kazna> read = CenovnikMetode.ucitajKazne(f.toString());

        assertEquals(2, read.size());
        assertEquals("x", read.get(0).getOpis());
        assertEquals(1.1, read.get(0).getIznos(), 1e-9);
    }

    @Test
    void testUcitajUsluge() throws Exception {
        Path f = tempDir.resolve("usluge.csv");
        Files.writeString(f, "tren;10.0\nnesto;3.5\n", StandardCharsets.UTF_8);

        List<Usluga> usluge = CenovnikMetode.ucitajUsluge(f.toString());
        assertEquals(2, usluge.size());
        assertEquals("tren", usluge.get(0).getNaziv());
        assertEquals(10.0, usluge.get(0).getCena(), 1e-9);
    }

    @Test
    void testSacuvajUslugeAndUcitajBack() throws Exception {
        Path f = tempDir.resolve("usluge2.csv");
        List<Usluga> list = List.of(new Usluga("a", 5.0), new Usluga("b", 7.5));

        CenovnikMetode.sacuvajUsluge(f.toString(), list);
        List<Usluga> read = CenovnikMetode.ucitajUsluge(f.toString());

        assertEquals(2, read.size());
        assertEquals("a", read.get(0).getNaziv());
        assertEquals(5.0, read.get(0).getCena(), 1e-9);
    }

    @Test
    void testUcitajCenovniciAndTrenutniCenovnik() throws Exception {
        Path f = tempDir.resolve("cenovnici.csv");
        LocalDate od = LocalDate.now().minusDays(1);
        LocalDate doD = LocalDate.now().plusDays(1);

        String line = "1;" + od.toString() + ";" + doD.toString() + ";;kazna1,5.0|kazna2,2.5;usl1,10.0\n";
        Files.writeString(f, line, StandardCharsets.UTF_8);

        List<Cenovnik> cenovnici = CenovnikMetode.ucitajCenovnike(f.toString());
        assertEquals(1, cenovnici.size());
        Cenovnik c = cenovnici.get(0);
        assertEquals(od, c.getDatumOd());
        assertEquals(doD, c.getDatumDo());


        Cenovnik current = CenovnikMetode.ucitajTrenutniCenovnik(f.toString());
        assertNotNull(current);
    }

    @Test
    void testSacuvajCenovniciWritesExpectedContent() throws Exception {
        Path f = tempDir.resolve("cenovnici.csv");
        LocalDate od = LocalDate.of(2025, 1, 1);
        LocalDate doD = LocalDate.of(2025, 12, 31);

        Cenovnik c = new Cenovnik(42, od, doD);


        CenovnikMetode.sacuvajCenovnike(f.toString(), List.of(c));
        String content = Files.readString(f, StandardCharsets.UTF_8);
        assertTrue(content.contains("42;"));
        assertTrue(content.contains(od.toString()));
        assertTrue(content.contains(doD.toString()));
    }
}
