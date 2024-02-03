package co.loyyee.scrape;

import co.loyyee.scrape.model.Category;
import co.loyyee.scrape.model.News;
import co.loyyee.scrape.model.Outlet;
import co.loyyee.scrape.model.TickerNews;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class Benzinga {
    private ArrayList<TickerNews> tickerNewsList;

    public Benzinga() {
       tickerNewsList = new ArrayList<>();
    }

    private LocalDate formatDate(String date) {
        DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy", Locale.ENGLISH);
        LocalDate localDate = LocalDate.parse(date, parseFormatter);
        return localDate;
    }

    private LocalDateTime recentPastToMins(String recentPast) {
        LocalDateTime currentDateTime = LocalDateTime.now();

        String[] split = recentPast.split( " ");
        int whileAgo = Integer.parseInt(split[0]);
        String time = split[1];
        LocalDateTime result = switch (time) {
            case "day", "days" -> currentDateTime.minusDays(whileAgo);
            case "hour", "hours" -> currentDateTime.minusHours(whileAgo);
            default -> currentDateTime.minusMinutes(whileAgo);
        };
        return result;
    }
    public void scrape() {

    }
    public void scrape(String ticker) {
        if(ticker.isBlank())  {
            System.out.println("Ticker cannot be empty");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("https://www.benzinga.com/quote/");
        sb.append(ticker);
        sb.append("/news");
        String url = sb.toString();
        System.out.println(url);
        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla")
                    .get();
            Elements headlines = document.select("a.content-headline");
            for(Element element : headlines) {
                String href = headlines.attr("href");
                String title = element.select("div.content-title").first().text();
                String[] contentFooter = element.select("div.text-gray-400.text-sm").text().trim().split("-");
                String source = contentFooter[0].trim();
                String recentPast = element.select("span.text-gray-500").text();
                LocalDateTime issuedDate = recentPastToMins(contentFooter[1].trim());

                TickerNews tickerNews = new TickerNews(ticker, title, href, Outlet.BENZINGA, source, issuedDate);
                tickerNewsList.add(tickerNews);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public ArrayList<TickerNews> getTickerNewsList(){
        return tickerNewsList;
    }

    public static void main(String[] args) {
        Benzinga b = new Benzinga();
        b.scrape("AAPL");
        System.out.println(b.getTickerNewsList());

    }

}
