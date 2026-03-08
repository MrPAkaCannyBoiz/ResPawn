package com.respawn.services.extensions;

import com.respawnmarket.Image;
import com.respawn.entities.ImageEntity;

import java.util.List;

public class ImageExtension
{
    public static List<Image> toProtoImageList(List<ImageEntity> imageEntities)
    {
        return imageEntities.stream()
                .map(imageEntity -> Image.newBuilder()
                        .setId(imageEntity.getId())
                        .setUrl(imageEntity.getImageUrl())
                        .setProductId(imageEntity.getProduct().getId())
                        .build())
                .toList();
    }
}
