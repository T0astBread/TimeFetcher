/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.t0ast.timefetcher;

import java.io.File;

/**
 *
 * @author T0astBread
 */
public class Main
{

    static final String DB_PATH = System.getProperty("user.home") + "/Documents/WakaTime Data/";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception
    {
        FetchingController controller = new FetchingController(new File(DB_PATH), ApiSecret.API_KEY); //Replace this with the auth token copied from Postman
        controller.startFetching();
    }
}
