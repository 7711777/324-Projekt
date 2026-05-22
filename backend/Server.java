import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Server {

    private static final double STANDARD_STUNDEN_PRO_WOCHE = 8 * 5;
    private static final double WOCHEN_PRO_JAHR = 52;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(3000), 0);
        server.createContext("/berechnen", Server::berechnen);
        server.start();
        System.out.println("Server laeuft auf http://localhost:3000");
    }

    private static void berechnen(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseForm(body);

        Double stundenlohn  = parsePositive(params.get("stundenlohn"));
        Double stundenWoche = parsePositive(params.get("stunden_woche"));
        if (stundenWoche == null) stundenWoche = STANDARD_STUNDEN_PRO_WOCHE;

        if (stundenlohn == null) {
            sendHtml(exchange, 400, fehlerHtml(
                    "Bitte einen gueltigen, positiven Stundenlohn eingeben."));
            return;
        }

        double woche = stundenlohn * stundenWoche;
        double monat = woche * WOCHEN_PRO_JAHR / 12;
        double jahr  = woche * WOCHEN_PRO_JAHR;

        sendHtml(exchange, 200, resultatHtml(stundenlohn, stundenWoche, woche, monat, jahr));
    }

    private static Map<String, String> parseForm(String body) {
        Map<String, String> map = new HashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isEmpty()) continue;
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String v = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                map.put(k, v);
            }
        }
        return map;
    }

    private static Double parsePositive(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            double v = Double.parseDouble(raw.trim());
            return v > 0 ? v : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String resultatHtml(double stundenlohn, double stundenWoche,
                                       double woche, double monat, double jahr) {
        return """
                <!DOCTYPE html>
                <html lang="de">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Gehaltsrechner</title>
                <style>
                  body { font-family: system-ui, sans-serif; display: grid; place-items: center;
                         min-height: 100vh; margin: 0; padding: 1rem; background: #fafafa; }
                  table { border-collapse: collapse; font-size: 1.2rem; width: 100%%; max-width: 28rem; }
                  td { padding: .4rem 1rem; }
                  td.val { text-align: right; font-variant-numeric: tabular-nums; }
                  a { display: block; margin-top: 1.5rem; text-align: center; color: #555; }
                  @media (max-width: 480px) {
                    table { font-size: 1rem; }
                    td { padding: .3rem .5rem; }
                  }
                </style>
                </head>
                <body>
                  <div>
                    <table>
                      <tr><td>Stundenlohn</td>     <td class="val">CHF %s</td></tr>
                      <tr><td>Stunden / Woche</td> <td class="val">%s h</td></tr>
                      <tr><td>Woche</td>           <td class="val">CHF %s</td></tr>
                      <tr><td>Monat</td>           <td class="val">CHF %s</td></tr>
                      <tr><td>Jahr</td>            <td class="val">CHF %s</td></tr>
                    </table>
                    <a href="javascript:history.back()">zurueck</a>
                  </div>
                </body>
                </html>
                """.formatted(fmt(stundenlohn), fmt(stundenWoche),
                              fmt(woche), fmt(monat), fmt(jahr));
    }

    private static String fehlerHtml(String meldung) {
        return """
                <!DOCTYPE html>
                <html lang="de">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Fehler</title>
                <style>
                  body { font-family: system-ui, sans-serif; display: grid; place-items: center;
                         min-height: 100vh; margin: 0; padding: 1rem; background: #fafafa; }
                  .box { max-width: 24rem; text-align: center; }
                  .meldung { color: #b00; font-size: 1.1rem; }
                  a { display: block; margin-top: 1.5rem; color: #555; }
                </style>
                </head>
                <body>
                  <div class="box">
                    <p class="meldung">%s</p>
                    <a href="javascript:history.back()">zurueck</a>
                  </div>
                </body>
                </html>
                """.formatted(meldung);
    }

    private static void sendHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] out = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(status, out.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(out);
        }
    }

    private static String fmt(double v) {
        return String.format(Locale.US, "%,.2f", v);
    }
}
