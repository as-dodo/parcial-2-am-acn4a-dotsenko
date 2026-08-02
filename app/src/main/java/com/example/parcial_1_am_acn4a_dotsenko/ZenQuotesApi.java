package com.example.parcial_1_am_acn4a_dotsenko;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ZenQuotesApi {

    @GET("api/today")
    Call<List<QuoteResponse>> getTodayQuote();
}
