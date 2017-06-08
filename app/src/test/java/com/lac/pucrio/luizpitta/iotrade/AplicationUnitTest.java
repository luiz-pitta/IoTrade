package com.lac.pucrio.luizpitta.iotrade;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.test.mock.MockContext;

import com.lac.pucrio.luizpitta.iotrade.Utils.Utilities;

import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class AplicationUnitTest {

    private final Context mContext;

    public AplicationUnitTest() {
        Context mockContext = createMockContext();
        mContext = mockContext.getApplicationContext();
    }

    @Test
    public void convertionDpToPixelCorrect() throws Exception {
        float actual = Utilities.convertDpToPixel(100.0f, mContext);
        float expected = 25.0f;
        assertEquals(actual, expected, 5);
    }

    /**
     * @return A mocked context which returns a spy of {@link RuntimeEnvironment#application} in
     * {@link Context#getApplicationContext()}.
     */
    public static Context createMockContext() {
        // otherwise the JobScheduler isn't supported we check if the service is enable
        // Robolectric doesn't parse services from the manifest, see https://github.com/robolectric/robolectric/issues/416
        PackageManager packageManager = mock(PackageManager.class);
        when(packageManager.queryBroadcastReceivers(any(Intent.class), anyInt())).thenReturn(Collections.singletonList(new ResolveInfo()));

        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.serviceInfo = new ServiceInfo();
        resolveInfo.serviceInfo.permission = "android.permission.BIND_JOB_SERVICE";
        when(packageManager.queryIntentServices(any(Intent.class), anyInt())).thenReturn(Collections.singletonList(resolveInfo));

        Context context = spy(RuntimeEnvironment.application);
        when(context.getPackageManager()).thenReturn(packageManager);

        Context mockContext = mock(MockContext.class);
        when(mockContext.getApplicationContext()).thenReturn(context);
        return mockContext;
    }
}