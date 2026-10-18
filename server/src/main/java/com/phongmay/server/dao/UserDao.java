package com.phongmay.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


import com.phongmay.server.config.DatabaseConfig;

public class UserDao {
    public boolean  checkLogin(String username,String password){
        String sql="SELECT password_hash,role FROM users where LOWER(username)= LOWER(?)";
        try(Connection conn=DatabaseConfig.getConnection();
          PreparedStatement ps=conn.prepareStatement(sql)) {
            ps.setString(1, username);
            
            try(ResultSet rs=ps.executeQuery()){
                if (!rs.next()) {
					System.out.println("LOGIN: không tìm thấy user '" + username + "'");	
                    return false;
                }
                String hash=rs.getString("password_hash");
				if(!org.mindrot.jbcrypt.BCrypt.checkpw(password, hash)) return false;
				System.out.println("LOGIN: hash length=" + (hash == null ? "null" : hash.length())
                    + ", prefix=" + (hash == null ? "" : hash.substring(0, Math.min(7, hash.length()))));
                return true;
            }
        } catch (Exception e) {
           e.printStackTrace();
           return false;
        }
    }
}
