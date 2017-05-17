/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.t0ast.timefetcher.tray;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author T0astBread
 */
public class TrayHandler
{
    private static TrayHandler inst;
    private TrayIcon icon;

    public static TrayHandler getInst()
    {
        return inst == null ? inst = new TrayHandler() : inst;
    }

    public TrayHandler()
    {
        Image icon = Toolkit.getDefaultToolkit().createImage("trayicon.png");
        this.icon = new TrayIcon(icon, "TimeFetcher");
    }
    
    public void addToTray()
    {
        try
        {
            SystemTray.getSystemTray().add(this.icon);
        }
        catch(AWTException ex)
        {
            Logger.getLogger(TrayHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void displayStarting()
    {
        this.icon.displayMessage("TimeFetcher for WakaTime", "", TrayIcon.MessageType.ERROR);
    }
    
    public void removeFromTray()
    {
        SystemTray.getSystemTray().remove(this.icon);
    }
}
