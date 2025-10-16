package manager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import modeli.Cenovnik;
import modeli.Kazna;
import enumi.StatusKnjige;
import gui.IzdavanjeDialog;

public class ReturnManager {

	public static void processBookReturnWithPenalties(String bookId, Cenovnik trenutniCenovnik) throws IOException {
	    String fajlIzdavanje = "podaci/izdavanje.csv";
	    String fajlKasnjenja = "podaci/kasnjenja.csv";
	    String fajlDugovi = "podaci/dugovi.csv";
	    
	    List<String> linije = Files.readAllLines(Paths.get(fajlIzdavanje), StandardCharsets.UTF_8);
	    LocalDate danas = LocalDate.now();
	    boolean bookFound = false;
	    
	    for (int i = 0; i < linije.size(); i++) {
	        String linija = linije.get(i);
	        String[] delovi = linija.split(";");

	        if (delovi.length >= 9) {
	            String fileBookId = delovi[0].trim();
	            String fileUser = delovi[2].trim();
	            String returnedStatus = delovi[7].trim();
	            
	            if (fileBookId.trim().equals(bookId) && 
	                 
	                delovi[7].trim().equalsIgnoreCase("false")) {
	                
	                try {
	                	LocalDate expectedReturnDate = null;
	                	String[] patterns = {"d.M.yyyy", "dd.MM.yyyy","yyyy-MM-dd"};
	                    for (String p : patterns) {
	                        try {
	                            java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern(p);
	                            expectedReturnDate = LocalDate.parse(delovi[5].trim(),f);
	                            
	                            break;
	                        } catch (Exception ignored) {}
	                    }
	                    
	                    String reservationId = delovi[8].trim();

	                    long daysLate = java.time.temporal.ChronoUnit.DAYS.between(expectedReturnDate, danas);
	                    
	                    if (daysLate > 0) {

	                        processLateReturn(fileUser, daysLate, reservationId, trenutniCenovnik, 
	                                        fajlKasnjenja, fajlDugovi, danas);
	                    }

	                    delovi[7] = "true";
	                    linije.set(i, String.join(";", delovi));
	                    bookFound = true;
	                    break;
	                    
	                } catch (Exception e) {

	                    continue;
	                }
	            }
	        }
	    }
	    
	    if (bookFound) {

	        Files.write(Paths.get(fajlIzdavanje), linije, StandardCharsets.UTF_8);

	        IzdavanjeDialog.updateBookStatus(bookId, StatusKnjige.DOSTUPNA);
	    }
	}

	private static void processLateReturn(String username, long daysLate, String reservationId, 
	                                    Cenovnik trenutniCenovnik, String fajlKasnjenja, 
	                                    String fajlDugovi, LocalDate danas) throws IOException {

	    double dailyLateFee = 0.0;
	    for (Kazna kazna : trenutniCenovnik.getKazne()) {
	        if ("Kašnjenje".equalsIgnoreCase(kazna.getOpis()) || 
	            kazna.getOpis().toLowerCase().contains("kašnjen")) {
	            dailyLateFee = kazna.getIznos();
	            break;
	        }
	    }
	    
	    if (dailyLateFee == 0.0) {

	        dailyLateFee = 50.0;
	    }
	    
	    if (daysLate <= 30) {

	        double totalFine = dailyLateFee * daysLate;

	        String kasnjenjeEntry = String.format("%s;%s;%.2f\n", username, danas, totalFine);
	        Files.write(Paths.get(fajlKasnjenja), kasnjenjeEntry.getBytes(StandardCharsets.UTF_8),
	                   java.nio.file.StandardOpenOption.CREATE,
	                   java.nio.file.StandardOpenOption.APPEND);
	        addOrUpdateDebt(username, totalFine, fajlDugovi);
	        System.out.println(String.format("Late fee applied: %s - %.2f RSD for %d days late", 
	                                        username, totalFine, daysLate));
	        
	    } else {

	        double monthlyFine = dailyLateFee * 30;
	        double severePenalty = monthlyFine + 2000.0;

	        addOrUpdateDebt(username, severePenalty, fajlDugovi);

	        String kasnjenjeEntry = String.format("%s;0;%.2f\n", username, reservationId);
	        Files.write(Paths.get(fajlKasnjenja), kasnjenjeEntry.getBytes(StandardCharsets.UTF_8),
	                   java.nio.file.StandardOpenOption.CREATE,
	                   java.nio.file.StandardOpenOption.APPEND);
	        
	        System.out.println(String.format("Severe late penalty applied: %s - %.2f RSD added to debt for %d days late", 
	                                        username, severePenalty, daysLate));
	    }
	}

