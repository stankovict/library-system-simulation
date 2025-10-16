package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import manager.UlogovaniKorisnik;

class UlogovaniKorisnikTest {

    @BeforeEach
    void setup() {

        UlogovaniKorisnik.logout();
    }

    @Test
    void testSetAndGetUsername() {
        UlogovaniKorisnik.setUsername("user1");
        assertEquals("user1", UlogovaniKorisnik.getUsername(), "Username treba da se poklapa sa postavljenom vrednoscu");

        UlogovaniKorisnik.setUsername("admin");
        assertEquals("admin", UlogovaniKorisnik.getUsername(), "Username treba da se apdejtuje");
    }

    @Test
    void testLogout() {
        UlogovaniKorisnik.setUsername("user1");
        UlogovaniKorisnik.logout();
        assertNull(UlogovaniKorisnik.getUsername(), "Username treba da bude null");
    }
}
