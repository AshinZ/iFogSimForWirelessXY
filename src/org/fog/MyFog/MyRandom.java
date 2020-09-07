package org.fog.MyFog;

/**
 * Random number generation
 * Created by Z_HAO on 2020/2/23
 */
public class MyRandom {
    private double x;
    private double y;
    private double lambda;

    /**
     * Constructor with a parameter lambda.
     * @param _lambda _lambda means λ in exponential distribution
     */
    public MyRandom(double _lambda) {
        lambda = _lambda;
        x = expDoubleRand();
    }

    /**
     * Constructor which limits the random number from l to r. [l, r)
     * @param _lambda _lambda means λ in exponential distribution
     * @param l left boundary
     * @param r right boundary
     */
    public MyRandom(double _lambda , double l , double r) {
        lambda = _lambda;
        x = expDoubleRand(l , r);
        y *= (r - l) + l;
    }

    /**
     * Generate a random double number which satisfies exponential distribution.
     * @return a random double number
     */
    public double expDoubleRand() {
        y = Math.random();
        return -(1 / lambda) * Math.log(1.0 - y);
    }

    /**
     * Generate a random double number which satisfies exponential distribution ranges from l to r.
     * @return a random double number
     */
    public double expDoubleRand(double l , double r) {
        double left = 1.0 - Math.exp(-lambda * l);
        double right = 1.0 - Math.exp(-lambda * r);
        y = Math.random() * (right - left) + left;
        return -(1 / lambda) * Math.log(1.0 - y);
    }

    public int getIntRandom() {
        return (int)x + 1;
    }

    public double getDoubleRandom() {
        return x;
    }

    public double getRandom() {
        return y;
    }
}