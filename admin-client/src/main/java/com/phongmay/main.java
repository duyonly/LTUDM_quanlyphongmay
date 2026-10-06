package com.phongmay;

import javax.swing.SwingUtilities;

import com.phongmay.view.LoginView;

class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginView().setVisible(true));
    }
}