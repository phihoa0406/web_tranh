package com.example.web_tranh.dao;

import com.example.web_tranh.entity.Art;
import com.example.web_tranh.entity.FavoriteArt;
import com.example.web_tranh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path = "favorite-art")
public interface FavoriteArtRepository extends JpaRepository<FavoriteArt, Integer> {
    public FavoriteArt findFavoriteArtByArtAndUser(Art art, User user);
    public List<FavoriteArt> findFavoriteArtsByUser(User user);
    @Query("SELECT f FROM FavoriteArt f WHERE f.user.idUser = :userId")
    List<FavoriteArt> findFavoriteArtsByUserId(@Param("userId") int userId);
    @Query("SELECT COUNT(fa) > 0 FROM FavoriteArt fa " +
            "WHERE fa.art.idArt = :artId AND fa.user.idUser = :userId")
    boolean existsByArtIdAndUserId(@Param("artId") int artId, @Param("userId") int userId);

    List<FavoriteArt> findByUser_IdUser(int userId);
    @Query("SELECT f.art FROM FavoriteArt f WHERE f.user.idUser = :idUser")
    List<Art> findArtsByUserId(@Param("idUser") int idUser);

}
