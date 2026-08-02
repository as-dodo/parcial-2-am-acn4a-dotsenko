package com.example.parcial_1_am_acn4a_dotsenko;

import com.google.gson.annotations.SerializedName;

public class QuoteResponse {

    @SerializedName("q")
    private String text;

    @SerializedName("a")
    private String author;

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return author;
    }
}
