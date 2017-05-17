/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.t0ast.timefetcher;

import com.google.gson.Gson;
import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

/**
 *
 * @author T0astBread
 */
public class FetchingController
{
    public static final boolean DEBUG = false;
    
    private final DBMaintainer maintainer;
    private final ApiFetcher fetcher;
    private final Gson gson;

    public FetchingController(File db, String apiSecret) throws IOException
    {
        this.maintainer = new DBMaintainer(db);
        this.fetcher = new ApiFetcher(apiSecret);
        this.gson = new Gson();
    }
    
    public void startFetching() throws IOException
    {
        LocalDate dayToFetch = this.maintainer.getDayToFetchFrom();
        for(; dayToFetch.compareTo(LocalDate.now()) < 0; dayToFetch = dayToFetch.plusDays(1))
        {
            ApiFetcher.ApiResponse response = this.fetcher.getData(dayToFetch); //REPLACE THIS WITH getData TO GET THE ACTUAL API DATA (or debugGetData for debugging purposes)
            DBMaintainer.DBEntry entry = this.maintainer.createEntryForDay(dayToFetch, response.getFetchedAt());
            try(FileWriter responseOut = new FileWriter(entry.getResponse()))
            {
                responseOut.write(response.getResponse());
            }
            try(FileWriter metaOut = new FileWriter(entry.getMeta()))
            {
                metaOut.write(this.gson.toJson(response.getMeta()));
            }
            if(!response.isSuccessful())
            {
                Desktop.getDesktop().browse(entry.getResponse().toURI());
                Desktop.getDesktop().browse(entry.getMeta().toURI());
            }
        }
        this.maintainer.writeDBData();
    }
}
