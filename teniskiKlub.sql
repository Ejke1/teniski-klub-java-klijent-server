`administrator`/*
  Nova SQL skripta za dijagram (teniski klub)
  - Grupa ima: BrojClanova, MaxKapacitet
  - Svaka grupa MORA da ima najmanje 2 clana (u podacima je 2–5 po grupi)
  - Ubaceno 20 clanova
*/

SET NAMES utf8mb4;
SET SQL_MODE='';
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0;

DROP DATABASE IF EXISTS `teniskiklub`;
CREATE DATABASE `teniskiklub` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `teniskiklub`;

/* =========================
   Administrator
   ========================= */
DROP TABLE IF EXISTS `ClanGrupe`;
DROP TABLE IF EXISTS `Grupa`;
DROP TABLE IF EXISTS `Clan`;
DROP TABLE IF EXISTS `Trener`;
DROP TABLE IF EXISTS `Kategorija`;
DROP TABLE IF EXISTS `Administrator`;

CREATE TABLE `Administrator` (
  `AdministratorID` BIGINT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `Ime` VARCHAR(30) NOT NULL,
  `Prezime` VARCHAR(30) NOT NULL,
  `Username` VARCHAR(30) NOT NULL,
  `Password` VARCHAR(60) NOT NULL,
  PRIMARY KEY (`AdministratorID`),
  UNIQUE KEY `uq_admin_username` (`Username`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

INSERT INTO `Administrator` (`AdministratorID`,`Ime`,`Prezime`,`Username`,`Password`) VALUES
(1,'Aleksandar','Eic','coa','coa123'),
(2,'Stefan','Markovic','stefan','stefan123'),
(3,'Nikola','Petrovic','nikola','nikola123');


/* =========================
   Kategorija
   ========================= */
CREATE TABLE `Kategorija` (
  `KategorijaID` BIGINT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `Naziv` VARCHAR(30) NOT NULL,
  `Opis` VARCHAR(200) NOT NULL,
  PRIMARY KEY (`KategorijaID`),
  UNIQUE KEY `uq_kategorija_naziv` (`Naziv`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

INSERT INTO `Kategorija` (`KategorijaID`,`Naziv`,`Opis`) VALUES
(1,'Pocetni','Osnovna tehnika i koordinacija; preporuceno za pocetnike.'),
(2,'Srednji','Utvrdjivanje udaraca, kretanje i osnovna taktika.'),
(3,'Napredni','Rad na taktici, servisu i igri poen na visem nivou.'),
(4,'Rekreativci','Grupe za odrasle, kondicija i mecevi.');


/* =========================
   Trener
   ========================= */
CREATE TABLE `Trener` (
  `TrenerID` BIGINT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `Ime` VARCHAR(30) NOT NULL,
  `Prezime` VARCHAR(30) NOT NULL,
  `BrojTelefona` VARCHAR(30) NOT NULL,
  `GodineIskustva` INT(3) NOT NULL,
  PRIMARY KEY (`TrenerID`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

INSERT INTO `Trener` (`TrenerID`,`Ime`,`Prezime`,`BrojTelefona`,`GodineIskustva`) VALUES
(1,'Ivan','Ristic','060101010',8),
(2,'Tamara','Vukovic','060202020',11),
(3,'Ognjen','Pavlovic','060303030',6);


/* =========================
   Clan (20 clanova)
   ========================= */
CREATE TABLE `Clan` (
  `ClanID` BIGINT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `Ime` VARCHAR(30) NOT NULL,
  `Prezime` VARCHAR(30) NOT NULL,
  `Godine` INT(3) NOT NULL,
  `Email` VARCHAR(80) NOT NULL,
  `Telefon` VARCHAR(30) NOT NULL,
  `KategorijaID` BIGINT(10) UNSIGNED NOT NULL,
  PRIMARY KEY (`ClanID`),
  UNIQUE KEY `uq_clan_email` (`Email`),
  CONSTRAINT `fk_clan_kategorija_id`
    FOREIGN KEY (`KategorijaID`) REFERENCES `Kategorija` (`KategorijaID`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

INSERT INTO `Clan` (`ClanID`,`Ime`,`Prezime`,`Godine`,`Email`,`Telefon`,`KategorijaID`) VALUES
(1,'Lana','Stojanovic',9,'lana.stojanovic@example.com','064111222',1),
(2,'Uros','Milosevic',10,'uros.milosevic@example.com','064333444',1),
(3,'Teodora','Jankovic',8,'teodora.jankovic@example.com','064555111',1),
(4,'Petar','Vasic',7,'petar.vasic@example.com','064555222',1),
(5,'Tara','Vukasinovic',9,'tara.vukasinovic@example.com','064777111',1),
(6,'Dusan','Stankovic',10,'dusan.stankovic@example.com','064777222',1),

(7,'Mina','Popovic',12,'mina.popovic@example.com','065555666',2),
(8,'Andrej','Nikolic',13,'andrej.nikolic@example.com','065777888',2),
(9,'Nina','Lazic',11,'nina.lazic@example.com','065111222',2),
(10,'Pavle','Mitic',12,'pavle.mitic@example.com','065111333',2),
(11,'Katarina','Petric',13,'katarina.petric@example.com','065777111',2),

(12,'Sara','Kovacevic',15,'sara.kovacevic@example.com','062123456',3),
(13,'Viktor','Ilic',16,'viktor.ilic@example.com','062654321',3),
(14,'Isidora','Radovic',14,'isidora.radovic@example.com','062111222',3),
(15,'Luka','Cvetkovic',15,'luka.cvetkovic@example.com','062111333',3),
(16,'Ognjen','Maric',16,'ognjen.maric@example.com','062777111',3),

(17,'Jelena','Simic',28,'jelena.simic@example.com','063222333',4),
(18,'Marko','Djordjevic',34,'marko.djordjevic@example.com','063444555',4),
(19,'Milica','Kostic',19,'milica.kostic@example.com','063111222',4),
(20,'Nenad','Zivkovic',27,'nenad.zivkovic@example.com','063111333',4);


/* =========================
   Grupa (BrojClanova + MaxKapacitet)
   Napomena: CHECK postoji, ali u MariaDB moze zavisiti od verzije;
   podatke smo svakako uneli da ispunjavaju uslov (min 2 clana).
   ========================= */
CREATE TABLE `Grupa` (
  `GrupaID` BIGINT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `Naziv` VARCHAR(50) NOT NULL,
  `Opis` VARCHAR(200) NOT NULL,
  `BrojClanova` INT(4) NOT NULL,
  `MaxKapacitet` INT(4) NOT NULL,
  `KategorijaID` BIGINT(10) UNSIGNED NOT NULL,
  `TrenerID` BIGINT(10) UNSIGNED NOT NULL,
  `AdministratorID` BIGINT(10) UNSIGNED NOT NULL,
  PRIMARY KEY (`GrupaID`),
  KEY `idx_grupa_kategorija` (`KategorijaID`),
  KEY `idx_grupa_trener` (`TrenerID`),
  KEY `idx_grupa_admin` (`AdministratorID`),
  CONSTRAINT `fk_grupa_kategorija_id`
    FOREIGN KEY (`KategorijaID`) REFERENCES `Kategorija` (`KategorijaID`),
  CONSTRAINT `fk_grupa_trener_id`
    FOREIGN KEY (`TrenerID`) REFERENCES `Trener` (`TrenerID`),
  CONSTRAINT `fk_grupa_admin_id`
    FOREIGN KEY (`AdministratorID`) REFERENCES `Administrator` (`AdministratorID`),
  CONSTRAINT `chk_grupa_kapaciteti`
    CHECK (`MaxKapacitet` > 0 AND `BrojClanova` >= 2 AND `BrojClanova` <= `MaxKapacitet`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

-- 6 grupa, svuda 2–5 clanova
INSERT INTO `Grupa`
(`GrupaID`,`Naziv`,`Opis`,`BrojClanova`,`MaxKapacitet`,`KategorijaID`,`TrenerID`,`AdministratorID`) VALUES
(1,'Mini Tenis A','Pocetnici (deca) – osnove reketa i kretanje.',5,10,1,1,1),
(2,'Mini Tenis B','Pocetnici – koordinacija i udarci iz mesta.',4,10,1,2,2),
(3,'Srednji Nivo A','Srednji – ritam igre, poeni i kretanje.',3,12,2,2,1),
(4,'Srednji Nivo B','Srednji – stabilnost udaraca i taktika.',3,12,2,3,3),
(5,'Napredni Sparing','Napredni – sparing i napredna taktika.',2,10,3,3,3),
(6,'Rekreativci Vece','Odrasli – kondicija, tehnika i mecevi.',3,16,4,1,2);


/* =========================
   ClanGrupe (veza Clan <-> Grupa)
   PK: (GrupaID, Rb)
   ========================= */
CREATE TABLE `ClanGrupe` (
  `GrupaID` BIGINT(10) UNSIGNED NOT NULL,
  `Rb` INT(7) NOT NULL,
  `Napomena` VARCHAR(200) NOT NULL,
  `ClanID` BIGINT(10) UNSIGNED NOT NULL,
  PRIMARY KEY (`GrupaID`,`Rb`),
  KEY `idx_clangrupe_clan` (`ClanID`),
  CONSTRAINT `fk_clangrupe_grupa_id`
    FOREIGN KEY (`GrupaID`) REFERENCES `Grupa` (`GrupaID`) ON DELETE CASCADE,
  CONSTRAINT `fk_clangrupe_clan_id`
    FOREIGN KEY (`ClanID`) REFERENCES `Clan` (`ClanID`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4;

-- Grupa 1 (5 clanova)
INSERT INTO `ClanGrupe` (`GrupaID`,`Rb`,`Napomena`,`ClanID`) VALUES
(1,1,'/',1),
(1,2,'Ne moze petkom.',2),
(1,3,'Preferira jutarnje termine.',3),
(1,4,'Potrebno vise rada na forhendu.',4),
(1,5,'/',5);

-- Grupa 2 (4 clana)
INSERT INTO `ClanGrupe` (`GrupaID`,`Rb`,`Napomena`,`ClanID`) VALUES
(2,1,'/',6),
(2,2,'Ne moze sredom.',1),
(2,3,'Dolazi ponedeljkom i cetvrtkom.',2),
(2,4,'/',3);

-- Grupa 3 (3 clana)
INSERT INTO `ClanGrupe` (`GrupaID`,`Rb`,`Napomena`,`ClanID`) VALUES
(3,1,'Rad na kretanju.',7),
(3,2,'/',8),
(3,3,'Backhand stabilnost.',9);

-- Grupa 4 (3 clana)
INSERT INTO `ClanGrupe` (`GrupaID`,`Rb`,`Napomena`,`ClanID`) VALUES
(4,1,'/',10),
(4,2,'Ubrzati noge u defanzivi.',11),
(4,3,'Rad na servisu.',7);

-- Grupa 5 (2 clana)
INSERT INTO `ClanGrupe` (`GrupaID`,`Rb`,`Napomena`,`ClanID`) VALUES
(5,1,'Sparing vikendom po dogovoru.',12),
(5,2,'Radi na servisu (varijacije).',13);

-- Grupa 6 (3 clana)
INSERT INTO `ClanGrupe` (`GrupaID`,`Rb`,`Napomena`,`ClanID`) VALUES
(6,1,'Samo vecernji termini.',17),
(6,2,'Povremeno odsutan zbog posla.',18),
(6,3,'/',19);


/* =========================
   (Opcionalno) Provera da se BrojClanova slaze sa ClanGrupe
   ========================= */
-- SELECT g.GrupaID, g.Naziv, g.BrojClanova, COUNT(cg.ClanID) AS StvarniBroj
-- FROM Grupa g
-- LEFT JOIN ClanGrupe cg ON cg.GrupaID = g.GrupaID
-- GROUP BY g.GrupaID, g.Naziv, g.BrojClanova;


/* Vracanje starih podesavanja */
SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_NOTES=@OLD_SQL_NOTES;