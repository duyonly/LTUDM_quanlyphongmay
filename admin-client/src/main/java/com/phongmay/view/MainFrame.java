package com.phongmay.view;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import com.phongmay.client.ServerConnection;

public class MainFrame extends JFrame {

	public MainFrame(ServerConnection serverConnection) {
		super("Dashboard quản lý phòng máy");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(720, 480));
		setSize(900, 600);
		setLocationRelativeTo(null);
		setContentPane(new DashbbordView(serverConnection));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent event) {
				serverConnection.close();
			}
		});
	}
}

