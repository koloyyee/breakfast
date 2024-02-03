package co.loyyee.scrape;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Finviz {
    private ArrayList<TickerNews> tickerNewsList;
    private String ticker;
    public Finviz() {
        tickerNewsList = new ArrayList<>();
        ticker = "AAPL";
    }
    /**
     * Turning e.g: String Feb-01-24 and 15:33PM into LocalDateTime
     * @param date The string format date e.g.: Feb-01-24
     * @param time The string format time e.g.: 03:33PM
     * @return a format to LocalDateTime as issuedDate
     * */
    private LocalDateTime formatDateTime(String date, String time ) {
        LocalDate lDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("MMM-dd-yy"));
        LocalTime lTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("hh:mma"));
        return lDate.atTime(lTime);
    }
    public void scrape(String ticker) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://finviz.com/quote.ashx?t=");
        this.ticker = ticker;
        String ending = "&p=d";
        sb.append(ticker);
        sb.append(ending);
        String url = sb.toString();

        try {
            Document document = Jsoup.connect(url).get();
            Elements newsTable = document.select("table#news-table");
            Elements trs = newsTable.select("tr");

            /** Obstacles:
             * The news date come is a string "[date] [time]"
             * but if the news is on the same date it will only show the time.
             * e.g.:
             * full - "Dec-01-23 05:53PM"
             * same date different time: "06:00AM"
             * **/
            String date = ""; // hold this in memory but outside of loop ready to be mutated.
            LocalDateTime issuedDateTime = null;
            for (Element tr : trs) {
                Element a = tr.select("a").first();
                String href = a.attr("href");
                String title = a.html();

                Elements td = tr.select("td[align='right']");
                for (Element dates : td) {
                    String[] datetime = dates.text().split(" ");
                    /** Handle full date */
                    if (datetime.length > 1) {
                        date = datetime[0];
                        if (date.contains("Today")) {
                            date = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM-dd-yy"));
                        }
                        issuedDateTime = formatDateTime(date, datetime[1]);
                    } else {
                        /** Handle time only */
                        issuedDateTime = formatDateTime(date, datetime[0]);
                    }
                }
                TickerNews tickerNews = new TickerNews(ticker, title, href, issuedDateTime);
                tickerNewsList.add(tickerNews);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ArrayList<TickerNews> getTickerNews() {
        return tickerNewsList;
    }

    public static void main(String[] args) {
        Finviz a = new Finviz();
        a.scrape("AAPL");
    }
}