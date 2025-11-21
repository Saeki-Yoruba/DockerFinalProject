package com.supernovapos.finalproject.store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supernovapos.finalproject.store.model.Store;

public interface StoreRepository extends JpaRepository<Store, Integer> {

	Optional<Store> findTopByOrderByIdAsc();
}
