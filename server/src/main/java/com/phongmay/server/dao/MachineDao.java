package com.phongmay.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.phongmay.server.config.DatabaseConfig;

public class MachineDao {
    //kết nối và thêm máy vào database
    public boolean connectMachine(String machineCode,String hostname,String ipAddress,String os){
        String sql="""
                INSERT INTO machines (machine_code,hostname,ip_address,os,status, last_seen)
                VALUES (?,?,?,?,'ONLINE',CURRENT_TIMESTAMP)
                 ON CONFLICT (machine_code)
            DO UPDATE SET
                hostname = EXCLUDED.hostname,
                ip_address = EXCLUDED.ip_address,
                os = EXCLUDED.os,
                status = 'ONLINE',
                last_seen = CURRENT_TIMESTAMP
                """;
        try(Connection connection= DatabaseConfig.getConnection();
            PreparedStatement statement=connection.prepareStatement(sql))
         {
            statement.setString(1, machineCode);
            statement.setString(2, hostname);
            statement.setString(3, ipAddress);
            statement.setString(4, os);
            statement.executeUpdate();
            System.out.println("Đã thêm máy " + machineCode + " vào database");
            return true;
        } catch (Exception e) {
          System.out.println("không thể thêm máy "+machineCode+" được");
          e.printStackTrace();
          return false;
        }
    }
    public boolean updateHeartbeat(String machineCode){
        String sql="""
                UPDATE machines SET status ='ONLINE', last_seen=CURRENT_TIMESTAMP
                WHERE machine_code=?
                """;
                try(Connection connection=DatabaseConfig.getConnection();
                    PreparedStatement statement=connection.prepareStatement(sql)
                ) {
                    statement.setString(1, machineCode);
                    int rows=statement.executeUpdate();
                    return rows>0;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
    }
    public boolean disconnectMachine(String machineCode){
        String sql="""
                UPDATE machines SET status='OFFLINE'
                WHERE machine_code=?
                """;
            try(Connection connection=DatabaseConfig.getConnection();
              PreparedStatement statement=connection.prepareStatement(sql)) {
                statement.setString(1, machineCode);
                int rows=statement.executeUpdate();
                return rows>0;
            } catch (Exception e) {
                e.printStackTrace();
                    return false;
            }
    }
}
