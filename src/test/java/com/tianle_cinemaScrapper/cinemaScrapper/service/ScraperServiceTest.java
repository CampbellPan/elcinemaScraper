package com.tianle_cinemaScrapper.cinemaScrapper.service;

import com.tianle_cinemaScrapper.cinemaScrapper.model.EntertainmentDocument;
import com.tianle_cinemaScrapper.cinemaScrapper.model.EntertainmentItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

class ScraperServiceTest {

    private ScraperService scraperService;

    private EntertainmentService entertainmentService;

    @BeforeEach
    void setUp() {
        // use spy to pack scraperService，make getWorkLinks() return customed data
        entertainmentService = mock(EntertainmentService.class);
        scraperService = spy(new ScraperService(entertainmentService));

    }

    @Test
    void testStartScrapingWithTypicalLinks(){
        List<String> testLinks = Arrays.asList(
                "https://elcinema.com/en/work/2087662/",
                "https://elcinema.com/en/work/2063593/",
                "https://elcinema.com/en/work/2087351/"
        );
        // 🔹 Mock `getWorkLinks()` 方法，返回固定的 4 个链接
        doReturn(testLinks).when(scraperService).getWorkLinks(anyString());

    // 运行 `startScraping()`
        scraperService.startScraping();

    // ✅ `scrapeAndSave()` 现在会运行真实逻辑
        verify(scraperService, times(1)).scrapeAndSave("https://elcinema.com/en/work/2087662/");
        verify(scraperService, times(1)).scrapeAndSave("https://elcinema.com/en/work/2063593/");
        verify(scraperService, times(1)).scrapeAndSave("https://elcinema.com/en/work/2087351/");
    }

    @Test
    void testStartScrapingWithInvalidLinks(){
        List<String> testLinks = Arrays.asList(
                "https://elcinema.com/en/work/111/"
        );
        doReturn(testLinks).when(scraperService).getWorkLinks(anyString());

        // 运行 `startScraping()`
        scraperService.startScraping();

        verify(scraperService, times(1)).scrapeAndSave("https://elcinema.com/en/work/111/");
    }

    @Test
    void testCallsDatabaseMethods() {
        String testUrl = "https://elcinema.com/en/work/2087351/";
        scraperService.scrapeAndSave(testUrl);

        verify(entertainmentService, times(1)).saveToMySQL(any(EntertainmentItem.class));
        verify(entertainmentService, times(1)).saveToMongoDB(any(EntertainmentDocument.class));
    }

}