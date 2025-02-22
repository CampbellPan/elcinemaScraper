package com.tianle_cinemaScrapper.cinemaScrapper.service;

import com.tianle_cinemaScrapper.cinemaScrapper.model.EntertainmentDocument;
import com.tianle_cinemaScrapper.cinemaScrapper.model.EntertainmentItem;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tianle_cinemaScrapper.cinemaScrapper.utils.CsvUtils.writeToCSV;


@Service
public class ScraperService {

    private final EntertainmentService entertainmentService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(15); // 5 线程并行爬取
    private static final String MOVIES_URL = "https://elcinema.com/en/index/work/category/1";
    private static final String TVSHOWS_URL = "https://elcinema.com/en/index/work/category/3";
    private static final String BASE_URL = "https://elcinema.com/en/work/";

    public ScraperService(EntertainmentService entertainmentService) {
        this.entertainmentService = entertainmentService;
    }



    public void startScraping(){
        System.out.println("===========scrapping ElCinema data begin=====================");
        //get all the links that need to be scrapped
        List<String> movieLinks = getWorkLinks(MOVIES_URL);
        List<String> tvShowLinks = getWorkLinks(TVSHOWS_URL);


        Set<String> allLinks = new HashSet<>();
        allLinks.addAll(movieLinks);
        allLinks.addAll(tvShowLinks);

        List<Future<Void>> futures = new ArrayList<>();
        for(String link : allLinks){
            try {
                Thread.sleep(300 + (int) (Math.random() * 500));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            futures.add(executorService.submit(() -> {
                scrapeAndSave(link);
                return null;
            }));
        }

        //after all the tasks finish
        for(Future<Void> future: futures){
            try {
                future.get();
            } catch (Exception e) {
                System.out.println("Thread execution error: " + e.getMessage());
            }
        }



        executorService.shutdown();
        System.out.println("executor service has shut down. Waiting for all the tasks to be completed.");
        boolean finished = false;
        try {
            finished = executorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if(finished){
            System.out.println("All the tasks have been completed.");
        }else{
            System.out.println("Time expired. There are tasks not completed.");
        }
        System.out.println("~~~~~~~~~~~~ Scrapping finish ~~~~~~~~~~~~~~");
    }

    List<String> getWorkLinks(String categoryUrl){
        List<String> workLinks = new ArrayList<>();

        //obtain the maximum page number
        int totalPages = findTotalPages(categoryUrl);
        int maxPage = Math.min(totalPages,2); // for development
        if(totalPages == 0){
            totalPages = 1;
        }

        for(int page = 1; page <= maxPage; page++) {
            String pageUrl = categoryUrl + "?page=" + page;
            System.out.println("Fetching page: " + pageUrl);
            try {
                Document doc = Jsoup.connect(pageUrl).get();
                List<Element> links = doc.select("a[href^=/en/work/]");
                for (Element link : links) {
                    String href = link.attr("href");
                    if (href.matches("/en/work/\\d+/")) {
                        String detailUrl = BASE_URL + href.split("/")[3] + "/";
                        workLinks.add(detailUrl);
                    }
                }

            } catch (IOException e) {
                System.out.println("Failed to parse the page: " + e.getMessage());
            }
        }
        System.out.println("Totally achieved " + workLinks.size() + " work links from " + categoryUrl);
        return workLinks;

    }

    private int findTotalPages(String categoryUrl) {
        try {
            Document doc = Jsoup.connect(categoryUrl).get();
            Elements paginationLinks = doc.select("ul.pagination li a");
            int maxPage = 1;

            for (Element link : paginationLinks) {
                String url = link.attr("href"); // e.g. /en/index/work/category/1?page=2
                Matcher m = Pattern.compile("page=(\\d+)").matcher(url);
                if (m.find()) {
                    int page = Integer.parseInt(m.group(1));
                    if (page > maxPage) {
                        maxPage = page;
                    }
                }
            }
            System.out.println("Current category has " + maxPage + " pages.");
            return maxPage;
        } catch (IOException e) {
            System.err.println("Failed to find total pages for " + categoryUrl + ": " + e.getMessage());
            return 0;
        }
    }

    void scrapeAndSave(String workUrl){
        String elCinemaId = workUrl.split("/")[5];
        int maxRetry = 3;
        int attempt = 0;
        while(attempt < maxRetry) {
            try {
                Document doc = Jsoup.connect(workUrl).get();

                String title = parseTitle(doc);
                String releaseDate = parseReleaseDate(doc);
                String type = parseType(doc);
                String description = parseDescription(doc);
                List<String> directors = parseDirector(doc);
                List<String> cast = parseCast(doc);
                List<String> genre = parseGenre(doc);

                // 控制台输出爬取的数据
                System.out.println("----------[SCRAPED DATA]--------------");
                System.out.println("🎬 title: " + title);
                System.out.println("📅 releaseDate: " + releaseDate);
                System.out.println("📌 Type: " + type);
                System.out.println("📖 description: " + (description.length() > 100 ? description.substring(0, 100) + "..." : description));
                System.out.println("🎭 director: " + String.join(", ", directors));
                System.out.println("👥 cast: " + String.join(", ", cast));
                System.out.println("🎭 genre: " + String.join(", ", genre));
                System.out.println("🔗 Url: " + workUrl);
                System.out.println("--------------------------------------------");

                //save into Mysql
                EntertainmentItem item = new EntertainmentItem();
                item.setElCinemaId(elCinemaId);
                item.setTitle(title);
                item.setReleaseDate(new SimpleDateFormat("yyyy-MM-dd").parse(releaseDate));
                item.setType(type);
                item.setElCinemaUrl(workUrl);
                entertainmentService.saveToMySQL(item);
                System.out.println("[Saved]：" + title + " into MYSQL");
                //save into MongoDB
                EntertainmentDocument document = new EntertainmentDocument();
                document.setElCinemaId(elCinemaId);
                document.setTitle(title);
                document.setDescription(description);
                document.setGenre(genre);
                document.setDirector(directors);
                document.setCast(cast);
                entertainmentService.saveToMongoDB(document);
                System.out.println("[Saved]：" + title + " into MongoDB");
                return;
            } catch (IOException e) {
                if (e.getMessage().contains("404")) {
                    System.err.println("[⚠️ 404] page doesn't exist: " + workUrl);
                    attempt++;
                    System.err.println("[⏳ Retry] reScraping... " + workUrl + "[" + attempt + "]");
                    if(attempt == 3) {
                        System.out.println("Failed to scrape" + workUrl + ": max retries have used, error reason: " + e.getMessage());
                        writeToCSV("failed_scrape", elCinemaId, workUrl, e.getMessage());
                    }
                    try {
                        Thread.sleep(500 + (int) (Math.random() * 200)); // **随机间隔，避免过快请求**
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.out.println("IOException: " + e.getMessage());
                }
            } catch (ParseException e) {
                writeToCSV("failed_scrape", elCinemaId, workUrl, e.getMessage());
            }
        }

    }

    private String parseTitle(Document doc) {
        String title = doc.select("span.left").text().trim();
        if (title.isEmpty()) {
            title = doc.select("input[name='title-en']").attr("value").trim();
        }
        // remove trailing "(2023)" if any
        title = title.replaceAll("\\(\\d{4}\\)$", "").trim();
        return title;
    }


    private String parseReleaseDate(Document doc) {
        Element releaseDateElement = doc.select("li:contains(Release Date)").first();
        if (releaseDateElement == null) return "0000-00-00";

        String raw = releaseDateElement.select("a").eachText().stream()
                .reduce((a, b) -> a + " " + b)
                .orElse("Unknown");
        raw = raw.replace("(more)", "").trim();

        if (raw.isEmpty() || "Unknown".equalsIgnoreCase(raw)) {
            return "0000-00-00";
        }
        // convert to yyyy-MM-dd
        return convertDateFormat(raw);
    }

    private String convertDateFormat(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).parse(date));
        } catch (Exception e) {
            return "0000-00-00";
        }
    }

