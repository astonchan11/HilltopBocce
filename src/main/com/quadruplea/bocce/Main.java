package com.quadruplea.bocce;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Please run with <numTeams> <numGames> options");
            System.exit(1);
        }

        int numTeams = 32;
        int numGames = 8;
        try {
            numTeams = Integer.parseInt(args[0]);
            numGames = Integer.parseInt(args[1]);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }

        RandomizedMatches randomizeMatches = new RandomizedMatches(numTeams, numGames);

        boolean randomizeSucceeded = false;
        do {
            try {
                randomizeSucceeded = randomizeMatches.randomize();
            } catch (IllegalStateException ise) {
                // try again
                System.err.println("Try again 1");
                randomizeSucceeded = false;
            }
        } while (!randomizeSucceeded);

        System.out.println(randomizeMatches);

        Map<String, Team> results = randomizeMatches.getTeamsMap();
        try (FileWriter fw = new FileWriter(new File("output.csv"))) {

            fw.write("2025 Bocceball Play Schedule:\n");

            Iterator<Team> curTeamIter = randomizeMatches.getAllTeams().listIterator();
            while (curTeamIter.hasNext()) {
                Team team = curTeamIter.next();
                fw.write(team.getName());
                fw.write(" ,");

                for (Iterator<String> iter = team.getOpposingTeams().iterator()
                     ; iter.hasNext(); iter.hasNext()) {
                    fw.write(iter.next());
                    if (iter.hasNext())
                        fw.write(",");
                }

                fw.write("\n");
            }

            fw.flush();
        }

    }
}
