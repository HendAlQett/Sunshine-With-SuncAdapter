package observers;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * Created by hend on 6/19/16.
 */
public final class BusProvider {
    private static final Bus bus = new Bus(ThreadEnforcer.ANY);

    public static Bus getInstance() {
        return bus;
    }

    private BusProvider() {

    }
}
