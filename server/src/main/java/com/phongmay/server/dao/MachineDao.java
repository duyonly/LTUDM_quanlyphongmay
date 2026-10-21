package com.phongmay.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.phongmay.common.MachineSpecs;
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
    public int changeStatus(int timeout){
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
    public boolean saveMachineSpecs(String machineCode ,MachineSpecs specs){
        if(specs==null){
            System.out.println("Không có cấu hình phần cứng cho máy " + machineCode);
        return false;
        }
        String findSql="""
                SELECT id
                from machines
                where machine_code=?
                """;
                String updateSql="""
            UPDATE machine_specs
            SET cpu=?, ram=?,gpu=?,disk=?
            where machine_id=?
            """;
    String insertSql="""
            INSERT INTO machine_specs (machine_id,cpu,ram,gpu,disk)
            VALUES (?,?,?,?,?)
            """;
            try (Connection con=DatabaseConfig.getConnection()){
                con.setAutoCommit(false);
                try {
                    int machineDbId;
                    try (PreparedStatement ps =con.prepareStatement(findSql)){
                        ps.setString(1, machineCode);
                        try(ResultSet rs=ps.executeQuery()) {
                            if(!rs.next()){
                                System.out.println("không tim thấy máy"+machineCode);
                            }
                            machineDbId=rs.getInt("id");
                        } 
                        
                    
                        
                } 
                int update;
                try(PreparedStatement ps=con.prepareStatement(updateSql)){
                    ps.setString(1, specs.getCpu());
                    ps.setString(2, specs.getRam());
                    ps.setString(3, specs.getGpu());
                    ps.setString(4, specs.getDisk());
                    ps.setInt(5, machineDbId);

                    update=ps.executeUpdate();
                }
                if(update==0){
                    try(PreparedStatement ps=con.prepareStatement(insertSql)){
                        ps.setInt(1, machineDbId);
                        ps.setString(2, specs.getCpu());
                        ps.setString(3, specs.getRam());
                        ps.setString(4, specs.getGpu());
                        ps.setString(5, specs.getDisk());
                        ps.executeUpdate();
                    }
                }
                con.commit();
                System.out.println("Đã lưu cấu hình phần cứng cho máy " + machineCode);
                return  true;}
                
                catch (Exception e) {
                    con.rollback();
                    throw e;
                }
                finally{
                    con.setAutoCommit(true);
                }
            } catch (Exception e) {
                System.err.println("Không thể lưu cấu hình máy " + machineCode);
            e.printStackTrace();
            return false;
            }
    }
    

}
