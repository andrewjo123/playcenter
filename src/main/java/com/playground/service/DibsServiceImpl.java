package com.playground.service;

import com.playground.dto.DibsDto;
import com.playground.entity.*;
import com.playground.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DibsServiceImpl implements DibsService{
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final DibsRepository dibsRepository;
    private final DibsItemRepository dibsItemRepository;

    @Override
    public Long addDibs(DibsDto dibsDto, String email) {
        Item item = itemRepository.findById(dibsDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);
        Member member = memberRepository.findByEmail(email);

        Dibs dibs = dibsRepository.findByMemberId(member.getId());
        if (dibs == null) {
            dibs = Dibs.createDibs(member);
            dibsRepository.save(dibs);
        }

        DibsItem savedDibsItem = dibsItemRepository.findByDibsIdAndItemId(dibs.getId(), item.getId());

        if (savedDibsItem != null) {
            return savedDibsItem.getId();
        } else {
            DibsItem dibsItem = DibsItem.createDibsItem(dibs, item);
            dibsItemRepository.save(dibsItem);
            return dibsItem.getId();
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<DibsDto> getDibsList(String email) {
        List<DibsDto> dibsDtoList = new ArrayList<>();

        Member member = memberRepository.findByEmail(email);
        Dibs dibs = dibsRepository.findByMemberId(member.getId());
        if (dibs == null) {
            return dibsDtoList;
        }

        dibsDtoList = dibsItemRepository.findDibsDtoList(dibs.getId());
        return dibsDtoList;
    }

    @Override
    public Long deleteDibsItem(Long itemId, String email) {
        Dibs dibs=dibsRepository.findByEmail(email);
        DibsItem dibsItem = dibsItemRepository.findByDibsIdAndItemId(dibs.getId(),itemId);
        Long dibsItemId=dibsItem.getId();
        dibsItemRepository.delete(dibsItem);
        return dibsItemId;
    }

    public Long deleteDibsItemFromId(Long dibsItemId){
        DibsItem dibsItem=dibsItemRepository.findById(dibsItemId).orElseThrow(EntityNotFoundException::new);
        dibsItemRepository.delete(dibsItem);
        return dibsItemId;
    }

    @Override
    public int getDibsCount(String email) {
        Long dibsId=dibsRepository.findByEmail(email).getId();
        return (int)dibsItemRepository.countByDibsId(dibsId);
    }

    @Override
    public boolean checkDibs(String email, Long itemId) {
        boolean checked=false;
        Dibs dibs=dibsRepository.findByEmail(email);
        DibsItem dibsItem = dibsItemRepository.findByDibsIdAndItemId(dibs.getId(),itemId);
        if(dibsItem!=null){
            checked=true;
        }
        return checked;
    }

    @Override
    public List<Long> dibsListforItemId(String email) {
        List<Object> itemIds=dibsItemRepository.getItemIDofDibs(email);
        // 각 Object를 Long으로 변환
        return itemIds.stream()
                .map(obj -> (Long) obj) // 각 Object를 Long으로 변환
                .collect(Collectors.toList());
    }

    @Override
    public Long getDibsItem(Long dibsItemId) {
        DibsItem dibsItem=dibsItemRepository.findById(dibsItemId).orElseThrow(EntityNotFoundException::new);
        return dibsItem.getItem().getId();
    }
}
