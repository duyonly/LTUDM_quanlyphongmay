package com.phongmay.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.phongmay.server.config.DatabaseConfig;

public class UserDao {

	public boolean authenticate(String username, String password) throws SQLException {
		String sql = "SELECT 1 FROM users WHERE username = ? AND password_hash = ?";
		try (Connection connection = DatabaseConfig.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, username);
			statement.setString(2, password);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next();
			}
		}
	}
}
