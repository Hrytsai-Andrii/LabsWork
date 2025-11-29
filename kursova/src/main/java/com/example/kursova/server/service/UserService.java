package com.example.kursova.server.service;

import com.example.kursova.common.model.User;
import com.example.kursova.server.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // --- ВИПРАВЛЕННЯ: Метод авторизації ---
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);

        // Перевіряємо, чи існує юзер і чи співпадають ХЕШІ паролів
        if (user != null && user.getPassword().equals(hashPassword(password))) {
            return user;
        }
        return null;
    }

    // --- ВИПРАВЛЕННЯ: Метод реєстрації ---
    public User register(String name, String email, String password) {
        if (userRepository.findByEmail(email) != null) return null;

        // Хешуємо пароль перед створенням об'єкта
        String hashedPassword = hashPassword(password);

        // Створюємо юзера вже з захешованим паролем
        User newUser = new User(0, name, email, hashedPassword);

        int id = userRepository.createUser(newUser);
        if (id != -1) {
            newUser.setUserID(id);
            return newUser;
        }
        return null;
    }

    // Інші методи залишаються без змін...

    public User registerUser(User user) {
        // Якщо цей метод використовується для оновлення/реєстрації готового об'єкта,
        // переконайтеся, що пароль там вже захешований, або додайте логіку тут.
        userRepository.update(user);
        return user;
    }

    public void updateUser(User user) {
        // Примітка: Якщо користувач змінює пароль, його теж треба хешувати перед викликом цього методу
        userRepository.update(user);
    }

    public void deleteUser(int id) {
        userRepository.delete(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByID(int id) {
        return userRepository.findByID(id);
    }

    // --- НОВИЙ МЕТОД: Хешування (SHA-256) ---
    private String hashPassword(String plainTextPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(plainTextPassword.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Помилка хешування пароля", e);
        }
    }

    // Допоміжний метод для перетворення байтів у читабельний рядок (Hex)
    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}