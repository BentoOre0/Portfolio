function resetRiders() {
  const ss = SpreadsheetApp.getActiveSpreadsheet();
  const ridersSheet = ss.getSheetByName("Riders");

  const lastRow = ridersSheet.getLastRow();

  if (lastRow >= 2) {
    // Reset column G (presence/status) to 0
    const colG = ridersSheet.getRange(2, 7, lastRow - 1, 1); // col G
    colG.setValue(0);

    // Clear column D (assignments)
    const colD = ridersSheet.getRange(2, 4, lastRow - 1, 1); // col D
    colD.clearContent();
  }

  const props = PropertiesService.getScriptProperties();
  clearQueue();
  Logger.log("Queue and presence cleared.");

  // Re-run status update to rebuild queue
  updateRiderStatus();

  // Print the fresh queue
  printQueue();
}
