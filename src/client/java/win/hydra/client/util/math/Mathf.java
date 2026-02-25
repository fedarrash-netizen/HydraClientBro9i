package win.hydra.client.util.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Mathf {

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException("places < 0");
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}


