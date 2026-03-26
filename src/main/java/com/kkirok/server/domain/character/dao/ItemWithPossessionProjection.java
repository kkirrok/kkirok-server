package com.kkirok.server.domain.character.dao;

import com.kkirok.server.domain.character.domain.ItemType;

public interface ItemWithPossessionProjection {

    Long getItemId();

    String getName();

    String getImage();

    ItemType getItemType();

    boolean getPossession();

    Boolean getIsWearing();
}
