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

    @Transactional(readOnly = true)
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
