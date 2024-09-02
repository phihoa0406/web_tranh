package com.example.web_tranh.dao;

import com.example.web_tranh.entity.Feedbacks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "feedbacks")
public interface FeedBacksRepository extends JpaRepository<Feedbacks, Integer> {
    long countBy();
}
