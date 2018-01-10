package portwatch.core.config.profile.port;

/**
 * Port profile for specifying a range of ports.
 */
public class PortRangeProfile implements PortProfile {

    private int lowerBound;
    private int upperBound;

    /**
     * @param lowerBound lower port bound (inclusive)
     * @param upperBound upper port bound (inclusive)
     */
    public PortRangeProfile(int lowerBound, int upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public String toString() {
        return String.format("%d-%d", this.lowerBound, this.upperBound);
    }
}
