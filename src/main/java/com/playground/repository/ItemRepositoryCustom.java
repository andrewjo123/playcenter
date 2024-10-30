package com.playground.repository;

import com.playground.dto.ItemCategoryDto;
import com.playground.dto.ItemSearchDto;
import com.playground.dto.MainItemDto;
import com.playground.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepositoryCustom {

    Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable);

    Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, ItemCategoryDto itemCategoryDto, Pageable pageable);

    Page<MainItemDto> getMainItemPage2(String company, ItemSearchDto itemSearchDto, ItemCategoryDto itemCategoryDto,Pageable pageable);

    Page<MainItemDto> getMainItem(String company, ItemSearchDto itemSearchDto, ItemCategoryDto itemCategoryDto, Pageable pageable);
}