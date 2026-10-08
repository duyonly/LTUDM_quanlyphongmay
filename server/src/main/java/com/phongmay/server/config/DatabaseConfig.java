package com.phongmay.server.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String URL ="jdbc:postgresql://localhost:5432/quanlyphongmay";
    private static  final String USER="postgres";
    private  static  final String PASSWORD="123";
    public  static  Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL,USER,PASSWORD);
    }
    public static  void testConnection(){
        try(Connection connection=getConnection()) {
            
            System.out.println("=================================");
            System.out.println("Kết nối PostgreSQL thành công!");
            System.out.println("Database: " + connection.getCatalog());
            System.out.println("=================================");
        } catch (Exception e) {
            System.out.println("Kết nối PostgreSQL thất bại!");
            e.printStackTrace();
        }
    }
}
