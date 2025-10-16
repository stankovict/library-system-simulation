package manager;

import java.io.BufferedReader;
import java.io.FileReader;

public class NajamManager {
	public static int ucitajDefaultNajam() {
	    try (BufferedReader br = new BufferedReader(new FileReader("podaci/najam.csv"))) {
	        String line = br.readLine();
	        if (line != null) {
	            return Integer.parseInt(line.trim());
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return 7;
	}
	
	public static boolean postaviDefaultNajam(int brojDana) {
	    try (java.io.PrintWriter out = new java.io.PrintWriter("podaci/najam.csv")) {
	        out.print(brojDana);
	        return true;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}


}
