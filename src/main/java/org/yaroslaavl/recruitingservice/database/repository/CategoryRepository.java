package org.yaroslaavl.recruitingservice.database.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.yaroslaavl.recruitingservice.database.entity.Category;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("""
           SELECT c FROM Category c
           WHERE (:searchName IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :searchName, '%')))
           ORDER BY c.name ASC
           """)
    List<Category> findFilteredCategories(String searchName);
}
