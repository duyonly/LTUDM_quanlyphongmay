package com.phongmay.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import com.phongmay.common.MachineStatus;
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
    public int chuyenTrangThai(int timeout){
        String sql="""
              UPDATE machines SET status='OFFLINE' WHERE status='ONLINE' AND last_seen<?
                """;
        try(Connection con=DatabaseConfig.getConnection();
    PreparedStatement ps=con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.
                valueOf(LocalDateTime.now()
                .minusSeconds(timeout)));
            return ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        return 0;
        }
    }
    public List<MachineStatus> getAllMachines(){
        List<MachineStatus> list=new ArrayList<>();
        String sql="""
            SELECT machine_code,hostname,ip_address,os,status FROM machines ORDER BY machine_code
                """;
                try (Connection con=DatabaseConfig.getConnection();
            PreparedStatement ps=con.prepareStatement(sql);
             ResultSet rs=ps.executeQuery()){
                    while (rs.next()) {
                        MachineStatus m=new MachineStatus();
                        m.setMachineId(rs.getString("machine_code"));
                        m.setMachineName(rs.getString("hostname"));
                        m.setIpAddress(rs.getString("ip_address"));
                        m.setOs(rs.getString("os"));
                        m.setStatus(rs.getString("status"));
                        list.add(m);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return  list;
    }
}
