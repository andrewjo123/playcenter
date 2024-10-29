package com.playground.repository;


import com.playground.entity.ItemCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemCodeRepository extends JpaRepository<ItemCode, Long> {

    @Query(value = "SELECT * FROM item_code i WHERE i.member_id IS NULL LIMIT :count", nativeQuery = true)
    List<ItemCode> getCode(@Param("count") int count);

}
