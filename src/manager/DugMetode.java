package manager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DugMetode {
    private static final String FILE_DUGOVI = "podaci/dugovi.csv";

    public static double ucitajDug(String username) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(FILE_DUGOVI), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";", -1);
                if (delovi.length == 2 && delovi[0].equals(username)) {
                    return Double.parseDouble(delovi[1]);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void sacuvajDug(String username, double iznos) {
        List<String> lines = new ArrayList<>();
        boolean pronadjen = false;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(FILE_DUGOVI), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] delovi = line.split(";", -1);
                if (delovi.length == 2 && delovi[0].equals(username)) {
                    lines.add(username + ";" + iznos);
                    pronadjen = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!pronadjen) {
            lines.add(username + ";" + iznos);
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_DUGOVI), StandardCharsets.UTF_8))) {
            for (String l : lines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void otplatiDug(String username, double iznos) {
        double trenutni = ucitajDug(username);
        double novi = Math.max(trenutni - iznos, 0);
        sacuvajDug(username, novi);
    }
}
