package com.sqrt4.grimedi;

import com.sqrt4.grimedi.ui.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            JFrame.setDefaultLookAndFeelDecorated(true);
        } catch(Exception e) {

        }
        MainWindow mw = new MainWindow();
        mw.setVisible(true);
    }
}