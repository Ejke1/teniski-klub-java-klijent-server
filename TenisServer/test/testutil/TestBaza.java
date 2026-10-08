package testutil;

import domain.Administrator;
import domain.Clan;
import domain.ClanGrupe;
import domain.Grupa;
import domain.Kategorija;
import domain.Trener;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public final class TestBaza {

    public static final String PODRAZUMEVANA_KONFIGURACIJA = "dbconfig-test.properties";

    private static final Object[][] ADMINISTRATORI = {
        {1L, "Aleksandar", "Eic", "coa", "coa123"},
        {2L, "Stefan", "Markovic", "stefan", "stefan123"},
        {3L, "Nikola", "Petrovic", "nikola", "nikola123"},
        {4L, "Marko", "Jovanovic", "marko", "marko123"}
    };

    private static final Object[][] KATEGORIJE = {
        {1L, "Pocetni", "Osnovna tehnika i koordinacija."},
        {2L, "Srednji", "Utvrdjivanje udaraca i osnovna taktika."},
        {3L, "Napredni", "Taktika, servis i igra poena."},
        {4L, "Rekreativci", "Grupe za odrasle."}
    };

    private static final Object[][] TRENERI = {
        {1L, "Ivan", "Ristic", "0601010101", 8},
        {2L, "Tamara", "Vukovic", "0602020202", 11}
    };

    private static final Object[][] CLANOVI = {
        {1L, "Lana", "Stojanovic", 9, "lana.stojanovic@test.rs", "0641112222", 1L},
        {2L, "Uros", "Milosevic", 10, "uros.milosevic@test.rs", "0643334444", 1L},
        {3L, "Petar", "Vasic", 7, "petar.vasic@test.rs", "0645552222", 1L},
        {4L, "Mina", "Popovic", 12, "mina.popovic@test.rs", "0655556666", 2L},
        {5L, "Andrej", "Nikolic", 13, "andrej.nikolic@test.rs", "0657778888", 2L},
        {6L, "Sara", "Kovacevic", 15, "sara.kovacevic@test.rs", "0621234567", 3L}
    };

    private static final Object[][] GRUPE = {
        {1L, "Mini Tenis A", "Pocetnici - osnove reketa i kretanje.", 3, 10, 1L, 1L, 1L},
        {2L, "Srednji Nivo A", "Srednji nivo - ritam igre.", 2, 12, 2L, 2L, 2L}
    };

    private static final Object[][] CLANOVI_GRUPA = {
        {1L, 1, "/", 1L},
        {1L, 2, "Ne moze petkom.", 2L},
        {1L, 3, "/", 3L},
        {2L, 1, "/", 4L},
        {2L, 2, "Rad na servisu.", 5L}
    };

    private static final String[] TABELE = {
        "CREATE TABLE IF NOT EXISTS `Administrator` ("
        + " `AdministratorID` BIGINT(10) UNSIGNED NOT NULL AUTO_INCREMENT,"
        + " `Ime` VARCHAR(30) NOT NULL,"
        + " `Prezime` VARCHAR(30) NOT NULL,"
        + " `Username` VARCHAR(30) NOT NULL,"
        + " `Password` VARCHAR(60) NOT NULL,"
        + " PRIMARY KEY (`AdministratorID`),"
        + " UNIQUE KEY `uq_admin_username` (`Username`)"
        + ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4",

        "CREATE TABLE IF NOT EXISTS `Kategorija` ("
        + " `KategorijaID` BIGINT(10) UNSIGNED NOT NULL AUTO_INCREMENT,"
        + " `Naziv` VARCHAR(30) NOT NULL,"
        + " `Opis` VARCHAR(200) NOT NULL,"
        + " PRIMARY KEY (`KategorijaID`),"
        + " UNIQUE KEY `uq_kategorija_naziv` (`Naziv`)"
        + ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4",

        "CREATE TABLE IF NOT EXISTS `Trener` ("
        + " `TrenerID` BIGINT(10) UNSIGNED NOT NULL AUTO_INCREMENT,"
        + " `Ime` VARCHAR(30) NOT NULL,"
        + " `Prezime` VARCHAR(30) NOT NULL,"
        + " `BrojTelefona` VARCHAR(30) NOT NULL,"
        + " `GodineIskustva` INT(3) NOT NULL,"
        + " PRIMARY KEY (`TrenerID`)"
        + ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4",

        "CREATE TABLE IF NOT EXISTS `Clan` ("
        + " `ClanID` BIGINT(10) UNSIGNED NOT NULL AUTO_INCREMENT,"
        + " `Ime` VARCHAR(30) NOT NULL,"
        + " `Prezime` VARCHAR(30) NOT NULL,"
        + " `Godine` INT(3) NOT NULL,"
        + " `Email` VARCHAR(80) NOT NULL,"
        + " `Telefon` VARCHAR(30) NOT NULL,"
        + " `KategorijaID` BIGINT(10) UNSIGNED NOT NULL,"
        + " PRIMARY KEY (`ClanID`),"
        + " UNIQUE KEY `uq_clan_email` (`Email`),"
        + " CONSTRAINT `fk_clan_kategorija_id` FOREIGN KEY (`KategorijaID`) REFERENCES `Kategorija` (`KategorijaID`)"
        + ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4",

        "CREATE TABLE IF NOT EXISTS `Grupa` ("
        + " `GrupaID` BIGINT(10) UNSIGNED NOT NULL AUTO_INCREMENT,"
        + " `Naziv` VARCHAR(50) NOT NULL,"
        + " `Opis` VARCHAR(200) NOT NULL,"
        + " `BrojClanova` INT(4) NOT NULL,"
        + " `MaxKapacitet` INT(4) NOT NULL,"
        + " `KategorijaID` BIGINT(10) UNSIGNED NOT NULL,"
        + " `TrenerID` BIGINT(10) UNSIGNED NOT NULL,"
        + " `AdministratorID` BIGINT(10) UNSIGNED NOT NULL,"
        + " PRIMARY KEY (`GrupaID`),"
        + " CONSTRAINT `fk_grupa_kategorija_id` FOREIGN KEY (`KategorijaID`) REFERENCES `Kategorija` (`KategorijaID`),"
        + " CONSTRAINT `fk_grupa_trener_id` FOREIGN KEY (`TrenerID`) REFERENCES `Trener` (`TrenerID`),"
        + " CONSTRAINT `fk_grupa_admin_id` FOREIGN KEY (`AdministratorID`) REFERENCES `Administrator` (`AdministratorID`),"
        + " CONSTRAINT `chk_grupa_kapaciteti` CHECK (`MaxKapacitet` > 0 AND `BrojClanova` >= 2 AND `BrojClanova` <= `MaxKapacitet`)"
        + ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4",

        "CREATE TABLE IF NOT EXISTS `ClanGrupe` ("
        + " `GrupaID` BIGINT(10) UNSIGNED NOT NULL,"
        + " `Rb` INT(7) NOT NULL,"
        + " `Napomena` VARCHAR(200) NOT NULL,"
        + " `ClanID` BIGINT(10) UNSIGNED NOT NULL,"
        + " PRIMARY KEY (`GrupaID`,`Rb`),"
        + " CONSTRAINT `fk_clangrupe_grupa_id` FOREIGN KEY (`GrupaID`) REFERENCES `Grupa` (`GrupaID`) ON DELETE CASCADE,"
        + " CONSTRAINT `fk_clangrupe_clan_id` FOREIGN KEY (`ClanID`) REFERENCES `Clan` (`ClanID`)"
        + ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4"
    };

    private static boolean semaNapravljena;
    private static Exception greskaPriPripremi;
    private static String url;
    private static String korisnik;
    private static String lozinka;

    private TestBaza() {
    }

    public static synchronized void pripremi() throws Exception {
        if (greskaPriPripremi != null) {
            throw new IllegalStateException(greskaPriPripremi.getMessage(), greskaPriPripremi);
        }
        if (!semaNapravljena) {
            try {
                ucitajKonfiguraciju();
                napraviSemu();
            } catch (Exception ex) {
                greskaPriPripremi = ex;
                throw ex;
            }
            semaNapravljena = true;
        }
        vratiPocetnePodatke();
    }

    private static void ucitajKonfiguraciju() throws IOException {
        if (System.getProperty("dbconfig") == null) {
            System.setProperty("dbconfig", PODRAZUMEVANA_KONFIGURACIJA);
        }
        String putanja = System.getProperty("dbconfig");
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(putanja)) {
            p.load(in);
        }
        url = p.getProperty("url");
        korisnik = p.getProperty("username");
        lozinka = p.getProperty("password", "");
        String baza = nazivBaze(url);
        if (!baza.toLowerCase().endsWith("_test")) {
            throw new IllegalStateException("Testovi smeju da rade samo nad test bazom (naziv se zavrsava sa _test), "
                    + "a u fajlu " + putanja + " je navedena baza '" + baza + "'.");
        }
    }

    private static String nazivBaze(String url) {
        String bezParametara = url.contains("?") ? url.substring(0, url.indexOf('?')) : url;
        return bezParametara.substring(bezParametara.lastIndexOf('/') + 1);
    }

    private static String adresaServera(String url) {
        String parametri = url.contains("?") ? url.substring(url.indexOf('?')) : "";
        String bezParametara = url.contains("?") ? url.substring(0, url.indexOf('?')) : url;
        return bezParametara.substring(0, bezParametara.lastIndexOf('/') + 1) + parametri;
    }

    private static void napraviSemu() throws SQLException {
        String baza = nazivBaze(url);
        try (Connection c = poveziSe(adresaServera(url)); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + baza + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
        try (Connection c = poveziSe(url); Statement s = c.createStatement()) {
            for (String tabela : TABELE) {
                s.executeUpdate(tabela);
            }
        }
    }

    private static Connection poveziSe(String adresa) throws SQLException {
        try {
            return DriverManager.getConnection(adresa, korisnik, lozinka);
        } catch (SQLException ex) {
            throw new SQLException("Nije moguce povezati se sa MySQL serverom (" + adresa + "). "
                    + "Proverite da li je MySQL pokrenut u XAMPP-u. Detalji: " + ex.getMessage(), ex);
        }
    }

    private static void vratiPocetnePodatke() throws SQLException {
        try (Connection c = poveziSe(url); Statement s = c.createStatement()) {
            s.executeUpdate("DELETE FROM ClanGrupe");
            s.executeUpdate("DELETE FROM Grupa");
            s.executeUpdate("DELETE FROM Clan");
            s.executeUpdate("DELETE FROM Trener");
            s.executeUpdate("DELETE FROM Kategorija");
            s.executeUpdate("DELETE FROM Administrator");
            for (Object[] a : ADMINISTRATORI) {
                s.executeUpdate("INSERT INTO Administrator (AdministratorID, Ime, Prezime, Username, Password) VALUES ("
                        + a[0] + ", '" + a[1] + "', '" + a[2] + "', '" + a[3] + "', '" + a[4] + "')");
            }
            for (Object[] k : KATEGORIJE) {
                s.executeUpdate("INSERT INTO Kategorija (KategorijaID, Naziv, Opis) VALUES ("
                        + k[0] + ", '" + k[1] + "', '" + k[2] + "')");
            }
            for (Object[] t : TRENERI) {
                s.executeUpdate("INSERT INTO Trener (TrenerID, Ime, Prezime, BrojTelefona, GodineIskustva) VALUES ("
                        + t[0] + ", '" + t[1] + "', '" + t[2] + "', '" + t[3] + "', " + t[4] + ")");
            }
            for (Object[] cl : CLANOVI) {
                s.executeUpdate("INSERT INTO Clan (ClanID, Ime, Prezime, Godine, Email, Telefon, KategorijaID) VALUES ("
                        + cl[0] + ", '" + cl[1] + "', '" + cl[2] + "', " + cl[3] + ", '" + cl[4] + "', '"
                        + cl[5] + "', " + cl[6] + ")");
            }
            for (Object[] g : GRUPE) {
                s.executeUpdate("INSERT INTO Grupa (GrupaID, Naziv, Opis, BrojClanova, MaxKapacitet, KategorijaID, "
                        + "TrenerID, AdministratorID) VALUES (" + g[0] + ", '" + g[1] + "', '" + g[2] + "', "
                        + g[3] + ", " + g[4] + ", " + g[5] + ", " + g[6] + ", " + g[7] + ")");
            }
            for (Object[] cg : CLANOVI_GRUPA) {
                s.executeUpdate("INSERT INTO ClanGrupe (GrupaID, Rb, Napomena, ClanID) VALUES ("
                        + cg[0] + ", " + cg[1] + ", '" + cg[2] + "', " + cg[3] + ")");
            }
        }
    }

    public static int broj(String upit) throws SQLException {
        try (Connection c = poveziSe(url); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(upit)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public static String vrednost(String upit) throws SQLException {
        try (Connection c = poveziSe(url); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(upit)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    public static Administrator administrator(long id) {
        Object[] a = red(ADMINISTRATORI, id);
        return new Administrator((Long) a[0], (String) a[1], (String) a[2], (String) a[3], (String) a[4]);
    }

    public static Kategorija kategorija(long id) {
        Object[] k = red(KATEGORIJE, id);
        return new Kategorija((Long) k[0], (String) k[1], (String) k[2]);
    }

    public static Trener trener(long id) {
        Object[] t = red(TRENERI, id);
        return new Trener((Long) t[0], (String) t[1], (String) t[2], (String) t[3], (Integer) t[4]);
    }

    public static Clan clan(long id) {
        Object[] c = red(CLANOVI, id);
        return new Clan((Long) c[0], (String) c[1], (String) c[2], (Integer) c[3], (String) c[4],
                (String) c[5], kategorija((Long) c[6]));
    }

    public static Grupa grupa(long id) {
        Object[] g = red(GRUPE, id);
        Grupa grupa = new Grupa((Long) g[0], (String) g[1], (String) g[2], (Integer) g[3], (Integer) g[4],
                kategorija((Long) g[5]), trener((Long) g[6]), administrator((Long) g[7]), new ArrayList<>());
        for (Object[] cg : CLANOVI_GRUPA) {
            if (cg[0].equals(id)) {
                grupa.getClanoviGrupe().add(new ClanGrupe(grupa, (Integer) cg[1], (String) cg[2], clan((Long) cg[3])));
            }
        }
        return grupa;
    }

    public static Grupa novaGrupa(String naziv, long... clanovi) {
        Grupa g = new Grupa(null, naziv, "Grupa napravljena u testu.", clanovi.length, 10,
                kategorija(1), trener(1), administrator(1), new ArrayList<>());
        int rb = 0;
        for (long id : clanovi) {
            g.getClanoviGrupe().add(new ClanGrupe(g, ++rb, "/", clan(id)));
        }
        return g;
    }

    private static Object[] red(Object[][] tabela, long id) {
        for (Object[] r : tabela) {
            if (((Long) r[0]) == id) {
                return r;
            }
        }
        throw new IllegalArgumentException("Nema test podatka sa ID " + id);
    }
}
