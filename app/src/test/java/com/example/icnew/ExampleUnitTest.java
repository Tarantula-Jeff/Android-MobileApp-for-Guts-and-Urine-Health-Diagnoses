package com.example.icnew;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import android.graphics.Bitmap;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;



/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Mock
    MainActivity mainActivity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testClassifyImage() {
        Bitmap mockBitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);

        try {
            doNothing().when(mainActivity).showConfirmationDialog(any(String.class));
            doNothing().when(mainActivity).sendSMS(anyString(), anyString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Mock method calls
        when(mainActivity.checkSelfPermission(anyString())).thenReturn(0); // Permission granted
        when(mainActivity.createScaledBitmap(any(Bitmap.class), anyInt(), anyInt(), anyBoolean())).thenReturn(mockBitmap);

        // Call the method to be tested
        mainActivity.classifyImage(mockBitmap);

        // Verify method calls
        verify(mainActivity, times(1)).showConfirmationDialog(any(String.class));
        verify(mainActivity, times(1)).sendSMS(anyString(), anyString());
    }
}
