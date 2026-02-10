package net.mctitan.rpg.util.directional;

import net.mctitan.data.BasicData;
import org.bukkit.World;

public class Vector extends BasicData {
    private double x = 0;
    private double y = 0;
    private double z = 0;

    public Vector() {}
    public Vector(org.bukkit.util.Vector v) { this(v.getX(), v.getY(), v.getZ()); }
    public Vector(Vector v) { this(v.x, v.y, v.z); }
    public Vector(org.bukkit.Location l) { this(l.getX(), l.getY(), l.getZ()); }
    public Vector(Location l) { this(l.x(), l.y(), l.z()); }
    public Vector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector clone() { return new Vector(this); }

    public Location location(World world) { return new Location(world, this.x, this.y, this.z); }
    public org.bukkit.util.Vector bukkit() { return new org.bukkit.util.Vector(x, y, z); }
    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }

    public Vector x(double x) { return new Vector(x, y, z); }
    public Vector y(double y) { return new Vector(x, y, z); }
    public Vector z(double z) { return new Vector(x, y, z); }

    public double magnitude() { return Math.sqrt(x*x + y*y + z*z); }

    public Vector add(Vector v) { return add(v.x, v.y, v.z); }
    public Vector add(double x, double y, double z) { return new Vector(this.x + x, this.y + y, this.z + z); }
    public Vector subtract(Vector v) { return subtract(v.x, v.y, v.z); }
    public Vector subtract(double x, double y, double z) { return add(-x, -y, -z); }
    public Vector multiply(double v) { return new Vector(x*v, y*v, z*v); }
    public Vector normalize() { return multiply(1 / magnitude()); }
    public double dot(Vector v) { return x*v.x + y*v.y + z*v.z; }
    public Vector cross(Vector v) { return new Vector(y*v.z - z*v.y, z*v.x - x*v.z, x*v.y - y*v.x); }

    public String toString() { return String.format("(%.02f, %.02f, %.02f)", x, y, z); }
}
