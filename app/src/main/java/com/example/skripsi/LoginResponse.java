package com.example.skripsi;

public class LoginResponse {
    private String message;
    private String token;
    private User user;

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public static class User {
        private String id;
        private String nama;
        private String email;
        private String role;

        public String getId() {
            return id;
        }

        public String getNama() {
            return nama;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }
    }
}
