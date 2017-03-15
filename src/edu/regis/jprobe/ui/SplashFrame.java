///////////////////////////////////////////////////////////////////////////////////
//
//  Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2006  James Di Vincenzo
//
//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
///////////////////////////////////////////////////////////////////////////////////
package edu.regis.jprobe.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.border.LineBorder;


/**
 * @author jdivinc
 *
 */
public class SplashFrame extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Timer timer;
    private JProgressBar status;

    /**
     * 
     */
    public SplashFrame(final int duration) {
        
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        ImageIcon image = IconManager.getIconManager().getSplashScreen();
        
        if (image == null) {
            return;
        }
        
        int startWidth = image.getIconWidth();
        int startHeight = image.getIconHeight();
        int x = (d.width - startWidth) / 2; 
        int y = (d.height - startHeight) / 2;
        JLabel l1 = new JLabel(image);
       
        add(l1, BorderLayout.NORTH);
        
        status = new JProgressBar();
        status.setString("Starting...");
        status.setStringPainted(true);
        status.setOpaque(false);
        status.setForeground(Color.GRAY);
        status.setMinimum(0);
        status.setMaximum(duration);
        
        
        setLocation (x,  y); 
        setSize(startWidth, startHeight + 20);
        Dimension statusSize = new Dimension(startWidth,20);
        status.setPreferredSize(statusSize);
        status.setBorder(new LineBorder(Color.black,1));
        add(status, BorderLayout.SOUTH);
        this.setUndecorated(true);
        
        final int increment = duration / 10;
       
        
        final long start = System.currentTimeMillis();
        
        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (System.currentTimeMillis() - start > duration) {
                    close();
                } else {
                    status.setValue(status.getValue() + increment);
                }
                
            }
            
        };
        timer = new Timer(100, al);
        timer.start();
        setVisible(true);
        
        
    }
    public void close() {
        this.dispose();
        timer.stop();
    }
    
    public void setStatus(String msg) {
        status.setString(msg);
    }
    
    public static void main(String[] args) throws Exception {
        SplashFrame sf = new SplashFrame(5000);
        for (int i = 0; i < 10; i++) {
            sf.setStatus("Loading Component " + i);
            Thread.sleep(500);
        }
    }

}
