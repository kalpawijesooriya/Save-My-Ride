package com.gnex.savemyride.DirectionHelpers;

import org.json.JSONException;

/**
 * Created by Vishal on 10/20/2018.
 */

public interface TaskLoadedCallback {
    void onTaskDone(Object... values) throws JSONException;
}