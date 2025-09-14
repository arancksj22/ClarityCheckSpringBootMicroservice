package com.claritycheck.Backend.service;

import com.claritycheck.Backend.repository.DBRepository;

public class DBService {
    DBRepository repo;

    DBService(DBRepository repo){
        this.repo = repo;
    }
}
