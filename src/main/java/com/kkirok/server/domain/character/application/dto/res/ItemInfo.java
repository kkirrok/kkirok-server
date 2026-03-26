package com.kkirok.server.domain.character.application.dto.res;

import com.kkirok.server.domain.character.dao.ItemWithPossessionProjection;
import com.kkirok.server.domain.character.domain.ItemType;
import io.swagger.v3.oas.annotations.media.Schema;

public record ItemInfo(
    @Schema(example = "1")
    Long itemId,
    @Schema(example = "대박개쩌는 아이템")
    String name,
    @Schema(example = "550e8400-e29b-41d4-a716-446655440000-profile.png")
    String image,
    @Schema(example = "false")
    Boolean isWearing,
    @Schema(example = "HAT")
    ItemType itemType
) {

    public static ItemInfo fromProjection(ItemWithPossessionProjection projection) {
        return new ItemInfo(
                projection.getItemId(),
                projection.getName(),
                projection.getImage(),
                Boolean.TRUE.equals(projection.getIsWearing()),
                projection.getItemType()
        );
    }

}