    private String parseType(Document doc) {
        Element e = doc.select("ul.list-separator a").first();
        return (e != null) ? e.text().trim() : "Unknown";
    }

    private String parseDescription(Document doc) {
        // 1️. find <p> after div `id=unit-1674146082118`
        Element descriptionDiv = doc.selectFirst("div#unit-1674146082118");
        if (descriptionDiv != null) {
            Element nextParagraph = descriptionDiv.nextElementSibling();
            if (nextParagraph != null && "p".equals(nextParagraph.tagName())) {
                return cleanDescription(nextParagraph);
            }
        }

        // 2. if cannot find any, try:
        for (Element p : doc.select("p")) {
            String text = cleanDescription(p);
            if (!text.isEmpty()) {
                return text; // 避免重复获取
            }
        }

        return "No description available";
    }

    private String cleanDescription(Element p) {
        // remove "...Read more"
        String text = p.text().replace("...Read more", "").trim();

        Element hiddenSpan = p.selectFirst("span.hide");
        if (hiddenSpan != null) {
            text += " " + hiddenSpan.text().trim();
        }

        return text.trim();
    }


    private List<String> parseDirector(Document doc) {
        List<String> directors = doc.select("li:contains(Director) ~ li a").eachText();
        directors.removeIf(d -> "(more)".equalsIgnoreCase(d));
        return directors;
    }

    private List<String> parseCast(Document doc) {
        List<String> cast = doc.select("div:contains(Cast) + div a").eachText();
        cast.removeIf(name -> name.equalsIgnoreCase("More")); // 过滤掉 "More"
        return cast;
    }

    private List<String> parseGenre(Document doc) {
        Element genreElement = doc.select("li:contains(Genre)").first();
        if (genreElement == null) return new ArrayList<>();
        List<String> genre = genreElement.select("a").eachText();
        genre.removeIf(g -> "(more)".equalsIgnoreCase(g));
        return genre;
    }


}
