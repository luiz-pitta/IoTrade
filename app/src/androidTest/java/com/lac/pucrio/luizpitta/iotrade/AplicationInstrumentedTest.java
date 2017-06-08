package com.lac.pucrio.luizpitta.iotrade;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.lac.pucrio.luizpitta.iotrade.Utils.Utilities;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AplicationInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.lac.pucrio.luizpitta.iotrade", appContext.getPackageName());

        float actual = Utilities.convertDpToPixel(100.0f, appContext);
        float expected = 25.0f;
        assertEquals(actual, expected, 5);
    }
}
