package co.loyyee.scrape;

import co.loyyee.scrape.model.*;
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
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Benzinga implements Runnable, Scraper<TickerNews> {
    private List<TickerNews> tickerNewsList;
    private String ticker;
    private Thread thread;
    private String url;
    final static private String BASE_URL = "https://www.benzinga.com/quote/";
    final static private String END = "/news";

    private boolean isToday;
     final private Logger log = LoggerFactory.getLogger(Benzinga.class);
     public Benzinga(String ticker, boolean isToday) {
         this(ticker);
         this.isToday = isToday;
     }
    public Benzinga(String ticker) {
        tickerNewsList = new ArrayList<>();
        this.ticker = ticker;
        // Platform thread
//        thread = new Thread(this);
//        thread.start();
        url = BASE_URL + ticker + END;
        thread = Thread.ofVirtual().start(this);
    }

    private LocalDate formatDate(String date) {
        DateTimeFormatter parseFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy", Locale.ENGLISH);
        LocalDate localDate = LocalDate.parse(date, parseFormatter);
        return localDate;
    }
    private boolean isIntMonth(String first) {
        try{
            Integer.parseInt(first);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    private boolean isToday(String date) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        int hoursAgo = Integer.parseInt(date.split(" ")[0]);
        return currentDateTime.toLocalDate().isEqual(currentDateTime.minusHours(hoursAgo).toLocalDate());
    }
/**
 * edge cases: Aug 30, 2023, 11:58AM
 * */
    private LocalDateTime recentPastToMins(String recentPast) {
        LocalDateTime currentDateTime = LocalDateTime.now();

        String[] split = recentPast.split(" ");
        int whileAgo;
        /** TODO: unable to solve the "Aug 30, 2023, 11:58AM" error yet */
//        if( !isIntMonth(split[0])) {
//            String test = "May 6, 2023, 5:10PM";
//            log.info(test);
//            // handle month. e.g.
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, uuuu, hh:mma", Locale.ENGLISH);
//                LocalDateTime localDateTime = LocalDateTime.parse(test, formatter);
//                log.info(localDateTime.toString());
//                return localDateTime;
//        }

        if( isIntMonth(split[0])) {
            {
                whileAgo = Integer.parseInt(split[0]);
                String time = split[1];
                LocalDateTime result = switch (time) {
                    case "day", "days" -> currentDateTime.minusDays(whileAgo);
                    case "hour", "hours" -> currentDateTime.minusHours(whileAgo);
                    default -> currentDateTime.minusMinutes(whileAgo);
                };
                return result;
            }
        } else {
        return null;
        }
    }

//    public List<TickerNews> scrape() {
//        try {
//            Document document = Jsoup.connect(url)
//                    .userAgent("Mozilla")
//                    .get();
//            Elements headlines = document.select("a.content-headline");
//
//            for (Element element : headlines) {
//                String href = element.attr("href");
//                String title = element.select("div.content-title").first().text();
//                String[] contentFooter = element.select("div.text-gray-400.text-sm").text().trim().split("-");
//                String source = contentFooter[0].trim();
//                if(contentFooter[1].contains("Sponsor")) {
//                    break;
//                }
//                LocalDateTime issuedDate = recentPastToMins(contentFooter[1].trim());
//
//                TickerNews tickerNews = new TickerNews(ticker, title, href, Outlet.BENZINGA, source, issuedDate);
//                tickerNewsList.add(tickerNews);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return getTickerNewsList();
//    }


    /**
     * Reference to parallelStream and multi-threaded scraping
     * source: https://stackoverflow.com/questions/63022139/web-scraping-using-multithreading/63022373#63022373
     * */
    @Override
    public List<TickerNews> scrape() {

        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla")
                    .get();
            Elements headlines = document.select("a.content-headline");

            tickerNewsList = headlines.parallelStream()
                .filter(element -> {
                    String[] contentFooter = element.select("div.text-gray-400.text-sm").text().trim().split("-");
                    return !contentFooter[1].contains("Sponsor");
                })
                    .map(element -> {
                        String href = element.attr("href");
                        String title = element.select("div.content-title").first().text();
                        String[] contentFooter = element.select("div.text-gray-400.text-sm").text().trim().split("-");
                        String source = contentFooter[0].trim();
                        LocalDateTime issuedDate = recentPastToMins(contentFooter[1].trim());
                        TickerNews tickerNews = new TickerNews(ticker, title, href, Outlet.BENZINGA, source, issuedDate);

                        return tickerNews;
                    }).collect(Collectors.toList());
            return getTickerNewsList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TickerNews> scrapeToday() {
			Document document = null;
			try {
				document = Jsoup.connect(url)
						.userAgent("Mozilla")
						.get();
        Elements headlines = document.select("a.content-headline");
          tickerNewsList = headlines.parallelStream()
              .filter(element -> {
                  String[] contentFooter = element.select("div.text-gray-400.text-sm").text().trim().split("-");
                  return !contentFooter[1].contains("Sponsor") && contentFooter[1].contains("hour");
              })
              .map(element -> {
                  String href = element.attr("href");
                  String title = element.select("div.content-title").first().text();
                  String[] contentFooter = element.select("div.text-gray-400.text-sm").text().trim().split("-");
                  String source = contentFooter[0].trim();
                  LocalDateTime issuedDate = recentPastToMins(contentFooter[1].trim());
                      TickerNews tickerNews = new TickerNews(ticker, title, href, Outlet.BENZINGA, source, issuedDate);
                      return tickerNews;
              }).collect(Collectors.toList());
          return getTickerNewsList();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
    }

    public List<TickerNews> getTickerNewsList(){
        return tickerNewsList;
    }
    public String getTicker() {
        return ticker;
    }

    public Thread getThread() {
        return thread;
    }

    @Override
    public void run() {
        if(isToday) {
            scrapeToday();
        } else {
            scrape();
        }
    }

    public static void main(String[] args) {
        Benzinga b = new Benzinga("AAPL");
//        b.scrape();
        System.out.println(b.scrapeToday());
//        System.out.println(b.getTickerNewsList());

    }

}
