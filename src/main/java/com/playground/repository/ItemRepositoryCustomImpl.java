package com.playground.repository;

import com.playground.dto.ItemCategoryDto;
import com.playground.entity.QItemCategory;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.playground.constant.ItemSellStatus;
import com.playground.dto.ItemSearchDto;
import com.playground.dto.MainItemDto;
import com.playground.dto.QMainItemDto;
import com.playground.entity.Item;
import com.playground.entity.QItem;
import com.playground.entity.QItemImg;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemRepositoryCustomImpl implements ItemRepositoryCustom{

    private JPAQueryFactory queryFactory;

    public ItemRepositoryCustomImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus){
        if (searchSellStatus == null) {
            return null;
        }
        // searchSellStatus 값에 따라 조건을 설정
        if (searchSellStatus == ItemSellStatus.SOLD_OUT) {
            return QItem.item.stockNumber.eq(0); // 품절 상태 + stockNumber가 0
        } else if (searchSellStatus == ItemSellStatus.SELL) {
            return QItem.item.stockNumber.gt(0); // 재고 있음 + stockNumber가 0보다 큼
        } else {
            return null; //기본 값
        }
    }

    private BooleanExpression regDtsAfter(String searchDateType){

        LocalDateTime dateTime = LocalDateTime.now();

        if(StringUtils.equals("all", searchDateType) || searchDateType == null){
            return null;
        } else if(StringUtils.equals("1d", searchDateType)){
            dateTime = dateTime.minusDays(1);
        } else if(StringUtils.equals("1w", searchDateType)){
            dateTime = dateTime.minusWeeks(1);
        } else if(StringUtils.equals("1m", searchDateType)){
            dateTime = dateTime.minusMonths(1);
        } else if(StringUtils.equals("6m", searchDateType)){
            dateTime = dateTime.minusMonths(6);
        }

        return QItem.item.regTime.after(dateTime);
    }

    private BooleanExpression searchByLike(String searchBy, String searchQuery){

        if(StringUtils.equals("itemNm", searchBy)){
            return QItem.item.itemNm.like("%" + searchQuery + "%");
        } else if(StringUtils.equals("createdBy", searchBy)){
            return QItem.item.createdBy.like("%" + searchQuery + "%");
        }

        return null;
    }

    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable) {

        List<Item> content = queryFactory
                .selectFrom(QItem.item)
                .where(regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(),
                                itemSearchDto.getSearchQuery()))
                .orderBy(QItem.item.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.select(Wildcard.count).from(QItem.item)
                .where(regDtsAfter(itemSearchDto.getSearchDateType()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(), itemSearchDto.getSearchQuery()))
                .fetchOne()
                ;

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression itemNmLike(String searchQuery){
        return StringUtils.isEmpty(searchQuery) ? null : QItem.item.itemNm.like("%" + searchQuery + "%");
    }

    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, ItemCategoryDto itemCategoryDto,Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;
        QItemCategory itemCategory= QItemCategory.itemCategory;

        BooleanBuilder whereClause = new BooleanBuilder();

        // 기본 조건 추가
        whereClause.and(itemImg.repimgYn.eq("Y"));
        whereClause.and(itemNmLike(itemSearchDto.getSearchQuery()));

        //정렬 조건 추가
        List<OrderSpecifier<?>> orderClauses = new ArrayList<>();
        if(itemSearchDto.getOrderBy()!=null){
            if (itemSearchDto.getOrderBy().equals("price_high")) {
                orderClauses.add(item.price.desc());
            } else if (itemSearchDto.getOrderBy().equals("price_low")) {
                orderClauses.add(item.price.asc());
            } else if (itemSearchDto.getOrderBy().equals("sales")){
                orderClauses.add(item.buyCnt.desc());
            }
        }
        orderClauses.add(item.openDate.desc());

        // ItemCategoryDto의 필드 추가
        if (itemCategoryDto != null) {
            if (itemCategoryDto.isAction()) whereClause.and(itemCategory.action.eq(true));
            if (itemCategoryDto.isAdventure()) whereClause.and(itemCategory.adventure.eq(true));
            if (itemCategoryDto.isRpg()) whereClause.and(itemCategory.rpg.eq(true));
            if (itemCategoryDto.isShooter()) whereClause.and(itemCategory.shooter.eq(true));
            if (itemCategoryDto.isStrategy()) whereClause.and(itemCategory.strategy.eq(true));
            if (itemCategoryDto.isSimulation()) whereClause.and(itemCategory.simulation.eq(true));
            if (itemCategoryDto.isPuzzle()) whereClause.and(itemCategory.puzzle.eq(true));
            if (itemCategoryDto.isSports()) whereClause.and(itemCategory.sports.eq(true));
            if (itemCategoryDto.isRacing()) whereClause.and(itemCategory.racing.eq(true));
            if (itemCategoryDto.isFighting()) whereClause.and(itemCategory.fighting.eq(true));
            if (itemCategoryDto.isSurvival()) whereClause.and(itemCategory.survival.eq(true));
            if (itemCategoryDto.isRhythm()) whereClause.and(itemCategory.rhythm.eq(true));
            if (itemCategoryDto.isSandbox()) whereClause.and(itemCategory.sandbox.eq(true));
            if (itemCategoryDto.isBattleRoyale()) whereClause.and(itemCategory.battleRoyale.eq(true));
            if (itemCategoryDto.isCard()) whereClause.and(itemCategory.card.eq(true));
            if (itemCategoryDto.isBoardGame()) whereClause.and(itemCategory.boardGame.eq(true));
            if (itemCategoryDto.isHorror()) whereClause.and(itemCategory.horror.eq(true));
            if (itemCategoryDto.isPlatformer()) whereClause.and(itemCategory.platformer.eq(true));
            if (itemCategoryDto.isMoba()) whereClause.and(itemCategory.moba.eq(true));
            if (itemCategoryDto.isMmorpg()) whereClause.and(itemCategory.mmorpg.eq(true));
        }
        List<MainItemDto> content = queryFactory
                .select(
                        new QMainItemDto(
                                item.id,
                                item.itemNm,
                                itemImg.imgUrl,
                                item.price,
                                item.stockNumber,
                                item.buyCnt,
                                item.openDate)
                )
                .from(itemImg)
                .join(itemImg.item, item)
                .join(itemCategory).on(itemCategory.item.eq(item))
                .where(whereClause)
                .orderBy(orderClauses.toArray(new OrderSpecifier[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(Wildcard.count)
                .from(itemImg)
                .join(itemImg.item, item)
                .join(itemCategory).on(itemCategory.item.eq(item))
                .where(whereClause)
                .fetchOne()
                ;

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MainItemDto> getMainItemPage2(String company, ItemSearchDto itemSearchDto, ItemCategoryDto itemCategoryDto,Pageable pageable) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;
        QItemCategory itemCategory= QItemCategory.itemCategory;

        BooleanBuilder whereClause = new BooleanBuilder();

        // 기본 조건 추가
        whereClause.and(itemImg.repimgYn.eq("Y"));
        whereClause.and(itemCategory.company.eq(company));
        whereClause.and(itemNmLike(itemSearchDto.getSearchQuery()));

        //정렬 조건 추가
        List<OrderSpecifier<?>> orderClauses = new ArrayList<>();
        if(itemSearchDto.getOrderBy()!=null){
            if (itemSearchDto.getOrderBy().equals("price_high")) {
                orderClauses.add(item.price.desc());
            } else if (itemSearchDto.getOrderBy().equals("price_low")) {
                orderClauses.add(item.price.asc());
            } else if (itemSearchDto.getOrderBy().equals("sales")){
                orderClauses.add(item.buyCnt.desc());
            }
        }
        orderClauses.add(item.openDate.desc());

        // ItemCategoryDto의 필드 추가
        if (itemCategoryDto != null) {
            if (itemCategoryDto.isAction()) whereClause.and(itemCategory.action.eq(true));
            if (itemCategoryDto.isAdventure()) whereClause.and(itemCategory.adventure.eq(true));
            if (itemCategoryDto.isRpg()) whereClause.and(itemCategory.rpg.eq(true));
            if (itemCategoryDto.isShooter()) whereClause.and(itemCategory.shooter.eq(true));
            if (itemCategoryDto.isStrategy()) whereClause.and(itemCategory.strategy.eq(true));
            if (itemCategoryDto.isSimulation()) whereClause.and(itemCategory.simulation.eq(true));
            if (itemCategoryDto.isPuzzle()) whereClause.and(itemCategory.puzzle.eq(true));
            if (itemCategoryDto.isSports()) whereClause.and(itemCategory.sports.eq(true));
            if (itemCategoryDto.isRacing()) whereClause.and(itemCategory.racing.eq(true));
            if (itemCategoryDto.isFighting()) whereClause.and(itemCategory.fighting.eq(true));
            if (itemCategoryDto.isSurvival()) whereClause.and(itemCategory.survival.eq(true));
            if (itemCategoryDto.isRhythm()) whereClause.and(itemCategory.rhythm.eq(true));
            if (itemCategoryDto.isSandbox()) whereClause.and(itemCategory.sandbox.eq(true));
            if (itemCategoryDto.isBattleRoyale()) whereClause.and(itemCategory.battleRoyale.eq(true));
            if (itemCategoryDto.isCard()) whereClause.and(itemCategory.card.eq(true));
            if (itemCategoryDto.isBoardGame()) whereClause.and(itemCategory.boardGame.eq(true));
            if (itemCategoryDto.isHorror()) whereClause.and(itemCategory.horror.eq(true));
            if (itemCategoryDto.isPlatformer()) whereClause.and(itemCategory.platformer.eq(true));
            if (itemCategoryDto.isMoba()) whereClause.and(itemCategory.moba.eq(true));
            if (itemCategoryDto.isMmorpg()) whereClause.and(itemCategory.mmorpg.eq(true));
        }

        List<MainItemDto> content = queryFactory
                .select(
                        new QMainItemDto(
                                item.id,
                                item.itemNm,
                                itemImg.imgUrl,
                                item.price,
                                item.stockNumber,
                                item.buyCnt,
                                item.openDate)
                )
                .from(itemImg)
                .join(itemImg.item, item)
                .join(itemCategory).on(itemCategory.item.eq(item))
                .where(whereClause)
                .orderBy(orderClauses.toArray(new OrderSpecifier[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(Wildcard.count)
                .from(itemImg)
                .join(itemImg.item, item)
                .join(itemCategory).on(itemCategory.item.eq(item))
                .where(whereClause)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<MainItemDto> getMainItem(String company, ItemCategoryDto itemCategoryDto) {
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;
        QItemCategory itemCategory= QItemCategory.itemCategory;

        BooleanBuilder whereClause = new BooleanBuilder();
           JPAQuery<Item> query = queryFactory.selectFrom(item);

       // 기본 조건 추가
       whereClause.and(itemImg.repimgYn.eq("Y")); // 대표 이미지 조건

       if (company != null) {
        whereClause.and(itemCategory.company.eq(company));
        }
    
        List<MainItemDto> content = queryFactory
                .select(
                        new QMainItemDto(
                                item.id,
                                item.itemNm,
                                itemImg.imgUrl,
                                item.price,
                                item.stockNumber,
                                item.buyCnt,
                                item.openDate)
                )
                .from(itemImg)
                .join(itemImg.item, item)
                .join(itemCategory).on(itemCategory.item.eq(item))
                .where(whereClause)
                .orderBy(Expressions.numberTemplate(Double.class, "rand()").asc())
                .limit(8)
                .fetch();
    
        long total = queryFactory
                .select(Wildcard.count)
                .from(itemImg)
                .join(itemImg.item, item)
                .join(itemCategory).on(itemCategory.item.eq(item))
                .where(whereClause)
                .fetchOne();
    
        return content;
    }

}