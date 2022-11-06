package com.example.uploadingfiles.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.uploadingfiles.Records;

@Repository
public interface RecordsRepository extends JpaRepository<Records,Long>{
    
}
