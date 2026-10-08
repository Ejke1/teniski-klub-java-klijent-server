# Teniski klub

Klijent-server Java aplikacija za vođenje teniskog kluba: evidencija članova i trenera i
pravljenje grupa za treninge. Kada se grupa napravi ili izmeni, server svim članovima grupe
šalje mejl sa PDF dokumentom o grupi.

Projekat iz predmeta **Projektovanje softvera**, FON.
Autor: Aleksandar Eić.

## Šta se gde nalazi

| Folder | Šta sadrži |
|---|---|
| `TenisZajednicki` | Zajednički deo za server i klijent: domenske klase (`Clan`, `Trener`, `Grupa`, `ClanGrupe`, `Kategorija`, `Administrator`) i klase za prenos podataka (`Request`, `Response`, `Operation`). |
| `TenisServer` | Serverski deo aplikacije. |
| `TenisKlijent` | Klijentski deo aplikacije. |
| `lib` | Biblioteke za testiranje (JUnit 4.13.2, Hamcrest 1.3). |
| `teniskiKlub.sql` | Skripta za kreiranje baze `teniskiklub` sa početnim podacima. |
| `mysql-connector-j-9.6.0.jar` | Drajver za povezivanje sa MySQL bazom. |

### TenisServer

| Paket | Šta radi |
|---|---|
| `db` | `DBBroker` - povezivanje sa bazom i SQL upiti kroz transakcije. |
| `so` | Sistemske operacije za članove, trenere, grupe, kategorije i prijavu administratora. |
| `controller` | `ServerController` - poziva sistemske operacije. |
| `thread` | Mrežna komunikacija: nit servera i posebna nit za svakog povezanog klijenta. |
| `forms` | Serverska forma (pokretanje i gašenje servera) i forma za konfiguraciju baze. |
| `pdf` | Pravljenje PDF dokumenta sa podacima o grupi i spiskom članova. |
| `mail` | Slanje mejla sa PDF-om svim članovima grupe. |

### TenisKlijent

| Paket | Šta radi |
|---|---|
| `session` | Veza sa serverom i podaci o prijavljenom administratoru. |
| `controller` | `ClientController` - šalje zahteve serveru i vraća odgovore. |
| `models` | Modeli tabela, sa automatskim osvežavanjem podataka sa servera. |
| `forme` | Forma za prijavu i glavna forma za kreiranje grupe. |
| `form.clan` | Unos, pretraga i detalji člana, i izbor člana preko lupe. |
| `form.trener` | Unos, pretraga i detalji trenera. |
| `form.grupa` | Pretraga grupa i detalji grupe. |

## Testiranje

Oba projekta imaju JUnit testove u delu `Test Packages` (214 na serveru, 184 na klijentu).
Kada se projekat u NetBeans-u pokrene (Run), prvo se izvrše svi testovi, a program se
pokreće samo ako su svi prošli. Serverski testovi rade nad posebnom bazom `teniskiklub_test`
i lažnim mejl serverom, a klijentski nad lažnim serverom, pa testiranje ne menja prave
podatke i ne šalje prave mejlove.

## Pokretanje

1. U XAMPP-u pokrenuti MySQL i izvršiti skriptu `teniskiKlub.sql`.
2. Otvoriti sva tri projekta u NetBeans-u.
3. Pokrenuti `TenisServer`, čekirati provere u serverskoj formi i kliknuti „Pokreni server“.
4. Pokrenuti `TenisKlijent` i prijaviti se.

Podešavanja za slanje mejla čuvaju se u `TenisServer/mailconfig.properties`. Taj fajl nije
na GitHub-u jer sadrži lozinku; ako ne postoji, program sam napravi prazan šablon.
