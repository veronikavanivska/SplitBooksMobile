package com.example.splitbooks.DTO.response;


import java.util.List;

import lombok.Data;

@Data
public class BookDetailResponse {
    private String id;
    private VolumeInfo volumeInfo;


    @Data
    public static class VolumeInfo {
        private String title;
        private String subtitle;
        private List<String> authors;
        private String publisher;
        private String publishedDate;
        private String description;
        private Integer pageCount;
        private List<String> categories;
        private ImageLinks imageLinks;
        private String language;
        private Boolean isInLibrary;
    }

    @Data
    public static class ImageLinks {
        private String smallThumbnail;
        private String thumbnail;
    }

}
