# 324-Projekt — Gehaltsrechner

Ein minimalistischer Gehaltsrechner: Stundenlohn eingeben, Wochen-, Monats- und Jahresgehalt erhalten.

## Struktur

- `frontend/` — einfaches HTML-Formular (`index.html`)
- `backend/` — Java HTTP-Server (`Server.java`, nutzt nur das JDK, keine externen Libraries)

## Berechnung

Annahmen:
- 8 Stunden pro Tag
- 5 Tage pro Woche → 40 Stunden / Woche
- 52 Wochen pro Jahr

Daraus:
- Woche = Stundenlohn × 40
- Monat = Stundenlohn × 40 × 52 / 12 ≈ Stundenlohn × 173.33
- Jahr  = Stundenlohn × 40 × 52 = Stundenlohn × 2080

## Starten

Im Ordner `backend/`:

```bash
java Server.java
```

Anschliessend `frontend/index.html` im Browser öffnen, Stundenlohn eintippen und absenden. Das Resultat liefert das Backend als HTML zurück.

**Voraussetzung:** Java 11 oder neuer.
