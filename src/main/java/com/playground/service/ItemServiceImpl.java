package com.playground.service;

import com.playground.constant.ItemSellStatus;
import com.playground.dto.*;
import com.playground.entity.Item;
import com.playground.entity.ItemCategory;
import com.playground.entity.ItemCode;
import com.playground.entity.ItemImg;
import com.playground.repository.*;
import com.playground.repository.ItemRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.List;

import static com.playground.dto.ItemCategoryDto.modelMapper;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemImgService itemImgService;
    private final ItemImgRepository itemImgRepository;
    private final ItemCategoryRepository categoryRepository;
    private final ItemCodeRepository itemCodeRepository;
    private final EmailService emailService;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public Long saveItem(ItemFormDto itemFormDto, List<MultipartFile> itemImgFileList) throws Exception {
        // Register item
        Item item = itemFormDto.createItem();
        item.setItemSellStatus(ItemSellStatus.SOLD_OUT);
        itemRepository.save(item);

        ItemCategory category=itemFormDto.getItemCategoryDto().toEntity(item, itemFormDto.getCompany());
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
        ItemCategoryDto itemCategoryDto=ItemCategoryDto.fromEntity(categories);
        itemFormDto.setItemCategoryDto(itemCategoryDto);
        
        // 리뷰 추가
        List<Object[]>result=itemRepository.getAvgAndCount(itemId);

        if (result != null && result.get(0).length == 2) {
            Double avg = (Double) result.get(0)[0]; // Average grade
            Long allReview = (Long) result.get(0)[1];   // Count of reviews

            itemFormDto.setAvg(avg);
            itemFormDto.setAllReview(allReview);
        } else {
            // Handle cases where there are no reviews or the item does not exist
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
        if(itemFormDto.getStockNumber()==0){
            itemFormDto.setItemSellStatus(ItemSellStatus.SOLD_OUT);
        }else{
            itemFormDto.setItemSellStatus(ItemSellStatus.SELL);
        }
        item.updateItem(itemFormDto);
        List<Long> itemImgIds = itemFormDto.getItemImgIds();

        // Update images
        for (int i = 0; i < itemImgFileList.size(); i++) {
            itemImgService.updateItemImg(itemImgIds.get(i), itemImgFileList.get(i));
        }

        //추가
        ItemCategory category=categoryRepository.findByItemId(item.getId());
        modelMapper.map(itemFormDto.getItemCategoryDto(),category);
        category.setCompany(itemFormDto.getCompany());
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
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, ItemCategoryDto itemCategoryDto, Pageable pageable) {
        return itemRepository.getMainItemPage(itemSearchDto, itemCategoryDto, pageable);
    }

    @Override
    public Page<MainItemDto> getMainItemPage2(String company, ItemSearchDto itemSearchDto, ItemCategoryDto itemCategoryDto,Pageable pageable) {
        return itemRepository.getMainItemPage2(company, itemSearchDto, itemCategoryDto, pageable);
    }

    // 1024추가
    @Transactional
    @Override
    public int saveCodes(Long itemId, List<String> codes) {
        Item item = itemRepository.findById(itemId).get();
        int beforeStock=item.getStockNumber();
        List<ItemCode> itemCodes = new ArrayList<>();

        codes.forEach(code -> {
            ItemCode itemCode = new ItemCode();
            itemCode.setItem(item);
            itemCode.setCodNum(code);
            itemCodes.add(itemCode);
        });

        itemCodeRepository.saveAll(itemCodes);
        item.setStockNumber(item.getStockNumber()+codes.size());
        item.setItemSellStatus(ItemSellStatus.SELL);
        itemRepository.save(item);

        if(beforeStock==0){
            List<Object> getList=memberRepository.findEmailFromItemId(item.getId());
            if(getList!=null){
                List<String> emailList = getList.stream().map(obj -> (String) obj).toList();
                String subject="[놀이마당] "+item.getItemNm()+" 입고되었습니다.";
                Context context=new Context();
                context.setVariable("itemId", item.getId());
                context.setVariable("itemNm",item.getItemNm());
                context.setVariable("count",codes.size());
                context.setVariable("itemImgName",itemImgRepository.findByItemId(itemId).get(0).getImgName());

                emailService.sendEmailToMany(emailList, subject, "mailForm/stockNotification",context);
            }
        }
        return codes.size(); // 반복한 횟수 반환
    }


    @Override
    public List<MainItemDto> getMainItem(String company, ItemCategoryDto itemCategoryDto) {
        return itemRepository.getMainItem(company, itemCategoryDto); // Repository 메서드 호출
    }
}
