package com.quadruplea.bocce;

import com.quadruplea.bocce.util.RandomizedSet;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RandomizedMatches {

    private int numTeams;
    private int numGames;
    private List<Team> allTeams = null;


    public RandomizedMatches(int numTeams, int numGames) {
        this.numTeams = numTeams;
        this.numGames = numGames;
        resetTeams();
    }

    private void resetTeams() {
        allTeams = new LinkedList<>();

        for (int i = 1; i <= numTeams; i++) {
            allTeams.add(new Team(i, numGames));
        }
    }

    public boolean randomize() {

        LinkedList <Team> unassignableTeams = new LinkedList<>();

        resetTeams();
        for (int curGame = 1; curGame <= numGames; curGame++) {

            Team unassignableTeam = null;

            try {
                unassignableTeam = assignGames(allTeams, curGame);
            } catch (IllegalStateException ise) {
                // try again
                curGame = 0;
                resetTeams();
                System.err.println("Try again 2");
                System.err.println(ise.getMessage());
                continue;
            }

            if (unassignableTeam != null) {
                if (unassignableTeams.contains(unassignableTeam)) {

                    System.err.println("I tried, but " + unassignableTeam.getName() + " cannot be assigned twice. Exiting!!");
                    resetTeams();
                    unassignableTeams.clear();
                    curGame = 0;
                    continue;
                }
                unassignableTeams.add(unassignableTeam);
            }
        }

        if (!unassignableTeams.isEmpty()) {
            try {
                if (assignGames(unassignableTeams, numGames) != null) {
                    throw new IllegalStateException("I tried, but unassignedTeams cannot be assigned in one shot. Giving up.");
                }
            } catch (IllegalStateException ise) {
                System.err.println("Try again 3");
                System.err.println(ise.getMessage());
                throw ise;
            }
        }

        // final check

        for (Team t : allTeams) {
            if (t.getNumGamesSet() < numGames) {
                System.err.println("Not all teams are set. Try again");
                return false;
            }
        }

        return true; // success
    }

    private Team assignGames(List<Team> teams, int curGame) throws IllegalStateException {
        LinkedHashSet<Team> curSet = new LinkedHashSet<>(teams);

        while (!curSet.isEmpty()) {
            Team curTeam = null;
            Team opposingTeam = null;

            if (curSet.size() == 1) {
                return curSet.removeFirst();
            } else if (curSet.size() == 2) {
                curTeam = curSet.getFirst();
                opposingTeam = curSet.getLast();

                if (!curTeam.canCompete(opposingTeam)) {
                    throw new IllegalStateException("The remaining two teams can't play against each other");
                }
            } else {

                curTeam = RandomizedSet.getRandomSetElement(curSet, curGame);
                if (curTeam == null || curTeam.getNumGamesSet() == curGame) {
                    continue;
                }

                Team previousTeam = null;
                do {
                    opposingTeam = RandomizedSet.getRandomSetElement(curSet, curGame);

                    if (opposingTeam == previousTeam) {
                        throw new IllegalStateException("Can't find opposingTeam from remaining list");
                    }
                    previousTeam = opposingTeam;
                } while (opposingTeam == null || !curTeam.canCompete(opposingTeam));
            }

            curTeam.addOpposingTeam(opposingTeam.getTeamNum());
            opposingTeam.addOpposingTeam(curTeam.getTeamNum());

            curSet.remove(curTeam);
            curSet.remove(opposingTeam);
        }

        return null;
    }

    public List<Team> getAllTeams() {
        return allTeams;
    }

    public Map<String, Team> getTeamsMap() {

        return allTeams.stream().collect(Collectors.toMap(Team::getName, team->team));
    }

    public String toString() {
        String s = "RandomizedMatches:\n";
        for (Team t : allTeams) {
            s += t + "\n";
        }
        return s;
    }
}
