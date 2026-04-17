package com.quadruplea.bocce.ui;

import com.quadruplea.bocce.RandomizedMatches;
import com.quadruplea.bocce.Team;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainFrame {
    JFrame mainFrame;
    JTextField maxTeamsField;
    JTextField maxGamesField;
    JButton generateBtn;
    JTable resultsTable;
    GamesTableModel tableModel;

    MainFrame(){
        resultsTable = new JTable();
        tableModel = new GamesTableModel();
        resultsTable.setModel(tableModel);
        resultsTable.setAutoCreateRowSorter(true);

        mainFrame = new JFrame();//creating instance of JFrame
        JPanel paramPanel = new JPanel(new BorderLayout());
        JScrollPane mainPanel = new JScrollPane(resultsTable);
        JPanel buttonPanel = new JPanel();

        generateBtn = new JButton("Generate");//creating instance of JButton
        generateBtn.setBounds(130,100,100, 40);

        mainFrame.add(paramPanel, BorderLayout.NORTH);
        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.add(buttonPanel, BorderLayout.SOUTH);

        maxTeamsField = new JTextField();
        maxGamesField = new JTextField();
        maxTeamsField.setBounds(new Rectangle(5, 5, 100, 30));
        maxTeamsField.setPreferredSize(new Dimension(100, 25));
        maxGamesField.setBounds(new Rectangle(5, 5, 100, 30));
        maxGamesField.setPreferredSize(new Dimension(100, 25));

        paramPanel.setLayout(new BoxLayout(paramPanel, BoxLayout.LINE_AXIS));
        paramPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        paramPanel.add(Box.createHorizontalGlue());
        paramPanel.add(new JLabel("Number of teams: "));
        paramPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        paramPanel.add(maxTeamsField);

        paramPanel.add(Box.createHorizontalStrut(100));
        paramPanel.add(new JLabel("Number of games: "));
        paramPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        paramPanel.add(maxGamesField);

        paramPanel.add(Box.createHorizontalGlue());

        buttonPanel.add(generateBtn);

        mainFrame.setSize(400,300);//400 width and 500 height
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Rectangle bounds = gd.getDefaultConfiguration().getBounds();
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());

        Rectangle safeBounds = new Rectangle(bounds);
        safeBounds.x += insets.left;
        safeBounds.y += insets.top;
        safeBounds.width = 800;
        safeBounds.height = 700;

        mainFrame.setBounds(safeBounds);

        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);

        generateBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int numTeams = Integer.parseInt(maxTeamsField.getText());
                int numGames = Integer.parseInt(maxGamesField.getText());

                AtomicBoolean completed = new AtomicBoolean(true);

                do {
                    RandomizedMatches randomizeMatches = new RandomizedMatches(numTeams, numGames);
                    HashMap<String, Integer> validations = new HashMap<>();
                    boolean randomizeSucceeded;
                    do {
                        try {
                            randomizeSucceeded = randomizeMatches.randomize();
                        } catch (IllegalStateException ise) {
                            // try again
                            System.err.println("Try again A");
                            randomizeSucceeded = false;
                        }
                    } while (!randomizeSucceeded);

                    System.out.println(randomizeMatches);

                    FileWriter fw = null;

                    try {
                        fw = new FileWriter(new File("output.csv"));

                        fw.write("2025 Bocceball Play Schedule:\n");

                        Iterator<Team> curTeamIter = randomizeMatches.getAllTeams().listIterator();
                        while (curTeamIter.hasNext()) {
                            Team team = curTeamIter.next();
                            fw.write(team.getName());
                            fw.write(" ,");

                            // add element if not there yet
                            if (!validations.containsKey(team.getName())) {
                                validations.put(team.getName(), 0);
                            }

                            for (Iterator<String> iter = team.getOpposingTeams().iterator()
                                 ; iter.hasNext(); iter.hasNext()) {
                                String opposingTeam = iter.next();
                                fw.write(opposingTeam);

                                if (!validations.containsKey(opposingTeam)) {
                                    validations.put(opposingTeam, 0);
                                }

                                validations.compute(opposingTeam, (k, counts) -> counts + 1);
                                if (iter.hasNext())
                                    fw.write(",");
                            }

                            fw.write("\n");
                        }
                        fw.flush();
                    } catch (Exception exception) {
                        System.err.println(exception.getMessage());
                    } finally {
                        if (fw != null) {
                            try {
                                fw.close();
                            } catch (IOException ex) {
                                System.err.println(ex.getMessage());
                            }
                        }
                    }

                    // Make sure Validation won't have any oddities
                    validations.forEach((k, v) -> {
                        if (v != numGames) {
                            System.err.println("PLEASE CHECK " + k + ", v = " + v + "!!!!");

                            completed.set(true);
                            // throw new IllegalStateException("something wrong with the program");
                        }
                    });
                    tableModel.setTableModel(randomizeMatches.getTeamsMap());
                    tableModel.fireTableStructureChanged();
                } while (!completed.get());
            }
        });
    }

    public static void main(String[] args) {
//        System.setProperty("sun.java2d.uiScale.enabled", "true");
//        System.setProperty("sun.java2d.uiScale", "2.0");
        new MainFrame();
    }
}