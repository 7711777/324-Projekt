import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class Server {

    private static final double STUNDEN_PRO_WOCHE = 8 * 5;
    private static final double WOCHEN_PRO_JAHR = 52;
    private static final double STUNDEN_PRO_MONAT = STUNDEN_PRO_WOCHE * WOCHEN_PRO_JAHR / 12;
    private static final double STUNDEN_PRO_JAHR = STUNDEN_PRO_WOCHE * WOCHEN_PRO_JAHR;

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
        double stundenlohn = parseStundenlohn(body);

        double woche = stundenlohn * STUNDEN_PRO_WOCHE;
        double monat = stundenlohn * STUNDEN_PRO_MONAT;
        double jahr = stundenlohn * STUNDEN_PRO_JAHR;

        String html = """
                <!DOCTYPE html>
                <html lang="de">
                <head><meta charset="UTF-8"><title>Gehaltsrechner</title>
                <style>
                  body { font-family: system-ui, sans-serif; display: grid; place-items: center;
                         min-height: 100vh; margin: 0; background: #fafafa; }
                  table { border-collapse: collapse; font-size: 1.2rem; }
                  td { padding: .4rem 1rem; }
                  td.val { text-align: right; font-variant-numeric: tabular-nums; }
                  a { display: block; margin-top: 1.5rem; text-align: center; color: #555; }
                </style></head>
                <body>
                  <div>
                    <table>
                      <tr><td>Stundenlohn</td><td class="val">CHF %s</td></tr>
                      <tr><td>Woche</td>      <td class="val">CHF %s</td></tr>
                      <tr><td>Monat</td>      <td class="val">CHF %s</td></tr>
                      <tr><td>Jahr</td>       <td class="val">CHF %s</td></tr>
                    </table>
                    <a href="javascript:history.back()">zurück</a>
                  </div>
                </body>
                </html>
                """.formatted(fmt(stundenlohn), fmt(woche), fmt(monat), fmt(jahr));

        byte[] out = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, out.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(out);
        }
    }

    private static double parseStundenlohn(String body) {
        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && "stundenlohn".equals(kv[0])) {
                String raw = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                try { return Double.parseDouble(raw); } catch (NumberFormatException ignored) { }
            }
        }
        return 0;
    }

    private static String fmt(double v) {
        return String.format(Locale.US, "%,.2f", v);
    }
}
