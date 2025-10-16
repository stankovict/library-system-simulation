package modeli;

public class PosebnaKategorija {
		private String kategorija;
		private double popust;
		
		public PosebnaKategorija(String kategorija, double popust) {
			this.kategorija = kategorija;
			this.popust = popust;
		}
		
		
		public String getKategorija() {
			return kategorija;
		}
		
		public void setKategorija(String kategorija) {
			this.kategorija = kategorija;
		}
		
		public double getPopust() {
			return popust;
		}
		
		public void setPopust(double popust) {
			this.popust = popust;
		}
		
		public String toString() {
			return kategorija;
		}
}
