package co.loyyee.scrape;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class Benzinga {
    private ArrayList<TickerNews> tickerNewsList;
    private String ticker;

    public Benzinga() {
       tickerNewsList = new ArrayList<>();
       ticker = "AAPL";
    }

    private LocalDate formatDate(String date) {
        DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy", Locale.ENGLISH);
        LocalDate localDate = LocalDate.parse(date, parseFormatter);
        return localDate;
    }
    public void scrape(String ticker) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://www.benzinga.com/quote/");
        sb.append(ticker);
        sb.append("/news");
        String url = sb.toString();

        // grep the h2
        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla")
                    .get();
           String currentDate = document.select("h2").first().text();
           formatDate(currentDate);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        Benzinga b = new Benzinga();
        b.scrape("AAPL");

    }

}
