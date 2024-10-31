package com.playground.repository;

import com.playground.dto.DibsDto;
import org.springframework.data.jpa.repository.JpaRepository;
import com.playground.entity.DibsItem;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface DibsItemRepository extends JpaRepository<DibsItem, Long>{
    DibsItem findByDibsIdAndItemId(Long dibsId, Long itemId);

    @Query("select new com.playground.dto.DibsDto(i.id, di.id, i.itemNm, i.price, im.imgUrl) " +
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
}
