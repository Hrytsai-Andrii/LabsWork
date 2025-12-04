package com.example.labs.server.repository;

import java.util.List;

public interface IRepository<T> {
    T findByID(int id);
    List<T> findAll();
    void update(T entity);
    void delete(int id);
}