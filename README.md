# 324-Projekt — Gehaltsrechner

Ein minimalistischer Gehaltsrechner: Stundenlohn und Wochenarbeitszeit eingeben, Wochen-, Monats- und Jahresgehalt erhalten.

## Struktur

- `frontend/` — HTML-Formular (`index.html`)
- `backend/`
  - `Server.java` — HTTP-Server (nur JDK, keine externen Libraries)
- `PLANUNG.md` — User-Stories, Tasks, Label- und Milestone-Schema

## Berechnung

Bei einer Wochenarbeitszeit `h` (Stunden / Woche, Default 40):

- Woche = Stundenlohn × h
- Monat = Stundenlohn × h × 52 / 12
- Jahr  = Stundenlohn × h × 52

Default 40 = 8 h/Tag × 5 Tage. Teilzeit-Pensen einfach durch tieferes `h` abdecken (z.B. 20 für 50 %).

## Starten

Im Ordner `backend/`:

```bash
java Server.java
```

Anschliessend `frontend/index.html` im Browser öffnen, Stundenlohn und Stunden/Woche eingeben und absenden. Das Resultat liefert das Backend als HTML zurück.

**Voraussetzung:** Java 15 oder neuer (Text-Blocks, Single-File-Source).

## Validierung

- Stundenlohn muss positiv und numerisch sein (Frontend `min="0.5"`, Backend prüft erneut).
- Stunden / Woche ist optional; ohne Angabe wird 40 verwendet.
- Ungültige Eingaben werden vom Backend mit HTTP 400 und einer kleinen Fehlerseite abgewiesen.
