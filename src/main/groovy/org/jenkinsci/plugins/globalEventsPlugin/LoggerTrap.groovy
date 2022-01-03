package org.jenkinsci.plugins.globalEventsPlugin

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Created by nickgrealy@gmail.com on 19/09/2015.
 */
class LoggerTrap extends Logger {

    List<String> all = [], severe = [], warning = [], info = [], fine = [], finer = [], finest = [], config = []

    protected LoggerTrap(String name) {
        super(name, null)
    }

    @Override
    void log(Level level, String msg, Throwable thrown) {
        log(level, msg)
    }

    @Override
    void log(Level level, String msg) {
        all << msg
        switch (level) {
            case Level.CONFIG:
                config << msg
                break
            case Level.FINE:
                fine << msg
                break
            case Level.FINER:
                finer << msg
                break
            case Level.FINEST:
                finest << msg
                break
            case Level.SEVERE:
                severe << msg
                break
            case Level.WARNING:
                warning << msg
                break
            default:
                info << msg
        }
    }

    @Override
    void severe(String msg) {
        all << msg
        severe << msg
    }

    @Override
    void warning(String msg) {
        all << msg
        warning << msg
    }

    @Override
    void info(String msg) {
        all << msg
        info << msg
    }

    @Override
    void finer(String msg) {
        all << msg
        finer << msg
    }

    @Override
    void fine(String msg) {
        all << msg
        fine << msg
    }

    @Override
    void finest(String msg) {
        all << msg
        finest << msg
    }

    @Override
    void config(String msg) {
        all << msg
        config << msg
    }
}
