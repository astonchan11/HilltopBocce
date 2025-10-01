package com.quadruplea.bocce;

import com.sun.source.tree.Tree;

import java.util.*;

public class Team {
    private String name;
    private int teamNum;
    private SortedSet<String> opposingTeams;
    private int numGamesToPlay;

    public Team(int teamNum, int numGamesToPlay) {
        this.name = "Team " + teamNum;
        this.teamNum = teamNum;
        opposingTeams = new TreeSet<>();
        this.numGamesToPlay = numGamesToPlay;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Team{" +
                "name='" + name + '\'' +
                ", teamNum = " + teamNum +
                ", opposingTeams=" + Arrays.toString(opposingTeams.toArray()) +
                ", numGamesSet=" + opposingTeams.size() +
                '}';
    }

    public int getNumGamesSet() {
        return opposingTeams.size();
    }

    public int getTeamNum() {
        return teamNum;
    }

    public boolean canCompete(Team opposingTeam) {
        int opposingTeamNum = opposingTeam.getTeamNum();

        // competing against itself
        if (opposingTeamNum == teamNum) {
            return false;
        }

        String opposingTeamName = "Team " + opposingTeamNum;

        if (opposingTeams.contains(opposingTeamName)) {
            return false;
        }

        return (opposingTeams.size() != numGamesToPlay);
    }

    public Set<String> getOpposingTeams() {
        return opposingTeams;
    }

    public void addOpposingTeam(int opposingTeamNum) {
        opposingTeams.add("Team " + opposingTeamNum);
    }
}
