package com.app.Kaylia.repository;

import com.app.Kaylia.model.ConfirmOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConfirmOrderRepo extends JpaRepository<ConfirmOrder, Integer> {

    List<ConfirmOrder> findByUserId(int userId);
}
