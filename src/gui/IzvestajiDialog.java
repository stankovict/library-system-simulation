package gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class IzvestajiDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();


	public IzvestajiDialog() {
	    setBounds(100, 100, 450, 300);
	    getContentPane().setLayout(new BorderLayout());
	    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
	    getContentPane().add(contentPanel, BorderLayout.CENTER);
	    	    contentPanel.setLayout(null);
	    	    JButton btnPrihodiRashodi = new JButton("Prihodi i rashodi");
	    	    btnPrihodiRashodi.addActionListener(e -> {
	    	        PrihodiRashodiDialog dialog = new PrihodiRashodiDialog();
	    	        dialog.setModal(true);
	    	        dialog.setVisible(true);
	    	    });
	    	    btnPrihodiRashodi.setBounds(45, 47, 152, 29);
	    	    contentPanel.add(btnPrihodiRashodi);

	    	    getRootPane().setDefaultButton(btnPrihodiRashodi);
	    	    	    
	    	    	    JButton btnIzdateKnjige = new JButton("Izdate knjige po bibliotekaru");
	    	    	    btnIzdateKnjige.addActionListener(e -> {
	    	    	    	IzdateZaDatumDialog d = new IzdateZaDatumDialog(this);
	    	    	    	d.setLocationRelativeTo(this);
	    	    	        d.setVisible(true);
	    	    	    }
	    	    	    		);
	    	    	    btnIzdateKnjige.setBounds(45, 87, 152, 29);
	    	    	    contentPanel.add(btnIzdateKnjige);
	    	    	    
	    	    	    JButton btnObradjeneRez = new JButton("Obrađene rezervacije");
	    	    	    btnObradjeneRez.setBounds(45, 127, 152, 29);
	    	    	    contentPanel.add(btnObradjeneRez);
	    	    	    
	    	    	    btnObradjeneRez.addActionListener(e -> {
	    	    	        ObradjeneRezDialog d = new ObradjeneRezDialog(this);
	    	    	        d.setLocationRelativeTo(this);
	    	    	        d.setVisible(true);
	    	    	    });

	    	    	    
	    	    	    
	    	    	    JButton btnKnjigaDetalj = new JButton("Detalji o knjigama");
	    	    	    btnKnjigaDetalj.setBounds(45, 167, 152, 29);
	    	    	    contentPanel.add(btnKnjigaDetalj);
	    	    	    
	    	    	    btnKnjigaDetalj.addActionListener(e-> {
	    	    	    	KnjigeDetaljiDialog dialog = new KnjigeDetaljiDialog(this);
	    	    	    	dialog.setLocationRelativeTo(this);
	    	    	        dialog.setVisible(true);
	    	    	    });
	    	    	    
	    	    	    JButton btnPrihodKategorija = new JButton("Chart prihodi");
	    	    	    btnPrihodKategorija.setBounds(233, 47, 152, 29);
	    	    	    contentPanel.add(btnPrihodKategorija);
	    	    	    
	    	    	    btnPrihodKategorija.addActionListener(e -> {
	    	    	    	PrihodiKategorijeDialog dialog = new PrihodiKategorijeDialog(this);
	    	    	    	dialog.setLocationRelativeTo(this);
	    	    	        dialog.setVisible(true);
	    	    	    	
	    	    	    });
	    	    	    
	    	    	    JButton btnOpterecenje = new JButton("Opterećenje bibliotekara");
	    	    	    btnOpterecenje.setBounds(233, 87, 152, 29);
	    	    	    contentPanel.add(btnOpterecenje);
	    	    	    
	    	    	    btnOpterecenje.addActionListener(e -> {
	    	    	        OpterecenjeBibliotekaraDialog dialog = new OpterecenjeBibliotekaraDialog(this);
	    	    	        dialog.setLocationRelativeTo(this);
	    	    	        dialog.setVisible(true);
	    	    	    });

	    	    	    
	    	    	    JButton btnStatusi = new JButton("Statusi rezervacija");
	    	    	    btnStatusi.addActionListener(e ->{
	    	    	    	RezStatusiDialog dialog = new RezStatusiDialog(this);
	    	    	    	dialog.setLocationRelativeTo(this);
	    	    	        dialog.setVisible(true);
	    	    	    });
	    	    	    btnStatusi.setBounds(233, 127, 152, 29);
	    	    	    contentPanel.add(btnStatusi);
	    	    	    
	    	    	    
	    	    	    
	    	    	    JLabel lblIzvestaji = new JLabel("IZVEŠTAJI");
	    	    	    lblIzvestaji.setHorizontalAlignment(SwingConstants.CENTER);
	    	    	    lblIzvestaji.setBounds(83, 24, 76, 14);
	    	    	    contentPanel.add(lblIzvestaji);
	    	    	    
	    	    	    JLabel lblChartovi = new JLabel("CHARTOVI");
	    	    	    lblChartovi.setHorizontalAlignment(SwingConstants.CENTER);
	    	    	    lblChartovi.setBounds(268, 24, 76, 14);
	    	    	    contentPanel.add(lblChartovi);

	    JPanel buttonPane = new JPanel();
	    buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
	    getContentPane().add(buttonPane, BorderLayout.SOUTH);

	    JButton btnZatvori = new JButton("Zatvori");
	    btnZatvori.addActionListener(e -> dispose());
	    buttonPane.add(btnZatvori);
	}
}
