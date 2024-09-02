package com.example.web_tranh.dao;

import com.example.web_tranh.entity.Art;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RepositoryRestResource(path = "arts")
public interface ArtRepository extends JpaRepository<Art, Integer> {
    Page<Art> findByNameArtContaining(@RequestParam("nameArt") String nameArt, Pageable pageable);
    Page<Art> findByAuthorId(@RequestParam("idAuthor") int idAuthor, Pageable pageable);
    Page<Art> findByReviewStatus(@RequestParam("reviewStatus") String reviewStatus, Pageable pageable);
    Page<Art> findByListGenres_idGenre(@RequestParam("idGenre") int idGenre, Pageable pageable);


    Page<Art> findByNameArtContainingAndListGenres_idGenre(@RequestParam("nameArt") String nameArt ,@RequestParam("idGenre") int idGenre, Pageable pageable);
    List<Art> findArtByReviewStatus(String reviewStatus);
    List<Art> findAllByIdArtIn(List<Integer> ids);
    long count();
}
