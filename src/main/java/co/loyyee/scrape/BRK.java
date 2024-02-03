package co.loyyee.scrape;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class BRK {

    public static void run(String[] args) {
       try {
        // String url = "https://www.berkshirehathaway.com/letters/1979.html";
        String url = "https://scrapeme.live/shop/";
        Document doc = Jsoup
        .connect(url)
         .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
     .get();
    //  File tempHtml = new File("temp.html");
        // FileUtils.writeStringToFile( tempHtml, doc.html(), "UTF-8");
    //   String letterContent = doc.select("pre").text();

        System.out.println(doc);
    } catch (IOException e) {
        e.printStackTrace();
    }
    }
}
