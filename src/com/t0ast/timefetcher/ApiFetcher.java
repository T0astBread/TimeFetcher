/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.t0ast.timefetcher;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * @author T0astBread
 */
public class ApiFetcher
{

    public static final int FETCH_TRIES = 4;

    private OkHttpClient client;
    private String apiKey64;

    public ApiFetcher(String apiKey)
    {
        this.client = new OkHttpClient();
        this.apiKey64 = Base64.getEncoder().encodeToString(apiKey.getBytes());
    }

    public ApiResponse getData(LocalDate forDay) throws IOException
    {
        if(FetchingController.DEBUG) return debugGetData(forDay);
        
        Request request = buildRequest(forDay);
        ApiResponse lastResponse = new ApiResponse();
        for(int i = 0; i < FETCH_TRIES; i++)
        {
            Response response = this.client.newCall(request).execute();
            lastResponse.setResponse(response.body().string());
            lastResponse.setTries(i + 1);
            lastResponse.setFetchedNow();
            if(response.isSuccessful())
            {
                lastResponse.setSuccessful(true);
                return lastResponse;
            }
            try
            {
                Thread.sleep(100);
            }
            catch(InterruptedException ex)
            {
                Logger.getLogger(ApiFetcher.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
        return lastResponse;
    }

    ApiResponse debugGetData(LocalDate forDay)
    {
        ApiResponse res = new ApiResponse();
        if(Math.random() > .5f)
        {
            res.setResponse("debug response");
            res.setSuccessful(true);
            res.setTries(1);
        }
        else
        {
            res.setResponse("none");
            res.setSuccessful(false);
            res.setTries((int) (Math.random() * FETCH_TRIES));
        }
        return res;
    }

    private Request buildRequest(LocalDate forDay)
    {
        String dateToGet = forDay.toString();
        return new Request.Builder()
        .url("https://wakatime.com/api/v1/users/current/summaries?start=" + dateToGet + "&end=" + dateToGet)
        .addHeader("Authorization", "Basic " + this.apiKey64) //Authenticates with your API key, not OAuth
        .build();
    }

    public static class ApiResponse
    {
        private String response;
        private ResponseMeta meta;

        private ApiResponse()
        {
            this.response = "none";
            this.meta = new ResponseMeta();
        }

        private void setResponse(String response)
        {
            this.response = response;
        }

        public String getResponse()
        {
            return response;
        }

        public int getTries()
        {
            return this.meta.tries;
        }

        private void setTries(int tries)
        {
            this.meta.tries = tries;
        }

        public boolean isSuccessful()
        {
            return this.meta.successful;
        }

        private void setSuccessful(boolean successful)
        {
            this.meta.successful = successful;
        }
        
        public LocalDateTime getFetchedAt()
        {
            return this.meta.fetchedAt;
        }
        
        public void setFetchedAt(LocalDateTime fetchedAt)
        {
            this.meta.fetchedAt = fetchedAt;
        }
        
        public void setFetchedNow()
        {
            setFetchedAt(LocalDateTime.now());
        }

        public ResponseMeta getMeta()
        {
            return meta;
        }

        public static class ResponseMeta
        {
            private boolean successful;
            private int tries;
            private LocalDateTime fetchedAt;

            public ResponseMeta()
            {
                this.fetchedAt = LocalDateTime.now();
            }

            public boolean isSuccessful()
            {
                return successful;
            }

            public int getTries()
            {
                return tries;
            }

            public LocalDateTime getFetchedAt()
            {
                return fetchedAt;
            }
        }
    }
}
