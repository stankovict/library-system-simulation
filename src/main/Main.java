package main;
import java.awt.EventQueue;

import gui.MainFrame;

public class Main {

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

	}

}
