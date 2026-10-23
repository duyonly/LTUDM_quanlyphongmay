package com.phongmay.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


import com.phongmay.server.config.DatabaseConfig;

public class UserDao {
   public boolean checkLogin(String username,String password){
    String sql="""
            SELECT password_hash FROM users WHERE LOWER(username)=LOWER(?)
            """;
            try (Connection con=DatabaseConfig.getConnection();
        PreparedStatement ps=con.prepareStatement(sql)){
                ps.setString(1, username);
               try(ResultSet rs=ps.executeQuery()){
                if(!rs.next()) return false;
                String hash=rs.getString("password_hash");
                if(!org.mindrot.jbcrypt.BCrypt.checkpw(password, hash)) return false;
               }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
            return true;
   }
//    public boolean insertUser(String username,String password,String role){
//     String sql="""
//      insert into users (username,password_hash,role,created_at)
//      VALUE(?,?,?,CURRENT_TIMESTAMP)
//     """;
//     try (Connection con=DatabaseConfig.getConnection();
//      PreparedStatement ps=con.prepareStatement(sql)){
//         ps.setString(1, username);
//         ps.setString(2, password);
//         ps.setString(3, role);
//         ps.executeQuery();
//         System.out.println("đã thêm user "+username);
//         return true;
//     } catch (Exception e) {
//         e.printStackTrace();
//         return false;
//     }
//    }
//    public boolean updateUser(String username,String password,String role){
//     String sql="""
//      UPDATE  users SET username=?,password=?,role=?
//      where id=?
//     """;
//     try (Connection con=DatabaseConfig.getConnection();
//      PreparedStatement ps=con.prepareStatement(sql)){
//         ps.setString(1, );
//         ps.setString(2, password);
//         ps.setString(3, role);
//         ps.executeQuery();
//         System.out.println("đã thêm user "+username);
//         return true;
//     } catch (Exception e) {
//         e.printStackTrace();
//         return false;
//     }
//    }

}
