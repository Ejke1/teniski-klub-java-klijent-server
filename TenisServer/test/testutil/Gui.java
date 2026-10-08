package testutil;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import static org.junit.Assert.fail;
import org.junit.Assume;

public final class Gui {

    private static final long CEKANJE_MS = 10000;

    private Gui() {
    }

    public static class Poruka {

        private final JDialog dijalog;
        private final JOptionPane panel;

        Poruka(JDialog dijalog, JOptionPane panel) {
            this.dijalog = dijalog;
            this.panel = panel;
        }

        public String getTekst() {
            return String.valueOf(panel.getMessage());
        }

        public String getNaslov() {
            return dijalog.getTitle();
        }

        public int getTip() {
            return panel.getMessageType();
        }

        public void odgovori(int opcija) {
            naEdt(() -> panel.setValue(opcija));
            long kraj = System.currentTimeMillis() + CEKANJE_MS;
            while (saEdt(dijalog::isShowing)) {
                if (System.currentTimeMillis() > kraj) {
                    fail("Poruka '" + getTekst() + "' se nije zatvorila.");
                }
                spavaj(10);
            }
        }
    }

    public static void proveriEkran() {
        Assume.assumeFalse("Nema ekrana - testovi formi se preskacu.", GraphicsEnvironment.isHeadless());
    }

    public static void naEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (InvocationTargetException ex) {
            Throwable uzrok = ex.getCause();
            if (uzrok instanceof RuntimeException) {
                throw (RuntimeException) uzrok;
            }
            if (uzrok instanceof Error) {
                throw (Error) uzrok;
            }
            throw new RuntimeException(uzrok);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(ex);
        }
    }

    public static <T> T saEdt(Callable<T> c) {
        AtomicReference<T> rezultat = new AtomicReference<>();
        naEdt(() -> {
            try {
                rezultat.set(c.call());
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        return rezultat.get();
    }

    public static void kasnije(Runnable r) {
        SwingUtilities.invokeLater(r);
    }

    public static void klikni(AbstractButton dugme) {
        kasnije(dugme::doClick);
    }

    public static void klikniISacekaj(AbstractButton dugme) {
        naEdt(dugme::doClick);
    }

    public static void sacekajEdt() {
        naEdt(() -> {
        });
    }

    public static Poruka cekajPoruku() {
        long kraj = System.currentTimeMillis() + CEKANJE_MS;
        while (System.currentTimeMillis() < kraj) {
            Poruka p = saEdt(Gui::nadjiPoruku);
            if (p != null) {
                return p;
            }
            spavaj(10);
        }
        fail("Nije se pojavila ocekivana poruka (JOptionPane).");
        return null;
    }

    public static String zatvoriPoruku() {
        Poruka p = cekajPoruku();
        String tekst = p.getTekst();
        p.odgovori(JOptionPane.OK_OPTION);
        return tekst;
    }

    public static void odgovoriNaPitanje(int opcija) {
        cekajPoruku().odgovori(opcija);
    }

    public static boolean imaOtvorenihPoruka() {
        return saEdt(Gui::nadjiPoruku) != null;
    }

    private static Poruka nadjiPoruku() {
        for (Window w : Window.getWindows()) {
            if (w instanceof JDialog && w.isShowing()) {
                JOptionPane panel = nadjiKomponentu(((JDialog) w).getContentPane(), JOptionPane.class);
                if (panel != null && panel.getValue() == JOptionPane.UNINITIALIZED_VALUE) {
                    return new Poruka((JDialog) w, panel);
                }
            }
        }
        return null;
    }

    public static <T extends Window> T cekajProzor(Class<T> tip) {
        long kraj = System.currentTimeMillis() + CEKANJE_MS;
        while (System.currentTimeMillis() < kraj) {
            T prozor = saEdt(() -> {
                for (Window w : Window.getWindows()) {
                    if (tip.isInstance(w) && w.isShowing()) {
                        return tip.cast(w);
                    }
                }
                return null;
            });
            if (prozor != null) {
                return prozor;
            }
            spavaj(10);
        }
        fail("Nije se otvorio prozor " + tip.getSimpleName() + ".");
        return null;
    }

    public static void cekajDaSeZatvori(Window prozor) {
        long kraj = System.currentTimeMillis() + CEKANJE_MS;
        while (saEdt(prozor::isShowing)) {
            if (System.currentTimeMillis() > kraj) {
                fail("Prozor " + prozor.getClass().getSimpleName() + " se nije zatvorio.");
            }
            spavaj(10);
        }
        sacekajEdt();
    }

    public static boolean otvoren(Class<? extends Window> tip) {
        return saEdt(() -> {
            for (Window w : Window.getWindows()) {
                if (tip.isInstance(w) && w.isShowing()) {
                    return true;
                }
            }
            return false;
        });
    }

    public static void zatvoriSve() {
        naEdt(() -> {
            for (Window w : Window.getWindows()) {
                w.dispose();
            }
        });
        zaustaviPozadinskeNiti();
    }

    public static void zaustaviPozadinskeNiti() {
        for (Map.Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()) {
            for (StackTraceElement el : e.getValue()) {
                if (el.getClassName().startsWith("models.TableModel") && el.getMethodName().equals("run")) {
                    e.getKey().interrupt();
                    try {
                        e.getKey().join(5000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    break;
                }
            }
        }
    }

    public static void pustiTaster(JTextField polje) {
        KeyEvent dogadjaj = new KeyEvent(polje, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0,
                KeyEvent.VK_A, 'a');
        for (KeyListener l : polje.getKeyListeners()) {
            l.keyReleased(dogadjaj);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T polje(Object objekat, String ime) {
        Class<?> klasa = objekat.getClass();
        while (klasa != null) {
            try {
                Field f = klasa.getDeclaredField(ime);
                f.setAccessible(true);
                return (T) f.get(objekat);
            } catch (NoSuchFieldException ex) {
                klasa = klasa.getSuperclass();
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
        throw new IllegalArgumentException("Nema polja " + ime + " u " + objekat.getClass().getName());
    }

    public static void postaviPolje(Object objekat, String ime, Object vrednost) {
        Class<?> klasa = objekat.getClass();
        while (klasa != null) {
            try {
                Field f = klasa.getDeclaredField(ime);
                f.setAccessible(true);
                f.set(objekat, vrednost);
                return;
            } catch (NoSuchFieldException ex) {
                klasa = klasa.getSuperclass();
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
        throw new IllegalArgumentException("Nema polja " + ime + " u " + objekat.getClass().getName());
    }

    public static <T extends Component> T nadjiKomponentu(Container kontejner, Class<T> tip) {
        for (Component c : kontejner.getComponents()) {
            if (tip.isInstance(c)) {
                return tip.cast(c);
            }
            if (c instanceof Container) {
                T unutra = nadjiKomponentu((Container) c, tip);
                if (unutra != null) {
                    return unutra;
                }
            }
        }
        return null;
    }

    public static void spavaj(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
