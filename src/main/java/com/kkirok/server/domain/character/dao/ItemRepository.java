package com.kkirok.server.domain.character.dao;

import com.kkirok.server.domain.character.domain.Item;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("""
        select 
            i.id,
            i.name,
            i.itemImage,
            i.itemType,
            case when ip.id is not null then true else false end,
            ip.isWearing
        from Item i
        left join ItemPossession ip
            on ip.item = i
           and ip.character.member.id = :memberId
    """)
    List<ItemWithPossessionProjection> findItemsWithPossessions(@Param("memberId") Long memberId);

}