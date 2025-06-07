package com.example.splitbooks.DTO.response;


import lombok.Data;
import java.util.List;

@Data
public class BookResponse {

    private String kind;
    private int totalItems;
    private List<Item> items;

    @Data
    public static class Item {

        private String kind;
        private String id;
        private String selfLink;
        private VolumeInfo volumeInfo;
    }


    @Data
    public static class VolumeInfo {
        private String title;
        private List<String> authors;
        private ImageLinks imageLinks;
        private String language;
    }

    @Data
    public static class ImageLinks {
        private String smallThumbnail;
        private String thumbnail;
    }

}

