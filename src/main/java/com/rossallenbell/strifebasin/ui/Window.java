package com.rossallenbell.strifebasin.ui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.rossallenbell.strifebasin.connection.ConnectionToOpponent;
import com.rossallenbell.strifebasin.threads.CommSocketListener;
import com.rossallenbell.strifebasin.threads.CommSocketSender;
import com.rossallenbell.strifebasin.threads.GameLoop;

@SuppressWarnings("serial")
public class Window extends JFrame {
    
    private ConnectionPanel connPanel;
    private ConnectionToOpponent connection;
    
    private static Window theInstance;
    
    public static Window getInstance() {
        if (theInstance == null) {
            synchronized (Window.class) {
                if (theInstance == null) {
                    theInstance = new Window();
                }
            }
        }
        return theInstance;
    }
    
    private Window() {
        super("Strife Basin");
        
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}
        
        connection = ConnectionToOpponent.getInstance();
        connPanel = ConnectionPanel.getInstance();
        
        setVisible(true);
        
        getContentPane().add(connPanel);
        
        Dimension size = new Dimension(640, 480);
        setPreferredSize(size);
        setMinimumSize(size);
        pack();
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (connection != null) {
                    connection.cleanup();
                }
                GameLoop.getInstance().stop();
                CommSocketSender.getInstance().stop();
                CommSocketListener.getInstance().stop();
                dispose();
            }
        });
    }
    
    public void buildGameDisplay() {
        getContentPane().removeAll();
        
        getContentPane().add(Canvas.getInstance());
        
        InputListener inputListener = InputListener.getInstance();
        addMouseListener(inputListener);
        addMouseWheelListener(inputListener);
        addKeyListener(inputListener);
        
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
}
