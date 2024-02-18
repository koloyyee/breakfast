package co.loyyee.scrape;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.loyyee.scrape.Finviz;
import co.loyyee.scrape.Benzinga;

/**
 * A multithreaded scraping all started.
 * */
public class ScrapeAll {
     final static private Logger log = LoggerFactory.getLogger(ScrapeAll.class);

    public static void main(String[] args) {
        String[] tickers = new String[]{"AAPL", "GOOG", "META", "BRK-A", "TSLA"};
        ArrayList<Finviz> fscrapers = new ArrayList<>();
        ArrayList<Benzinga> bscrapers = new ArrayList<>();

        for(String ticker : tickers) {
            fscrapers.add(new Finviz(ticker, true));
            bscrapers.add(new Benzinga(ticker, true));
        }
////        List<Finviz> combinedList = new ArrayList<>(fscrapers);
//        for(Benzinga b : bscrapers)  {
//            try {
//                b.getThread().join();
//                log.info(b.getTickerNewsList().toString());
////                System.out.println(b.getTickerNewsList().toString());
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }

for(int i = 0; i < tickers.length; i++) {
    Benzinga b = bscrapers.get(i);
    Finviz f = fscrapers.get(i);
    try {
        b.getThread().join();
        f.getThread().join();
        log.info(b.getTickerNewsList().toString());
        log.info(f.getTickerNewsList().toString());
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
}
//        for(Finviz f : fscrapers) {
//            try {
//                f.getThread().join();
//                log.info(f.getTickerNewsList().toString());
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                throw new RuntimeException(e);
//            }
//        }
    }
    public static void vThread() throws InterruptedException, ExecutionException {
        String[] tickers = new String[]{"AAPL", "GOOG", "META", "BRK-A"};
        List<Callable<Finviz>> fscrapers = new ArrayList<>();
        List<Callable<Benzinga>> bscrapers = new ArrayList<>();

        for(String ticker : tickers) {
            fscrapers.add(()->new Finviz(ticker));
            bscrapers.add(()->new Benzinga(ticker));
        }

        ExecutorService service = Executors.newVirtualThreadPerTaskExecutor();
        for(Future<Finviz> f : service.invokeAll(fscrapers)) {
            Finviz finviz = f.get();
            finviz.getTickerNewsList().forEach(System.out::println);
        }
    }
    /**
     * TODO: Add method daily scrape with limit today's date.
     * TODO: turn the result into CSV
     * TODO: Send Email to myself with CSV attached
     *       - reference: https://www.baeldung.com/java-email
     * TODO: Add logging
     * TODO: Schedule the task to run every morning at 6:00am
     *       - reference: https://github.com/jobrunr/jobrunr?tab=readme-ov-file
     *       - reference: https://stackoverflow.com/questions/76587253/how-to-use-virtual-threads-with-scheduledexecutorservice
     *       - reference: https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ScheduledExecutorService.html
     *       - reference: https://howtodoinjava.com/java/multi-threading/scheduledexecutorservice/
     * TODO: Add JUnit tests
     * TODO: Add CLI feature
     * TODO: Publish to Github
     *
     * Nice to have features:
     * SQLITE 3: reference https://www.sqlitetutorial.net/sqlite-java/
     * */
}

