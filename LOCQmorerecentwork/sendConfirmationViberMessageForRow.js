function sendConfirmationViberMessageForRow(rowNumber) {
  /*
  
  Sends a Viber message only if both Rider Confirmation (col J) 
  and Order Status (col M) are blank.
  If sent successfully, it updates:
    - col J to "Waiting..."
    - col M to "Preparing Order"
    - col G to the current time
  
  */

  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var dispatchSheet = ss.getSheetByName('Dispatching & Fulfillment');
  var ridersSheet = ss.getSheetByName('Riders');

  // Read values from the row in Dispatching & Fulfillment
  var rowValues = dispatchSheet.getRange(rowNumber, 1, 1, 13).getValues()[0];
  var orderMessage = rowValues[7]; // Column F (index 5, since A=0)
  var riderName = rowValues[10]; // Column I (index 8)
  var colJ = rowValues[11]; // Column J
  var colK = rowValues[12]; // Column K
  var colM = rowValues[14]; // Column M (index 12, since A=0)

  // Ensure rider assigned and J & M are empty
  if (!riderName || colJ || colM) {
    Logger.log('Row ' + rowNumber + ' skipped: rider missing or J/M not empty');
    return;
  }

  // Search Riders sheet for phone number
  var ridersData = ridersSheet
    .getRange(2, 1, ridersSheet.getLastRow() - 1, 5)
    .getValues();
  var phoneNumber = null;
  var riderRow = null;
  for (var i = 0; i < ridersData.length; i++) {
    if (ridersData[i][0].trim() === riderName.trim()) {
      // Col A = Rider Name
      phoneNumber = ridersData[i][4]; // Col E = Phone
      riderRow = i + 2; // Actual row in sheet (offset for header)
      break;
    }
  }
  if (!phoneNumber) {
    Logger.log('No phone number found for rider: ' + riderName);
    dispatchSheet.getRange(rowNumber, 10).setValue('ERROR'); // J
    return;
  }
  var result = SendViaViber(phoneNumber, orderMessage);
  Logger.log(orderMessage);
  if (result.status === 200) {
    dispatchSheet.getRange(rowNumber, 12).setValue('Waiting...'); // Column J
    dispatchSheet.getRange(rowNumber, 15).setValue('Preparing Order'); // Column M
    dispatchSheet.getRange(rowNumber, 9).setValue(new Date());
    dispatchSheet.getRange(rowNumber, 9).setNumberFormat('MM/dd/yyyy HH:mm:ss'); // Column G (time sent)
    if (riderRow) {
      ridersSheet.getRange(riderRow, 4).setValue('Awaiting reply'); // Column D
    }
  } else {
    dispatchSheet.getRange(rowNumber, 12).setValue('ERROR'); // Column J
    if (riderRow) {
      ridersSheet.getRange(riderRow, 4).setValue('ERROR'); // Column D
    }
  }
}
