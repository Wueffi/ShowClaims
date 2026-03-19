package wueffi.showClaims.client;

public record Region(String label, double ax, double ay, double az, double bx, double by, double bz) {

    public boolean contains(double x, double y, double z) {
        double minX = Math.min(ax, bx), maxX = Math.max(ax, bx);
        double minY = Math.min(ay, by), maxY = Math.max(ay, by);
        double minZ = Math.min(az, bz), maxZ = Math.max(az, bz);
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
}
