package com.example.uploadingfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.uploadingfiles.dao.RecordsRepository;

import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:3000")



@RestController
public class RecordsController {

    @Autowired
    RecordsRepository recordsRepo;


    @GetMapping("/records")
    public List<Records> listofRecords(){
        return recordsRepo.findAll();
        
    }
}

