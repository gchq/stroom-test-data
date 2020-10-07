package stroom.testdata;

import java.util.concurrent.atomic.AtomicLong;

class AtomicLoopedLongSequence {
    private final long startInc;
    private final long endExc;
    private final AtomicLong lastVal = new AtomicLong();

    public AtomicLoopedLongSequence(final long endExc) {
        this(0, endExc);
    }

    public AtomicLoopedLongSequence(final long startInc, final long endExc) {
        Utils.checkArgument(endExc > startInc, "endExc must be > startInc");

        this.startInc = startInc;
        this.endExc = endExc;
        this.lastVal.set(startInc - 1L);
    }

    public long getNext() {
        return lastVal.updateAndGet(val -> {
            long newVal = val + 1;

            if (newVal >= endExc) {
                newVal = startInc;
            }
            return newVal;
        });
    }
}
