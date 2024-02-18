package co.loyyee.scrape.model;

import java.util.List;
public interface Scraper<T>{
		List<T> scrape();
		List<T> getTickerNewsList();

		List<T> scrapeToday();

}
