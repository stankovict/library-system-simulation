package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import modeli.Kazna;

class KaznaTest {
    @Test
    void constructorAndGetters() {
        Kazna k = new Kazna("kazna", 12.34);
        assertEquals("kazna", k.getOpis());
        assertEquals(12.34, k.getIznos(), 1e-9);
    }
}
