package com.supernovapos.finalproject.analytics.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.supernovapos.finalproject.analytics.model.entity.DailySalesView;

@Repository
public interface DailySalesRepository extends 
JpaRepository<DailySalesView, LocalDate>, 
JpaSpecificationExecutor<DailySalesView> {

}
