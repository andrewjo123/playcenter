package com.playground.repository;

import com.playground.entity.ItemImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ItemImgRepository extends JpaRepository<ItemImg, Long> {

    List<ItemImg> findByItemIdOrderByIdAsc(Long itemId);

    ItemImg findByItemIdAndRepimgYn(Long itemId, String repimgYn);

    @Modifying
    @Transactional
    @Query("delete from ItemImg i where i.item.id = :itemId")
    void deleteByItemId(Long itemId);

    // itemId에 해당하는 모든 이미지 조회
    List<ItemImg> findByItemId(Long itemId);

    //Task, 모든 이미지 조회
    @Query("select i from ItemImg i")
    List<ItemImg> getImgList();
}