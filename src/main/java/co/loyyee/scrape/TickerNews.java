package co.loyyee.scrape;

import java.time.LocalDateTime;

public record TickerNews(String ticker, String title, String href, LocalDateTime issuedDatetime) {

}