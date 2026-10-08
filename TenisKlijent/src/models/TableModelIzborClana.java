package models;

import domain.Clan;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

public class TableModelIzborClana extends AbstractTableModel {

    private ArrayList<Clan> lista = new ArrayList<>();
    private final String[] kolone = {"ID", "Ime", "Prezime", "Godine", "Email", "Telefon"};

    public void setLista(ArrayList<Clan> lista) {
        this.lista = lista != null ? lista : new ArrayList<>();
        fireTableDataChanged();
    }

    public ArrayList<Clan> getLista() {
        return lista;
    }

    public Clan getClan(int row) {
        return lista.get(row);
    }

    @Override
    public int getRowCount() {
        return lista.size();
    }

    @Override
    public int getColumnCount() {
        return kolone.length;
    }

    @Override
    public String getColumnName(int column) {
        return kolone[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        Clan c = lista.get(row);
        switch (column) {
            case 0:
                return c.getClanID();
            case 1:
                return c.getIme();
            case 2:
                return c.getPrezime();
            case 3:
                return c.getGodine();
            case 4:
                return c.getEmail();
            case 5:
                return c.getTelefon();
            default:
                return null;
        }
    }
}
