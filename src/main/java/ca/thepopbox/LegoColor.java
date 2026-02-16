package ca.thepopbox;

public class LegoColor {
    private final String name;
    private final int code;

    public LegoColor(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return name;
    }
}
