package com.pavelurusov.conway;

import javafx.scene.paint.Color;

public final class Settings {

    private static Settings instance = null;
    public final int ROWS = 50;
    public final int COLUMNS = 75;
    public final Color COLOR_DEAD = Color.WHITE;
    public final Color COLOR_ALIVE = Color.BLACK;
    public final Color COLOR_GRID = Color.LIGHTGRAY;
    public final double INTERVAL = 2e8;
    public final int SIZE = 12;

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }
}
