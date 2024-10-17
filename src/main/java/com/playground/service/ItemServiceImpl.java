package com.playground.service;

import com.playground.dto.ItemFormDto;
import com.playground.dto.ItemImgDto;
import com.playground.dto.ItemSearchDto;
import com.playground.dto.MainItemDto;
import com.playground.entity.Item;
import com.playground.entity.ItemCategory;
import com.playground.entity.ItemImg;
import com.playground.repository.ItemCategoryRepository;
import com.playground.repository.ItemImgRepository;
import com.playground.repository.ItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import javax.print.DocPrintJob;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemImgService itemImgService;
    private final ItemImgRepository itemImgRepository;
    private final ItemCategoryRepository categoryRepository;

    @Override
    @Transactional
    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {
        // Register item
        Item item = itemFormDto.createItem();
        itemRepository.save(item);

        // 추가
        ItemCategory category=new ItemCategory();
        category.setCompany(itemFormDto.getCompany());
        category.setTag(itemFormDto.getTag());
        category.setItem(item);
        categoryRepository.save(category);

        // Register images
        for (int i = 0; i < itemImgFileList.size(); i++) {
            ItemImg itemImg = new ItemImg();
            itemImg.setItem(item);

            itemImg.setRepimgYn(i == 0 ? "Y" : "N");

            itemImgService.saveItemImg(itemImg, itemImgFileList.get(i));
        }

        return item.getId();
    }

  
    @Override
    public ItemFormDto getItemDtl(Long itemId) {
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        List<ItemImgDto> itemImgDtoList = new ArrayList<>();
        for (ItemImg itemImg : itemImgList) {
            itemImgDtoList.add(ItemImgDto.of(itemImg));
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(EntityNotFoundException::new);
        ItemFormDto itemFormDto = ItemFormDto.of(item);
        itemFormDto.setItemImgDtoList(itemImgDtoList);

        //추가
        ItemCategory categories=categoryRepository.findByItemId(itemId);
        itemFormDto.setCompany(categories.getCompany());
        itemFormDto.setTag(categories.getTag());
        
        // 리뷰 추가
        List<Object[]>result=itemRepository.getAvgAndCount(itemId);

        if (result != null && result.get(0).length == 2) {
            Double avg = (Double) result.get(0)[0]; // Average grade
            Long allReview = (Long) result.get(0)[1];   // Count of reviews
            
            System.out.println("Average: " + avg);
            System.out.println("Count: " + allReview);

            itemFormDto.setAvg(avg);
            itemFormDto.setAllReview(allReview);
        } else {
            // Handle cases where there are no reviews or the item does not exist
            System.out.println("No reviews found or item does not exist.");
            itemFormDto.setAvg(0D);
            itemFormDto.setAllReview(0L);
        }

        
        return itemFormDto;
    }

    @Override
    @Transactional
    public Long updateItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {
        // Update item
        Item item = itemRepository.findById(itemFormDto.getId())
                .orElseThrow(EntityNotFoundException::new);
        item.updateItem(itemFormDto);
        List<Long> itemImgIds = itemFormDto.getItemImgIds();

        // Update images
        for (int i = 0; i < itemImgFileList.size(); i++) {
            itemImgService.updateItemImg(itemImgIds.get(i), itemImgFileList.get(i));
        }

        //추가
        ItemCategory category=categoryRepository.findByItemId(item.getId());
        category.setCompany(itemFormDto.getCompany());
        category.setTag(itemFormDto.getTag());
        categoryRepository.save(category);

        return item.getId();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getAdminItemPage(itemSearchDto, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {
        return itemRepository.getMainItemPage(itemSearchDto, pageable);
    }
}
