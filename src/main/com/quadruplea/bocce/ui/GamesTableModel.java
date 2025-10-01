package com.quadruplea.bocce.ui;

import com.quadruplea.bocce.Team;

import javax.swing.table.AbstractTableModel;
import java.util.Map;
import java.util.Optional;

public class GamesTableModel extends AbstractTableModel {

    Map<String, Team> matches = null;

    public void setTableModel(Map<String, Team> matches) {
        this.matches = matches;
        this.fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        if (matches == null)
            return 0;
        else
            return matches.size();
    }

    @Override
    public int getColumnCount() {
        if (matches == null || matches.isEmpty())
            return 0;

        Optional<Team> team = matches.values().stream().findFirst();
        return team.get().getOpposingTeams().size() + 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        if (matches == null || matches.isEmpty())
            return null;

        int teamNum = rowIndex + 1;
        String keyTeamName = "Team " + teamNum;

        if (columnIndex == 0) {
            return keyTeamName;
        }

        Team team = matches.get(keyTeamName);
        Object val = null;

        try {
            val = team.getOpposingTeams().toArray()[columnIndex - 1];
        } catch (Exception ee) {
            System.err.println(ee.getMessage());
        }

        return val;

//        return opposingTeam.getOpposingTeams().stream().toList().get(columnIndex);
    }
}
