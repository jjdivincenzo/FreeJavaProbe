///////////////////////////////////////////////////////////////////////////////////
//
//  Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2007  James Di Vincenzo
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import edu.regis.jprobe.model.Logger;

class TimerThread extends Thread {
    
    private ActionListener al;
    private long interval;
    private boolean running = false;
    private boolean active = true;
    private Logger logger;
    
    public TimerThread(ActionListener al, long interval, String name) {
        this.al = al;
        this.interval = interval;
        logger = Logger.getLogger();
        this.setName("UIUpdater:" + name);
        this.setDaemon(true);
        this.start();
    }
    
    public void run() {
        logger.info("Starting " + getName() + " Thread");
        while(active) {
            
            if (running) {
                try {
                    al.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Timer Event"));
                    synchronized(this) {
                        wait(interval);
                    }
                    
                } catch (Throwable e) {
                    logger.logException(e, this);
                }
            } else {
                synchronized(this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        logger.info(getName() + " has been interrupted");
                    }
                }
            }
               
            
            
        }
        logger.info(getName() + " Thread is Ending");
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
        synchronized(this) {
            notify();
        }
    }
    public void shutdown() {
        active = false;
        synchronized(this) {
            notify();
        }
    }


}