	private static void addOrUpdateDebt(String username, double amount, String fajlDugovi) throws IOException {
	    List<String> linije = new ArrayList<>();
	    boolean userFound = false;

	    try (BufferedReader br = new BufferedReader(new InputStreamReader(
	            new FileInputStream(fajlDugovi), StandardCharsets.UTF_8))) {
	        String line;
	        while ((line = br.readLine()) != null) {
	            String[] delovi = line.split(";");
	            if (delovi.length >= 2) {
	                String fileUsername = delovi[0].trim();
	                if (fileUsername.equalsIgnoreCase(username)) {

	                    try {
	                        double existingDebt = Double.parseDouble(delovi[1].trim());
	                        double newDebt = existingDebt + amount;
	                        linije.add(username + ";" + String.format("%.2f", newDebt));
	                        userFound = true;
	                    } catch (NumberFormatException e) {

	                        linije.add(username + ";" + String.format("%.2f", amount));
	                        userFound = true;
	                    }
	                } else {
	                    linije.add(line);
	                }
	            }
	        }
	    } catch (IOException e) {

	    }

	    if (!userFound) {
	        linije.add(username + ";" + String.format("%.2f", amount));
	    }

	    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
	            new FileOutputStream(fajlDugovi), StandardCharsets.UTF_8))) {
	        for (String linija : linije) {
	            bw.write(linija);
	            bw.newLine();
	        }
	    }
	}

	public static void markBookAsReturned(String bookId) throws IOException {
	    String fajlIzdavanje = "podaci/izdavanje.csv";
	    List<String> linije = Files.readAllLines(Paths.get(fajlIzdavanje), StandardCharsets.UTF_8);
	    
	    for (int i = 0; i < linije.size(); i++) {
	        String linija = linije.get(i);
	        String[] delovi = linija.split(";");
	        
	        if (delovi.length >= 8) {
	            String fileBookId = delovi[0].trim();
	            
	            if (fileBookId.trim().equals(bookId.trim()) &&
	                delovi[7].trim().equalsIgnoreCase("false")) {
	                
	                delovi[7] = "true";
	                linije.set(i, String.join(";", delovi));
	                IzdavanjeDialog.updateBookStatus(bookId, StatusKnjige.DOSTUPNA);
	                break;
	            }
	        }
	    }
	    
	    Files.write(Paths.get(fajlIzdavanje), linije, StandardCharsets.UTF_8);
	}


	public static void processBookReturn(String bookId, String username, Cenovnik trenutniCenovnik) throws IOException {
	    processBookReturnWithPenalties(bookId, trenutniCenovnik);
	}

	public static void processBookReturnByReservation(String reservationId, Cenovnik trenutniCenovnik) throws IOException {
	    String fajlIzdavanje = "podaci/izdavanje.csv";
	    
	    try (BufferedReader br = new BufferedReader(new InputStreamReader(
	            new FileInputStream(fajlIzdavanje), StandardCharsets.UTF_8))) {
	        String line;
	        while ((line = br.readLine()) != null) {
	            String[] delovi = line.split(";");
	            if (delovi.length >= 9 && delovi[8].trim().equals(reservationId) && 
	                "false".equalsIgnoreCase(delovi[7].trim())) {
	                
	                String bookId = delovi[0].trim();
	                String username = delovi[2].trim();
	                processBookReturnWithPenalties(bookId, trenutniCenovnik);
	                return;
	            }
	        }
	    } catch (IOException e) {
	        throw new IOException("Error processing return for reservation " + reservationId, e);
	    }
	}
}

