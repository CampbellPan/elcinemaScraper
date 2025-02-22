package com.tianle_cinemaScrapper.cinemaScrapper.repository;

import com.tianle_cinemaScrapper.cinemaScrapper.model.EntertainmentDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntertainmentDocumentRepository extends MongoRepository<EntertainmentDocument, String> {
}
