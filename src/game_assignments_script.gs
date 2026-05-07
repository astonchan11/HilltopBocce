/**
 * Hilltop Bocceball 2026 — Random Game Assignment Generator
 * Paste this entire file into Google Apps Script (Extensions → Apps Script)
 * Then click Run → generateBocceSchedule
 *
 * Reads: Setup!B5  (number of teams)
 *        Setup!B6  (games per team — should be 8)
 * Writes: "Game Assignments" tab
 *
 * ⚠ Running this again WILL overwrite the existing schedule.
 */

function generateBocceSchedule() {
  var ss           = SpreadsheetApp.getActiveSpreadsheet();
  var setupSheet   = ss.getSheetByName("Setup");
  var assignSheet  = ss.getSheetByName("Game Assignments");
  var teamsSheet   = ss.getSheetByName("Teams Info");

  if (!setupSheet || !assignSheet || !teamsSheet) {
    SpreadsheetApp.getUi().alert(
      "ERROR: Missing required sheet(s).\n" +
      "Make sure sheets named 'Setup', 'Game Assignments', and 'Teams Info' all exist."
    );
    return;
  }

  var numTeams      = parseInt(setupSheet.getRange("B5").getValue());
  var gamesPerTeam  = parseInt(setupSheet.getRange("B6").getValue());

  if (isNaN(numTeams) || numTeams < 2) {
    SpreadsheetApp.getUi().alert("ERROR: 'Number of Teams' (Setup B5) must be a number ≥ 2.");
    return;
  }
  if (isNaN(gamesPerTeam) || gamesPerTeam < 1) {
    SpreadsheetApp.getUi().alert("ERROR: 'Games Per Team' (Setup B6) must be a number ≥ 1.");
    return;
  }

  // Read team names from Teams Info col A (rows 3 to 3+numTeams-1)
  var teamNames = [];
  var rowPointer = 0;

  // always advance rowPointer, but advance numTeamsProcessed only if it is not empty
  for (var numTeamsProcessed = 0; numTeamsProcessed < numTeams; rowPointer++) {
    var name = teamsSheet.getRange(rowPointer + 3, 1).getValue();
    if (typeof name === 'string' && name.trim() === "") {
      // The value consists only of whitespace (spaces, tabs, newlines). This can
      // happen because the cell is merged and hence, there will be empty cells
      // Nothing is processed, try the next row
      continue;
    }
    teamNames.push(name || ("Team " + (numTeamsProcessed + 1)));
    numTeamsProcessed++;
  }

  // Build all unique matchups
  var allMatchups = [];
  for (var a = 0; a < numTeams; a++) {
    for (var b = a + 1; b < numTeams; b++) {
      allMatchups.push([a, b]);
    }
  }

  var maxAttempts = 500;
  var schedule    = null;

  for (var attempt = 0; attempt < maxAttempts; attempt++) {
    // Fisher-Yates shuffle
    var matchups = allMatchups.slice();
    for (var i = matchups.length - 1; i > 0; i--) {
      var j   = Math.floor(Math.random() * (i + 1));
      var tmp = matchups[i]; matchups[i] = matchups[j]; matchups[j] = tmp;
    }

    // Greedy assignment
    var gamesCount = new Array(numTeams).fill(0);
    var assigned   = [];

    for (var m = 0; m < matchups.length; m++) {
      var ta = matchups[m][0], tb = matchups[m][1];
      if (gamesCount[ta] < gamesPerTeam && gamesCount[tb] < gamesPerTeam) {
        assigned.push([ta, tb]);
        gamesCount[ta]++;
        gamesCount[tb]++;
      }
    }

    // Verify all teams have exactly gamesPerTeam
    var valid = true;
    for (var t = 0; t < numTeams; t++) {
      if (gamesCount[t] !== gamesPerTeam) { valid = false; break; }
    }

    if (valid) { schedule = assigned; break; }
  }

  if (!schedule) {
    SpreadsheetApp.getUi().alert(
      "ERROR: Could not generate a valid schedule after " + maxAttempts + " attempts.\n" +
      "Try a different number of teams or games per team."
    );
    return;
  }

  // Build per-team opponent lists
  var teamSchedule = [];
  for (var t = 0; t < numTeams; t++) teamSchedule.push([]);

  for (var g = 0; g < schedule.length; g++) {
    var ta = schedule[g][0], tb = schedule[g][1];
    teamSchedule[ta].push(teamNames[tb]);
    teamSchedule[tb].push(teamNames[ta]);
  }

  // Write to Game Assignments tab
  // Row 3 = first data row (rows 1-2 are headers)
  var dataStartRow = 3;

  for (var t = 0; t < numTeams; t++) {
    var row = dataStartRow + t;
    // Col A: team name (col 1)
    assignSheet.getRange(row, 1).setValue(teamNames[t]);
    // Cols B-I: opponents for games 1-8
    for (var g = 0; g < gamesPerTeam; g++) {
      var opp = (g < teamSchedule[t].length) ? teamSchedule[t][g] : "";
      assignSheet.getRange(row, g + 2).setValue(opp);
    }
  }

  // Clear any leftover rows below (in case numTeams decreased)
  var lastRow = assignSheet.getLastRow();
  if (lastRow >= dataStartRow + numTeams) {
    var clearStart = dataStartRow + numTeams;
    var clearCount = lastRow - clearStart + 1;
    if (clearCount > 0) {
      assignSheet.getRange(clearStart, 1, clearCount, gamesPerTeam + 1).clearContent();
    }
  }

  SpreadsheetApp.getUi().alert(
    "✅ Schedule generated successfully!\n" +
    numTeams + " teams × " + gamesPerTeam + " games each.\n" +
    "Total matches scheduled: " + schedule.length
  );
}