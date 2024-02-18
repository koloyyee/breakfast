package co.loyyee.scrape;

import co.loyyee.scrape.model.Outlet;
import co.loyyee.scrape.model.Scraper;
import co.loyyee.scrape.model.TickerNews;
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
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Finviz implements Runnable, Scraper<TickerNews> {
    private List<TickerNews> tickerNewsList;
    private String ticker;
    private final String BASE_URL = "https://finviz.com/quote.ashx?t=";
    private final String END = "&p=d";
    private final String url;
    private Thread thread;
    private boolean isToday;
    public Finviz(String ticker, boolean isToday) {
        this(ticker);
        this.isToday = isToday;
    }
    public Finviz(String ticker) {
        tickerNewsList = new ArrayList<TickerNews>();
        this.ticker = ticker;
        this.url = BASE_URL + ticker + END;
        thread = new Thread(this);
        thread.start();
        thread = Thread.ofVirtual().start(this);
    }
    /**
     * Turning e.g: String Feb-01-24 and 15:33PM into LocalDateTime
     * @param date The string format date e.g.: Feb-01-24
     * @param time The string format time e.g.: 03:33PM
     * @return a format to LocalDateTime as issuedDate
     * */
    private LocalDateTime formatDateTime(String date, String time ) {
            LocalDate lDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("MMM-dd-yy", Locale.ENGLISH));
            LocalTime lTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("hh:mma",  Locale.ENGLISH));
            return lDate.atTime(lTime);
    }
    @Override
    public java.util.List<TickerNews> scrape() {

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
                TickerNews tickerNews = new TickerNews(ticker, title, href, Outlet.FINVIZ, "none", issuedDateTime);
                tickerNewsList.add(tickerNews);

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return getTickerNewsList();
    }

    public String getTicker() {
        return ticker;
    }

    public Thread getThread() {
        return thread;
    }

    public java.util.List<TickerNews> getTickerNewsList() {
        return tickerNewsList;
    }

    @Override
    public List<TickerNews> scrapeToday() {

			Document document = null;
			try {
				document = Jsoup.connect(url).get();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			Elements newsTable = document.select("table#news-table");
      Elements trs = newsTable.select("tr");
        for (Element tr : trs) {
            Element a = tr.select("a").first();
            String href = a.attr("href");
            String title = a.html();
            Elements td = tr.select("td[align='right']");

            String date = ""; // hold this in memory but outside of loop ready to be mutated.
            LocalDateTime issuedDateTime = null;
            for (Element dates : td) {
                String[] datetime = dates.text().split(" ");
                /** Handle full date */
                if (datetime.length > 1) {
                    date = datetime[0];
                    if (date.contains("Today")) {
                        date = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM-dd-yy"));
                        issuedDateTime = formatDateTime(date, datetime[1]);
                        TickerNews tickerNews = new TickerNews(ticker, title, href, Outlet.FINVIZ, "none", issuedDateTime);
                        tickerNewsList.add(tickerNews);
                    }
                }

            }
        }
        System.out.println(getTickerNewsList());
        return getTickerNewsList();
    }

    @Override
    public void run() {
        if(isToday) {
            scrapeToday();
        } else {
            scrape();
        }

    }

//    public static void main(String[] args) {
//        Finviz a = new Finviz("GOOG");
//        a.scrape();
//        a.scrapeToday();
//    }
}