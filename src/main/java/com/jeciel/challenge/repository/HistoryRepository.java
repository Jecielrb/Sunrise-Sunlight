package com.jeciel.challenge.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.jeciel.challenge.history.History;

@Repository
public interface HistoryRepository extends MongoRepository<History, String>{

}
