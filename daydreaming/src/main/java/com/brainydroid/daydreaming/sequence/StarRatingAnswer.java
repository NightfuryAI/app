package com.brainydroid.daydreaming.sequence;

import com.brainydroid.daydreaming.background.Logger;
import com.brainydroid.daydreaming.db.Views;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.inject.Inject;

import java.util.HashMap;

public class StarRatingAnswer implements IAnswer {

    @SuppressWarnings("FieldCanBeLocal")
    private static String TAG = "StarRatingAnswer";

    @JsonView(Views.Public.class)
    @Inject @JacksonInject HashMap<String, Float> starRatings;

    public synchronized void addAnswer(String text, float rating) {
        Logger.v(TAG, "Adding answer {0} at position {1}", text, rating);
        starRatings.put(text, rating);
    }

}