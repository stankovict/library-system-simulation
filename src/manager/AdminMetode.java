package manager;

import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;

import enumi.StrucnaSprema;
import modeli.Administrator;
import enumi.Pol;

import java.io.*;
import java.util.*;

public class AdminMetode {

	public static void dodajAdmina(String fajl,
            String ime,
            String prezime,
            Pol pol,
            LocalDate datumRodjenja,
            String telefon,
            String adresa,
            String korisnickoIme,
            String lozinka,
            StrucnaSprema strucnaSprema,
            int godineStaza) {

	Administrator admin = new Administrator(
	ime, prezime, pol, datumRodjenja, telefon, adresa,
	korisnickoIme, lozinka, strucnaSprema, godineStaza
	);
	
	admin.obracunajPlatu();
	
	try (OutputStreamWriter writer = new OutputStreamWriter(
	new FileOutputStream(fajl, true), StandardCharsets.UTF_8)) {
	writer.write(admin.toCSV());
	writer.write(System.lineSeparator());
	System.out.println("Administrator uspešno dodat i upisan u fajl");
	} catch (IOException e) {
	System.out.println("Greška pri upisu u fajl: " + e.getMessage());
	}
	}
    
    public static void obrisiAdmina(String fajl) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Unesite korisničko ime administratora za brisanje: ");
        String korisnickoIme = sc.nextLine();

        List<String> linije = new ArrayList<>();
        boolean obrisan = false;


        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajl), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {

                String[] delovi = line.split(";");
                if (delovi.length > 6) {
                    String korisnikIzFajla = delovi[6].trim(); 
                    if (korisnikIzFajla.equalsIgnoreCase(korisnickoIme.trim())) {
                        obrisan = true;
                        continue;
                    }
                }
                linije.add(line);
            }
        } catch (IOException e) {
            System.out.println("Greška pri čitanju fajla: " + e.getMessage());
            return;
        }


        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fajl, false), StandardCharsets.UTF_8))) {

            for (String l : linije) {
                bw.write(l);
                bw.newLine();
            }

            if (obrisan) {
                System.out.println("Administrator sa korisničkim imenom '" + korisnickoIme + "' je uspešno obrisan.");
            } else {
                System.out.println("Nema administratora sa korisničkim imenom '" + korisnickoIme + "'.");
            }

        } catch (IOException e) {
            System.out.println("Greška pri upisu u fajl: " + e.getMessage());
        }
    }
    
    public static void ucitajIZapisi(String fajl, String uloga) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(fajl), StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";");
                if (delovi.length > 7) {
                    String korisnickoIme = delovi[0].trim();
                    String lozinka = delovi[1].trim();
                    System.out.printf("%-12s %-15s %-15s\n", uloga, korisnickoIme, lozinka);
                }
            }
        } catch (IOException e) {
            System.out.println("Greška pri čitanju fajla " + fajl + ": " + e.getMessage());
        }
    }
    
    public static void prikaziZaposlene() {
        System.out.println("==============================================");
        System.out.printf("%-12s %-15s %-15s\n", "Uloga", "Ime", "Prezime");
        System.out.println("==============================================");

        ucitajIZapisi("podaci/admini.csv", "Admin");
        ucitajIZapisi("podaci/bibliotekari.csv", "Bibliotekar");

        System.out.println("==============================================");
    }
}