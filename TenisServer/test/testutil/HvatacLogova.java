package testutil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import static org.junit.Assert.fail;

/**
 * Hvata poruke koje klasa upise preko java.util.logging, da bi test mogao da
 * proveri da je greska zabelezena (ili da nije).
 */
public class HvatacLogova extends Handler implements AutoCloseable {

    private final Logger logger;
    private final List<LogRecord> zapisi = new CopyOnWriteArrayList<>();

    public HvatacLogova(Class<?> klasa) {
        logger = Logger.getLogger(klasa.getName());
        setLevel(Level.ALL);
        logger.addHandler(this);
    }

    @Override
    public void publish(LogRecord zapis) {
        zapisi.add(zapis);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        logger.removeHandler(this);
    }

    public List<LogRecord> greske() {
        List<LogRecord> rezultat = new ArrayList<>();
        for (LogRecord z : zapisi) {
            if (z.getLevel() == Level.SEVERE) {
                rezultat.add(z);
            }
        }
        return rezultat;
    }

    public LogRecord cekajGresku() {
        long kraj = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < kraj) {
            List<LogRecord> g = greske();
            if (!g.isEmpty()) {
                return g.get(0);
            }
            Gui.spavaj(10);
        }
        fail("Greska nije zabelezena u logu klase " + logger.getName() + ".");
        return null;
    }
}
