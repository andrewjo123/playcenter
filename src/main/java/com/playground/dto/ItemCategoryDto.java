package com.playground.dto;

import com.playground.entity.Item;
import com.playground.entity.ItemCategory;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

@Getter
@Setter
public class ItemCategoryDto {
    public static ModelMapper modelMapper = new ModelMapper();

    static {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    private boolean action;
    private boolean adventure;
    private boolean rpg;
    private boolean shooter;
    private boolean strategy;
    private boolean simulation;
    private boolean puzzle;
    private boolean sports;
    private boolean racing;
    private boolean fighting;
    private boolean survival;
    private boolean rhythm;
    private boolean sandbox;
    private boolean battleRoyale;
    private boolean card;
    private boolean boardGame;
    private boolean horror;
    private boolean platformer;
    private boolean moba;
    private boolean mmorpg;

    public ItemCategory toEntity(Item item, String company) {
        ItemCategory category = modelMapper.map(this, ItemCategory.class);
        category.setItem(item);
        category.setCompany(company);
        return category;
    }

    public static ItemCategoryDto fromEntity(ItemCategory itemCategory) {
        return modelMapper.map(itemCategory, ItemCategoryDto.class);
    }
}
