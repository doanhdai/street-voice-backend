package com.foodstreet.voice.repository;

import com.foodstreet.voice.entity.FoodStallUpdate;
import com.foodstreet.voice.entity.FoodStallUpdateStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodStallUpdateRepository extends JpaRepository<FoodStallUpdate, Long> {
    List<FoodStallUpdate> findByStatusOrderByCreatedAtDesc(FoodStallUpdateStatus status);

    List<FoodStallUpdate> findByStatusInOrderByCreatedAtDesc(Collection<FoodStallUpdateStatus> statuses);

    boolean existsByFoodStall_IdAndStatus(Long foodStallId, FoodStallUpdateStatus status);

    boolean existsByFoodStall_IdAndStatusAndCreatedAtBefore(
            Long foodStallId,
            FoodStallUpdateStatus status,
            LocalDateTime createdAt
    );
}
