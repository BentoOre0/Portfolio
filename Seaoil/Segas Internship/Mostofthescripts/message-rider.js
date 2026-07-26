function checkMessages() {
  //THIS WILL BE SET TO TIME BASED SO BE CAREFUL NOT TO START IT MAY SPAM
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var dispatchSheet = ss.getSheetByName('Dispatching & Fulfillment');

  // Get all rows (starting from row 2 to skip header)
  var lastRow = dispatchSheet.getLastRow();
  if (lastRow < 2) return; // no data

  var data = dispatchSheet.getRange(2, 1, lastRow - 1, 15).getValues();

  for (var i = 0; i < data.length; i++) {
    var rowNumber = i + 2; // actual sheet row (since data starts at row 2)
    var riderName = data[i][10];
    var confirmation = data[i][11];
    var orderstatus = data[i][14];
    if (riderName && !confirmation && !orderstatus) {
      //PASSED is dummy value
      Logger.log('Row ' + rowNumber + ' is unprocessed. Sending Viber...');
      sendConfirmationViberMessageForRow(rowNumber);
    }
  }
}
