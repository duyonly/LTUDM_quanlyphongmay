package com.phongmay.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.phongmay.server.config.DatabaseConfig;

public class UserDao {
    public boolean checkLogin(String username,String password){
        String sql="SELECT 1 FROM users where LOWER(username)= LOWER(?) and password=?";
        try(Connection conn=DatabaseConfig.getConnection();
          PreparedStatement ps=conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try(ResultSet rs=ps.executeQuery()){
                return  rs.next();
            }
        } catch (Exception e) {
           e.printStackTrace();
           return false;
        }
    }
}
