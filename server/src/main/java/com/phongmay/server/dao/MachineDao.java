package com.phongmay.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.phongmay.server.config.DatabaseConfig;

public class MachineDao {
    public JSONArray getMachines() throws SQLException {
        String sql = """
                SELECT machine_code, hostname, ip_address, os, status, last_seen
                FROM machines
                ORDER BY machine_code
                """;
        JSONArray machines = new JSONArray();
        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                JSONObject machine = new JSONObject();
                machine.put("machine_code", resultSet.getString("machine_code"));
                machine.put("hostname", resultSet.getString("hostname"));
                machine.put("ip_address", resultSet.getString("ip_address"));
                machine.put("os", resultSet.getString("os"));
                machine.put("status", resultSet.getString("status"));
                machine.put("last_seen", resultSet.getString("last_seen"));
                machines.put(machine);
            }
        }
        return machines;
    }

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
