package com.supernovapos.finalproject.analytics.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.supernovapos.finalproject.analytics.model.entity.GroupOrdersView;

@Repository
public interface GroupOrdersRepository extends 
JpaRepository<GroupOrdersView, UUID>, 
JpaSpecificationExecutor<GroupOrdersView> {

}
