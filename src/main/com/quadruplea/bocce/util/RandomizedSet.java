package com.quadruplea.bocce.util;

import com.quadruplea.bocce.Team;

import java.util.Random;
import java.util.Set;

public class RandomizedSet {

    public static Team getRandomSetElement(Set<Team> set, int curGameNum) {

        Team retTeam = null;
        for (int i = 0; i < set.size(); i++) {
            retTeam = set.stream().skip(new Random().nextInt(set.size())).findFirst().orElse(null);
            if (retTeam.getNumGamesSet() < curGameNum) {
                return retTeam;
            }
        }
        return null;
    }
}
