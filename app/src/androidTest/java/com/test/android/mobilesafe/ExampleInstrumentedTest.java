package com.test.android.mobilesafe;

import android.content.Context;
import android.support.annotation.InterpolatorRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.view.animation.Interpolator;

import com.test.android.mobilesafe.db.dao.BlackNumberDao;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.test.android.mobilesafe", appContext.getPackageName());
    }

    @Test
    public void insertTest(){
        Context context = InstrumentationRegistry.getTargetContext();
        assertEquals("com.test.android.mobilesafe", context.getPackageName());
        BlackNumberDao dao = BlackNumberDao.getInstance(context);
        int mode = 0;
        for (int i = 0;i < 100;i ++){
            mode = new Random().nextInt(3);
            if (i < 10){
                dao.insert("1860000000" + i,mode+"");
            }else {
                dao.insert("186000000"+i,mode+"");
            }
        }
    }

}
