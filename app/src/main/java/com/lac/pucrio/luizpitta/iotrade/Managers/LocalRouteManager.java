package com.lac.pucrio.luizpitta.iotrade.Managers;

import android.util.JsonReader;

import com.lac.pucrio.luizpitta.iotrade.Models.locals.MessageData;
import com.lac.pucrio.luizpitta.iotrade.Utils.AppUtils;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by luis on 2/05/15.
 * Handle incoming JSON messages which are commands
 * to the Mobile Hub
 */
public class LocalRouteManager {
    /** DEBUG */
    private final static String TAG = LocalRouteManager.class.getSimpleName();

    /** Instance for the singleton */
    private static LocalRouteManager instance = new LocalRouteManager();

    private LocalRouteManager() {
    }

    public static LocalRouteManager getInstance() {
        return instance;
    }

    /**
     * It receives the String message (JSON), parses and sends it to their
     * destination service
     * @param data The JSON as a String
     * @return true  if everything was Ok
     *         false if it failed
     */
    public boolean routeMessage( String data ) {
        if( data == null ) {
            AppUtils.sendErrorMessage( TAG, MessageData.ERROR.ER04.toString() );
            return false;
        }

        JsonReader reader = new JsonReader( new StringReader( data ) );
        try {
            reader.beginArray();
            while( reader.hasNext() ) {
                reader.beginObject();
                while( reader.hasNext() ) {
                    String name = reader.nextName();
                    switch( name ) {
                        default:
                            reader.skipValue();
                            break;
                    }
                }
                reader.endObject();
            }
            reader.endArray();
            return true;
        } catch( IOException | IllegalArgumentException | IllegalStateException e ) {
            AppUtils.sendErrorMessage( TAG, e.getMessage() );
        }
        return false;
    }
}
