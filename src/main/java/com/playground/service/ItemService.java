package com.playground.service;

import com.playground.dto.ItemCategoryDto;
import com.playground.dto.ItemFormDto;
import com.playground.dto.ItemSearchDto;
import com.playground.dto.MainItemDto;
import com.playground.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ItemService {
    Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception;

    ItemFormDto getItemDtl(Long itemId);

    Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception;

    Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable);

    Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, ItemCategoryDto itemCategoryDto, Pageable pageable);

    Page<MainItemDto> getMainItemPage2(String company, ItemSearchDto itemSearchDto, ItemCategoryDto itemCategoryDto,Pageable pageable);

    int saveCodes(Long itemId,List<String> codes);
    List<MainItemDto> getMainItem(String company, ItemCategoryDto itemCategoryDto);
}
