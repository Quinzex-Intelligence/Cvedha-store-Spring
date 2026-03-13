package com.quinzex.repository;

import com.quinzex.entity.Ebooks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EbookRepo extends JpaRepository<Ebooks,Long> {
    List<Ebooks> findTop10ByActiveTrueOrderByBookIdAsc();

    List<Ebooks> findTop10ByActiveFalseOrderByBookIdAsc();

    // Next pages (cursor based)
    List<Ebooks> findTop10ByActiveTrueAndBookIdGreaterThanOrderByBookIdAsc(Long bookId);

    List<Ebooks> findTop10ByActiveFalseAndBookIdGreaterThanOrderByBookIdAsc(Long bookId);

    @Modifying
    @Query("UPDATE Ebooks e SET e.active = false WHERE e.bookId IN :ids")
    int deactivateBooks(@Param("ids") List<Long> ids);

    @Modifying
    @Query("UPDATE Ebooks e SET e.active = true WHERE e.bookId IN :ids")
    int activateBooks(@Param("ids") List<Long> ids);


    @Modifying
    @Query("""
UPDATE Ebooks e
SET e.availableQuantity = e.availableQuantity - :qty,
    e.reservedQuantity = e.reservedQuantity + :qty
WHERE e.bookId = :bookId AND e.availableQuantity >= :qty
""")
    int reserveStock(@Param("bookId") Long bookId, @Param("qty") int qty);

    @Modifying
    @Query("""
UPDATE Ebooks e
SET e.availableQuantity = e.availableQuantity + :qty,
    e.reservedQuantity = e.reservedQuantity - :qty
WHERE e.bookId = :bookId AND e.reservedQuantity >= :qty
""")
    int releaseStock(@Param("bookId") Long bookId, @Param("qty") int qty);

    @Query("""
SELECT e FROM Ebooks e
WHERE e.active = true
AND (:cursor IS NULL OR e.bookId > :cursor)
AND (
LOWER(e.bookName) LIKE LOWER(CONCAT('%', :keyword, '%'))
OR LOWER(e.bookAuthor) LIKE LOWER(CONCAT('%', :keyword, '%'))
)
ORDER BY e.bookId ASC
""")
    List<Ebooks> searchActiveBooksWithCursor(
            @Param("keyword") String keyword,
            @Param("cursor") Long cursor
    );

    @Query(value = """
SELECT * FROM ebooks
WHERE active = true
AND language_category = :language
AND (:cursor IS NULL OR book_id > :cursor)
ORDER BY book_id ASC
LIMIT 10
""", nativeQuery = true)
    List<Ebooks> findBooksByLanguageWithCursor(
            @Param("language") String language,
            @Param("cursor") Long cursor
    );

    @Query(value = """
SELECT * FROM ebooks
WHERE active = true
AND book_category = :category
AND (:cursor IS NULL OR book_id > :cursor)
ORDER BY book_id ASC
LIMIT 10
""", nativeQuery = true)
    List<Ebooks> findBooksByCategoryWithCursor(
            @Param("category") String category,
            @Param("cursor") Long cursor
    );
}
