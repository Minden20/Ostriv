package com.minden;

import com.minden.ui.JavaFxApp;

/**
 * Клас-лаунчер для обходу помилки:
 * "JavaFX runtime components are missing, and are required to run this application"
 * у сучасних версіях Java (11+).
 * 
 * ЗАПУСКАЙТЕ САМЕ ЦЕЙ КЛАС В IDE!
 */
public class Launcher {
    public static void main(String[] args) {
        JavaFxApp.main(args);
    }
}
