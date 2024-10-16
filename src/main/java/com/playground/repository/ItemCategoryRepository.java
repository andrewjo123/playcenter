package com.playground.repository;

import com.playground.entity.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {
    ItemCategory findByItemId(Long itemId);
}
