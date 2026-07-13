package com.astrotech.chat.cloudinary;

import jakarta.validation.constraints.NotBlank;


import java.util.List;

public record CloudinaryImageDeletionRequest (
        @NotBlank(message = "Public IDs needed")
        List<String> publicIds
){

}
