package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.utils.AuctionStatus;
import com.second_hand_auction_system.utils.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {
    Page<Item> findAllByItemStatus(ItemStatus itemStatus, Pageable pageable);

    Page<Item> findAllByAuction_StatusAndUserId(AuctionStatus auction_status, Integer user_id, Pageable pageable);

    //Page<Item> findAllByAuction_Status
    @Query("SELECT i FROM Item i " +
            "JOIN i.auction a " +
            "JOIN i.subCategory sc " +
            "WHERE (:itemName IS NULL OR :itemName = '' OR i.itemName LIKE %:itemName%) " +
            "AND (:minPrice IS NULL OR a.startPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR a.startPrice <= :maxPrice) " +
            "AND (:subCategoryIds IS NULL OR sc.subCategoryId IN :subCategoryIds)")
    Page<Item> searchItems(
            @Param("itemName") String itemName,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("subCategoryIds") List<Integer> subCategoryIds,
            Pageable pageable
    );

    @Query("SELECT i FROM Item i ORDER BY i.itemId ASC ")
    List<Item> findTop10Items(Pageable pageable);

    Page<Item> findItemByUser_Id(Integer user_id, Pageable pageable);
    Item findByAuction_AuctionId(Integer auction);

    Page<Item> findAllByUserIdAndAuctionStatus(Integer user_id, AuctionStatus auctionStatus, Pageable pageable);
}
