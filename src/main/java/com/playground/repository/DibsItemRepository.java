package com.playground.repository;

import com.playground.dto.DibsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import com.playground.entity.DibsItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DibsItemRepository extends JpaRepository<DibsItem, Long>{
    DibsItem findByDibsIdAndItemId(Long dibsId, Long itemId);

    @Query("select new com.playground.dto.DibsDto(i.id, di.id, i.itemNm, i.price, im.imgUrl, i.stockNumber) " +
            "from DibsItem di, ItemImg im " +
            "join di.item i " +
            "where di.dibs.id = :dibsId " +
            "and im.item.id = di.item.id " +
            "and im.repimgYn = 'Y' " +
            "order by di.regTime desc"
    )
    List<DibsDto> findDibsDtoList(Long dibsId);

    long countByDibsId(Long dibsId);

    @Query(value="SELECT di.item_id FROM dibs_item di JOIN dibs d ON d.dibs_id=di.dibs_id JOIN member m ON m.member_id=d.member_id WHERE m.email=:email", nativeQuery = true)
    List<Object> getItemIDofDibs(String email);

    // Task, 탈퇴한 유저의 찜 아이템 찾기
    @Query("select di from DibsItem di where di.dibs.id=:dibsId")
    List<DibsItem> findDibsItemForDelete(@Param("dibsId") Long dibsId);
}
