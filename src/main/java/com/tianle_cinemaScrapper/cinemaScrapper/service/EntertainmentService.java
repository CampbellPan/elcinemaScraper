package com.tianle_cinemaScrapper.cinemaScrapper.service;


import com.tianle_cinemaScrapper.cinemaScrapper.model.EntertainmentDocument;
import com.tianle_cinemaScrapper.cinemaScrapper.model.EntertainmentItem;
import com.tianle_cinemaScrapper.cinemaScrapper.repository.EntertainmentDocumentRepository;
import com.tianle_cinemaScrapper.cinemaScrapper.repository.EntertainmentItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.tianle_cinemaScrapper.cinemaScrapper.utils.CsvUtils.writeToCSV;

@Service
public class EntertainmentService {
    private final EntertainmentItemRepository itemRepository;
    private final EntertainmentDocumentRepository documentRepository;

    private static final Logger logger = LoggerFactory.getLogger(ScraperService.class);

    public EntertainmentService(EntertainmentItemRepository itemRepository, EntertainmentDocumentRepository documentRepository) {
        this.itemRepository = itemRepository;
        this.documentRepository = documentRepository;
    }

    /**
     * save EntertainmentItem to MySQL
     */
    public void saveToMySQL(EntertainmentItem item){
        try {
            if("Movie".equalsIgnoreCase(item.getType())){
                itemRepository.saveToMovies(item);
            }else if ("Series".equalsIgnoreCase(item.getType()) || "TV".equalsIgnoreCase(item.getType())){
                itemRepository.saveToTvShows(item);
            }else{
                throw new IllegalArgumentException("Invalid type: " + item.getType());
            }
        } catch (Exception e) {
            System.err.println("Failed to save in MYSQL：" + item.getElCinemaId());
            writeToCSV("failed_saved_in_mysql", item.getElCinemaId(), item.getElCinemaUrl(), e.getMessage());
        }
    }

    /**
     * save EntertainmentDocument to MongoDB
     */
    public void saveToMongoDB(EntertainmentDocument document) {
        try {
            // find if elCinemaId has existed
            Optional<EntertainmentDocument> existingDocument = documentRepository.findById(document.getElCinemaId());
            if (existingDocument.isPresent()) {
                EntertainmentDocument updatedDocument = existingDocument.get();
                updatedDocument.setTitle(document.getTitle());
                updatedDocument.setDescription(document.getDescription());
                updatedDocument.setGenre(document.getGenre());
                updatedDocument.setDirector(document.getDirector());
                updatedDocument.setCast(document.getCast());

                documentRepository.save(updatedDocument);
                logger.info("[Updated]：{} in MongoDB", document.getTitle());
            } else {
                documentRepository.save(document);
                logger.info("[Inserted]：{} into MongoDB", document.getTitle());
            }
        } catch (Exception e) {
            System.err.println("Failed to save in MongoDB：" + document.getElCinemaId());
            writeToCSV("failed_saved_in_mongodb", document.getElCinemaId(), "N/A", e.getMessage());
        }
    }
}
