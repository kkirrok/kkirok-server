package com.kkirok.server.domain.character.application.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PossessingItemsResponse(
        @Schema(example = """
                [
                  {
                    "itemId": 1,
                    "name": "대박개쩌는 아이템",
                    "image": "550e8400-e29b-41d4-a716-446655440000-profile.png",
                    "isWearing": false,
                    "itemType": "HAT"
                  },
                  {
                    "itemId": 12,
                    "name": "좀 안 쩌는 아이템",
                    "image": "2c1f5c11-8d84-4f9e-9a1d-b73d63a1b712-character-image.jpg",
                    "isWearing": true,
                    "itemType": "PANTS"
                  }
                ]
                """)
        List<ItemInfo> possession,
        @Schema(example = """
                [
                  {
                    "itemId": 2,
                    "name": "멋있는 아이템",
                    "image": "c9d8aa7b-13e2-4ee5-8c87-6d7f6cb3c987-hat.png",
                    "isWearing": false,
                    "itemType": "HAT"
                  },
                  {
                    "itemId": 21,
                    "name": "못생긴 아이템",
                    "image": "9a0d52b4-93f1-4b58-9f97-fb6dbf3cb80a-shirts.png",
                    "isWearing": true,
                    "itemType": "SHIRTS"
                  }
                ]
                """)
        List<ItemInfo> notPossession
) {
}
