package com.foodstreet.voice.repository;

import com.foodstreet.voice.entity.FoodStall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodStallRepository extends JpaRepository<FoodStall, Long> {

    @Query(value = """
            SELECT * FROM food_stalls
            ORDER BY ST_Distance(
                location,
                ST_GeogFromText('POINT(' || :longitude || ' ' || :latitude || ')')
            )
            LIMIT 1
            """, nativeQuery = true)
    Optional<FoodStall> findNearestStall(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude);
}