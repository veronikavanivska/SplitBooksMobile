package com.example.splitbooks.DTO.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditGenres {
    private List<Long> selectedGenres;


}
