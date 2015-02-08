package retrovolley.shadows;

import android.os.SystemClock;
import org.robolectric.annotation.Implements;

/**
 * A workaround stub for volley system clock usage
 *
 * @author Serj Lotutovici
 */
@Implements(
        value = SystemClock.class,
        callThroughByDefault = true
)
public class ShadowSystemClock {

    /**
     * Shadow method of {@link android.os.SystemClock#elapsedRealtime}
     *
     * @return 0
     */
    public static long elapsedRealtime() {
        return 0;
    }
}
