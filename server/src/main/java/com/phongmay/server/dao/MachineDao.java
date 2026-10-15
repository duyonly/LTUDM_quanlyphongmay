package com.phongmay.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.phongmay.common.MachineStatus;
import com.phongmay.server.config.DatabaseConfig;

public class MachineDao {
    public JSONArray getMachines() throws SQLException {
        String sql = """
              SELECT m.machine_code, m.hostname, m.ip_address, m.os, m.status, m.last_seen,
                  ms.cpu, ms.gpu, ms.ram, ms.disk,
                  latest_session.connected_at AS session_started_at,
                  latest_session.disconnected_at AS session_ended_at
              FROM machines m
              LEFT JOIN machine_specs ms ON ms.machine_id = m.id
              LEFT JOIN LATERAL (
                  SELECT connected_at, disconnected_at
                  FROM sessions
                  WHERE machine_id = m.id
                  ORDER BY connected_at DESC
                  LIMIT 1
              ) latest_session ON TRUE
              ORDER BY m.machine_code
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
                machine.put("cpu", resultSet.getString("cpu"));
                machine.put("gpu", resultSet.getString("gpu"));
                machine.put("ram", resultSet.getString("ram"));
                machine.put("disk", resultSet.getString("disk"));
                java.sql.Timestamp sessionStartedAt = resultSet.getTimestamp("session_started_at");
                java.sql.Timestamp sessionEndedAt = resultSet.getTimestamp("session_ended_at");
                boolean sessionActive = sessionStartedAt != null && sessionEndedAt == null;
                long activeSinceMs = sessionActive ? sessionStartedAt.getTime() : 0L;
                long elapsedSeconds = 0L;
                if (sessionStartedAt != null) {
                    long endTime = sessionActive ? System.currentTimeMillis() : sessionEndedAt.getTime();
                    elapsedSeconds = Math.max(0L, (endTime - sessionStartedAt.getTime()) / 1000L);
                }
                machine.put("active_since_ms", activeSinceMs);
                machine.put("uptime_seconds", elapsedSeconds);
                machines.put(machine);
            }
        }
        return machines;
    }

    public boolean openMachine(String machineCode) throws SQLException {
        String updateMachineSql = """
                UPDATE machines
                SET status = 'ONLINE', last_seen = CURRENT_TIMESTAMP
                WHERE machine_code = ?
                RETURNING id
                """;
        String activeSessionSql = """
                SELECT 1 FROM sessions
                WHERE machine_id = ? AND disconnected_at IS NULL
                LIMIT 1
                """;
        String insertSessionSql = "INSERT INTO sessions (machine_id) VALUES (?)";

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long machineId;
                try (PreparedStatement statement = connection.prepareStatement(updateMachineSql)) {
                    statement.setString(1, machineCode);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            connection.rollback();
                            return false;
                        }
                        machineId = resultSet.getLong("id");
                    }
                }

                boolean hasActiveSession;
                try (PreparedStatement statement = connection.prepareStatement(activeSessionSql)) {
                    statement.setLong(1, machineId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        hasActiveSession = resultSet.next();
                    }
                }
                if (!hasActiveSession) {
                    try (PreparedStatement statement = connection.prepareStatement(insertSessionSql)) {
                        statement.setLong(1, machineId);
                        statement.executeUpdate();
                    }
                }

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public boolean closeMachine(String machineCode) throws SQLException {
        String updateMachineSql = """
                UPDATE machines SET status = 'OFFLINE'
                WHERE machine_code = ?
                RETURNING id
                """;
        String closeSessionSql = """
                UPDATE sessions
                SET disconnected_at = CURRENT_TIMESTAMP
                WHERE machine_id = ? AND disconnected_at IS NULL
                """;

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long machineId;
                try (PreparedStatement statement = connection.prepareStatement(updateMachineSql)) {
                    statement.setString(1, machineCode);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            connection.rollback();
                            return false;
                        }
                        machineId = resultSet.getLong("id");
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(closeSessionSql)) {
                    statement.setLong(1, machineId);
                    statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public void addMachine(String machineCode, String hostname, String ipAddress, String os,
            String cpu, String gpu, String ram, String disk) throws SQLException {
        String insertMachineSql = """
                INSERT INTO machines (machine_code, hostname, ip_address, os, status)
                VALUES (?, ?, ?, ?, 'OFFLINE')
                RETURNING id
                """;
        String insertSpecsSql = """
                INSERT INTO machine_specs (machine_id, cpu, gpu, ram, disk)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long machineId;
                try (PreparedStatement statement = connection.prepareStatement(insertMachineSql)) {
                    statement.setString(1, machineCode);
                    statement.setString(2, hostname);
                    statement.setString(3, ipAddress);
                    statement.setString(4, os);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new SQLException("Không tạo được thiết bị trong database.");
                        }
                        machineId = resultSet.getLong("id");
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(insertSpecsSql)) {
                    statement.setLong(1, machineId);
                    statement.setString(2, cpu);
                    statement.setString(3, gpu);
                    statement.setString(4, ram);
                    statement.setString(5, disk);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public boolean updateMachine(String originalMachineCode, String machineCode, String hostname,
            String os, String cpu, String gpu, String ram, String disk) throws SQLException {
        String updateMachineSql = """
                UPDATE machines
                SET machine_code = ?, hostname = ?, os = ?
                WHERE machine_code = ?
                RETURNING id
                """;
        String updateSpecsSql = """
                INSERT INTO machine_specs (machine_id, cpu, gpu, ram, disk)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (machine_id) DO UPDATE
                SET cpu = EXCLUDED.cpu, gpu = EXCLUDED.gpu,
                    ram = EXCLUDED.ram, disk = EXCLUDED.disk
                """;

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long machineId;
                try (PreparedStatement statement = connection.prepareStatement(updateMachineSql)) {
                    statement.setString(1, machineCode);
                    statement.setString(2, hostname);
                    statement.setString(3, os);
                    statement.setString(4, originalMachineCode);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            connection.rollback();
                            return false;
                        }
                        machineId = resultSet.getLong("id");
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(updateSpecsSql)) {
                    statement.setLong(1, machineId);
                    statement.setString(2, cpu);
                    statement.setString(3, gpu);
                    statement.setString(4, ram);
                    statement.setString(5, disk);
                    statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
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
