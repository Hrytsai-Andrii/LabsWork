package com.example.kursova.server.repository;

import java.util.List;

public interface IRepository<T> {
    T findByID(int id);
    List<T> findAll();
    void update(T entity); // Виконує роль Save/Update
    void delete(int id);
}