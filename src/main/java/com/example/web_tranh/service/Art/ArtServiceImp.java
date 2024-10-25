package com.example.web_tranh.service.Art;

import com.example.web_tranh.dao.ArtRepository;
import com.example.web_tranh.dao.GenreRepository;
import com.example.web_tranh.dao.ImageRepository;
import com.example.web_tranh.entity.Art;
import com.example.web_tranh.entity.Genre;
import com.example.web_tranh.entity.Image;
import com.example.web_tranh.service.Base64ToMultipartFileConverter;
import com.example.web_tranh.service.UploadImage.UploadImageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ArtServiceImp implements ArtService
{
    private final ObjectMapper objectMapper;
    @Autowired
    private ArtRepository artRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private UploadImageService uploadImageService;

    public ArtServiceImp(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ResponseEntity<?> save(JsonNode artJson)
    {
        try {
            Art art = objectMapper.treeToValue(artJson, Art.class);
            // Lưu thể loại của tranh
            List<Integer> idGenreList = objectMapper.readValue(artJson.get("idGenres").traverse(),
                                        new TypeReference<List<Integer>>() {});
            List<Genre> genreList = new ArrayList<>();
            for (int idGenre : idGenreList) {
                Optional<Genre> genre = genreRepository.findById(idGenre);
                genreList.add(genre.get());
            }
            // gan vao db
            art.setListGenres(genreList);

            // Lưu trước để lấy id tranh đặt tên cho ảnh
            Art newArt = artRepository.save(art);

            // Lưu thumbnail cho ảnh
            String dataThumbnail = formatStringByJson(String.valueOf((artJson.get("thumbnail"))));

            Image thumbnail = new Image();
            thumbnail.setArt(newArt);
            thumbnail.setThumbnail(true);
            MultipartFile multipartFile = Base64ToMultipartFileConverter.convert(dataThumbnail);
            String thumbnailUrl = uploadImageService.uploadImage(multipartFile, "Art_" + newArt.getIdArt());
            thumbnail.setUrlImage(thumbnailUrl);

            List<Image> imagesList = new ArrayList<>();
            imagesList.add(thumbnail);

            // Lưu những ảnh có liên quan
            String dataRelatedImg = formatStringByJson(String.valueOf((artJson.get("relatedImg"))));
            List<String> arrDataRelatedImg = objectMapper.readValue(artJson.get("relatedImg").traverse(), new TypeReference<List<String>>() {
            });

            for (int i = 0; i < arrDataRelatedImg.size(); i++) {
                String img = arrDataRelatedImg.get(i);
                Image image = new Image();
                image.setArt(newArt);
                image.setThumbnail(false);
                MultipartFile relatedImgFile = Base64ToMultipartFileConverter.convert(img);
                String imgURL = uploadImageService.uploadImage(relatedImgFile, "Art_" + newArt.getIdArt() + "." + i);
                image.setUrlImage(imgURL);
                imagesList.add(image);
            }

            newArt.setListImages(imagesList);
            // Cập nhật lại ảnh
            artRepository.save(newArt);

            return ResponseEntity.ok("Success!");
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> update(JsonNode artJson)
    {
        try {
            // Chuyển JSON thành đối tượng Art
            Art art = objectMapper.treeToValue(artJson, Art.class);

            // Lưu thể loại liên quan
            List<Integer> idGenreList = objectMapper.readValue(artJson.get("idGenres").traverse(), new TypeReference<List<Integer>>() {});
            List<Genre> genreList = new ArrayList<>();
            for (int idGenre : idGenreList) {
                genreRepository.findById(idGenre).ifPresent(genreList::add);
            }
            art.setListGenres(genreList);

            // Danh sách ảnh hiện tại
            List<Image> currentImages = imageRepository.findImagesByArt(art);

            // Cập nhật thumbnail
            String thumbnailData = artJson.get("thumbnail").asText();
            if (Base64ToMultipartFileConverter.isBase64(thumbnailData)) {
                MultipartFile thumbnailFile = Base64ToMultipartFileConverter.convert(thumbnailData);
                String thumbnailUrl = uploadImageService.uploadImage(thumbnailFile, "Art_" + art.getIdArt());

                Image thumbnailImage = currentImages.stream()
                        .filter(Image::isThumbnail)
                        .findFirst()
                        .orElse(new Image());
                thumbnailImage.setArt(art);
                thumbnailImage.setUrlImage(thumbnailUrl);
                thumbnailImage.setThumbnail(true);
                imageRepository.save(thumbnailImage);
            }

            // Danh sách ảnh liên quan
            List<String> relatedImagesData = objectMapper.readValue(artJson.get("listImages").traverse(), new TypeReference<List<String>>() {});
            List<Image> updatedImages = new ArrayList<>();

            for (int i = 0; i < relatedImagesData.size(); i++) {
                String imgData = relatedImagesData.get(i);
                if (Base64ToMultipartFileConverter.isBase64(imgData)) {
                    MultipartFile relatedFile = Base64ToMultipartFileConverter.convert(imgData);
                    String imgUrl = uploadImageService.uploadImage(relatedFile, "Art_" + art.getIdArt() + "_" + i);

                    Image image = new Image();
                    image.setArt(art);
                    image.setThumbnail(false);
                    image.setUrlImage(imgUrl);
                    updatedImages.add(image);
                }
            }

            // Xóa các ảnh cũ không còn trong danh sách
            List<Image> imagesToDelete = currentImages.stream()
                    .filter(img -> !img.isThumbnail() && !updatedImages.contains(img))
                    .toList();
            imageRepository.deleteAll(imagesToDelete);

            // Lưu lại danh sách ảnh mới
            imageRepository.saveAll(updatedImages);

            // Cập nhật danh sách ảnh trong Art
            art.setListImages(updatedImages);
            artRepository.save(art);
            updateArtReviewStatus(art.getIdArt(),"Chờ duyệt");
            return ResponseEntity.ok("Update successful!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("An error occurred: " + e.getMessage());
        }
    }

    public void updateArtReviewStatus(int idArt, String reviewStatus) {
        // Tìm tranh theo ID
        Art art = artRepository.findById(idArt)
                .orElseThrow(() -> new NoSuchElementException("Art not found with ID: " + idArt));

        // Cập nhật reviewStatus
        art.setReviewStatus(reviewStatus);

        // Lưu thay đổi vào cơ sở dữ liệu
        artRepository.save(art);
    }

    @Override
    public long getTotalArt() {
        return artRepository.count();
    }

    private String formatStringByJson(String json) {
        return json.replaceAll("\"", "");
    }
}
