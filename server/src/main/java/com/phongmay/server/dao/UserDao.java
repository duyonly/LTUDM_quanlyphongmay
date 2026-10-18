package com.phongmay.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


import com.phongmay.server.config.DatabaseConfig;

public class UserDao {
    public boolean checkLogin(String username,String password){
        String sql="SELECT password_hash FROM users where LOWER(username)= LOWER(?)";
        try(Connection conn=DatabaseConfig.getConnection();
          PreparedStatement ps=conn.prepareStatement(sql)) {
            ps.setString(1, username);
            
            try(ResultSet rs=ps.executeQuery()){
                if (!rs.next()) {
                    return false;
                }
                String hash=rs.getString("password_hash");
                boolean ok = org.mindrot.jbcrypt.BCrypt.checkpw(password, hash);
    System.out.println("DEBUG: hash=" + hash + " ok=" + ok);
    return ok;
            }
        } catch (Exception e) {
           e.printStackTrace();
           return false;
        }
    }
}
