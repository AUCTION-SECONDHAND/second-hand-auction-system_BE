package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Item;
import com.second_hand_auction_system.models.MainCategory;
import com.second_hand_auction_system.models.SubCategory;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.utils.AuctionStatus;
import com.second_hand_auction_system.utils.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    Page<Item> findAllByItemStatusOrderByItemIdDesc(ItemStatus itemStatus, Pageable pageable);

    @Query("SELECT i FROM Item i " +
            "JOIN i.auction a " +
            "JOIN Bid b ON a.auctionId = b.auction.auctionId " +
            "JOIN b.user u " +
            "WHERE u.id = :userId " +
            "AND b.winBid = true " + "AND a.status = 'AWAITING_PAYMENT'"  )
    Page<Item> findItemsByUserId(@Param("userId") Integer userId, Pageable pageable);





    //Page<Item> findAllBySubCategory_MainCategory_mainCategoryId (Integer mainCategoryId, Pageable pageable);

    Page<Item> findAllBySubCategory_SubCategoryIdAndAuction_Status (Integer mainCategoryId,AuctionStatus auctionStatus, Pageable pageable);

    @Query("SELECT i FROM Item i " +
            "JOIN i.auction a " +
            "JOIN i.subCategory sc " +
            "WHERE (:itemName IS NULL OR :itemName = '' OR i.itemName LIKE %:itemName%) " +
            "AND (:minPrice IS NULL OR a.startPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR a.startPrice <= :maxPrice) " +
            "AND (:subCategoryIds IS NULL OR sc.subCategoryId IN :subCategoryIds) " +
            "AND a.status IN (:statuses)")
    Page<Item> searchItems(
            @Param("itemName") String itemName,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("subCategoryIds") List<Integer> subCategoryIds,
            @Param("statuses") List<AuctionStatus> statuses,
            Pageable pageable
    );



    @Query("SELECT i FROM Item i ORDER BY i.itemId ASC ")
    List<Item> findTop10Items(Pageable pageable);

    Page<Item> findItemByUser_Id(Integer user_id, Pageable pageable);
    Item findByAuction_AuctionId(Integer auction);

    Page<Item> findAllByUserIdAndAuctionStatus(Integer user_id, AuctionStatus auctionStatus, Pageable pageable);

    @Query("SELECT i, COUNT(b) AS bidCount " +
            "FROM Bid b " +
            "JOIN b.auction.item i " +
            "GROUP BY i.itemId " +
            "ORDER BY bidCount DESC")
    List<Item> findTop10ItemsWithMostBids(Pageable pageable);

    @Query("SELECT i.user FROM Item i WHERE i.itemId = :itemId")
    Optional<User> findUserByItemId(@Param("itemId") Integer itemId);


    @Query("SELECT i FROM Item i " +
            "JOIN i.auction a " +
            "JOIN i.subCategory sc " +
            "WHERE i.user.id = :userId " +
            "AND (:itemName IS NULL OR :itemName = '' OR i.itemName LIKE %:itemName%) " +
            "AND (:minPrice IS NULL OR a.startPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR a.startPrice <= :maxPrice) " +
            "AND (:subCategoryIds IS NULL OR sc.subCategoryId IN :subCategoryIds)")
    Page<Item> searchItemsByUserId(
            @Param("userId") Long userId, // userId parameter
            @Param("itemName") String itemName,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("subCategoryIds") List<Integer> subCategoryIds,
            Pageable pageable
    );


}
