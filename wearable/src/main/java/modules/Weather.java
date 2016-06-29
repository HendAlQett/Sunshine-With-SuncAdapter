package modules;

import android.graphics.Bitmap;

/**
 * Created by hend on 6/29/16.
 */
public class Weather {

    String max;
    String min;
    Bitmap bitmap;

    public Weather(String max, String min, Bitmap bitmap) {
        this.max = max;
        this.min = min;
        this.bitmap = bitmap;
    }

    public String getMax() {
        return max;
    }

    public String getMin() {
        return min;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
