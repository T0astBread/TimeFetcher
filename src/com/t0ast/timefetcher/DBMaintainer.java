/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.t0ast.timefetcher;

import com.google.gson.Gson;
import com.t0ast.utils.date.DateUtils;
import com.t0ast.utils.files.FileReadingUtils;
import com.t0ast.utils.files.comparators.DateFileNameComparator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/**
 *
 * @author T0astBread
 */
public class DBMaintainer
{

    public static final LocalDate YESTERDAY = LocalDate.now().minusDays(1);

    private File db, dbData;
    private DBData data;
    private Gson gson;
    private LocalDate lastFetchedDay;

    public DBMaintainer(File db) throws IOException
    {
        this.db = FetchingController.DEBUG ? new File(db.getAbsoluteFile() + " Debug") : db;
        this.db.mkdirs();
        this.gson = new Gson();
        this.dbData = new File(this.db, "dbdata.json");
        this.dbData.createNewFile();
        this.data = loadData(this.dbData);
    }

    private DBData loadData(File dbData)
    {
        try
        {
            DBData data = gson.fromJson(FileReadingUtils.readWholeFile(dbData), DBData.class);
            return data != null ? data : new DBData();
        }
        catch(IOException ex)
        {
            return new DBData();
        }
    }

    public LocalDate getDayToFetchFrom()
    {
        LocalDate fetchFrom = this.data.lastFetchedFrom != null ? this.data.lastFetchedFrom.minusDays(1) : this.data.lastFetchedTo;
        if(fetchFrom == null)
        {
            fetchFrom = getLatestFetchedDayFromFileStructure(db);
            if(fetchFrom == null)
            {
                fetchFrom = YESTERDAY;
            }
        }
        else fetchFrom = fetchFrom.plusDays(1);
        this.lastFetchedDay = fetchFrom;
        return fetchFrom;
    }

    private LocalDate getLatestFetchedDayFromFileStructure(File db)
    {
        Comparator<File> fileNameDateComparator = new DateFileNameComparator();
        String latestFetch = null;
        Optional<File> latestFetchOptional = Arrays.stream(db.listFiles()).reduce((f1, f2) -> fileNameDateComparator.compare(f1, f2) > 0 ? f1 : f2);
        if(latestFetchOptional.isPresent())
        {
            latestFetch = latestFetchOptional.get().getName();
        }
        return latestFetch != null && DateUtils.isDate(latestFetch) ? LocalDate.parse(latestFetch) : null;
    }

    public DBEntry createEntryForDay(LocalDate day, LocalDateTime timeOfFetching) throws IOException
    {

//            File dir = new File(new File(new File(db, Integer.toString(dayToFetch.getYear())), Integer.toString(dayToFetch.getMonthValue())), Integer.toString(dayToFetch.getDayOfMonth()));
        File dir = new File(new File(db, day.toString()), "fetched_at_" + getTimeAsFileName(timeOfFetching));
        dir.mkdirs();
        this.lastFetchedDay = day;
        return new DBEntry(dir);
    }
    
    private String getTimeAsFileName(LocalDateTime time)
    {
        return time.toString().replace(":", ",");
    }
    
    public void writeDBData() throws IOException
    {
        try(FileWriter dataOut = new FileWriter(this.dbData))
        {
            this.data.lastFetched = LocalDateTime.now();
            this.data.lastFetchedFrom = this.data.lastFetchedTo != null && YESTERDAY.compareTo(this.data.lastFetchedTo) > 0 ? this.data.lastFetchedTo : this.lastFetchedDay;
            this.data.lastFetchedTo = YESTERDAY.compareTo(this.lastFetchedDay) > 0 ? this.lastFetchedDay : YESTERDAY;
            dataOut.write(this.gson.toJson(this.data));
        }
    }

    public static class DBData
    {

        public LocalDateTime lastFetched;
        public LocalDate lastFetchedFrom, lastFetchedTo;
    }

    public static class DBEntry
    {

        private File dir, response, meta;

        public DBEntry(File dir) throws IOException
        {
            this.dir = dir;
            this.response = new File(dir, "response.json");
            this.response.createNewFile();
            this.meta = new File(dir, "meta.json");
            this.meta.createNewFile();
        }

        public File getDir()
        {
            return dir;
        }

        public File getResponse()
        {
            return response;
        }

        public File getMeta()
        {
            return meta;
        }
    }
}
