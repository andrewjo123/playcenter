package com.playground.service;

import com.playground.dto.DibsDto;

import java.util.List;

public interface DibsService {

    Long addDibs(DibsDto dibsDto, String email);

    List<DibsDto> getDibsList(String email);

    Long deleteDibsItem(Long itemId, String email);

    Long deleteDibsItemFromId(Long dibsItemId);

    int getDibsCount(String email);

    boolean checkDibs(String email, Long itemId);

    List<Long> dibsListforItemId(String email);

    Long getDibsItem(Long dibsItemId);
}
