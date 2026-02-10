package net.mctitan.rpg.util.directional;

public class DistanceMultiplier {
    public static double calculate(double radius, double distance, double minimum) {
        if(distance > radius) { return 0; }
        if(distance < 1) { return 1; }
        radius -= 1;
        distance -= 1;

        return (1 - minimum) + minimum * (radius - distance) / radius;
    }
}
