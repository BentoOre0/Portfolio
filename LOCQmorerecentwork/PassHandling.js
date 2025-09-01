function PassHandling() {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var dispatchSheet = ss.getSheetByName("Dispatching & Fulfillment");
  var dispatchData = dispatchSheet.getDataRange().getValues();

  for (var i = 1; i < dispatchData.length; i++) { // skip header
    var row = dispatchData[i];
    var orderStatus = row[12];   // col M
    var totalPasses = row[21];   // col V
    var rider_name = row[8]; // col I
    var TL_alerted = row[22];    // col W
    
    if (orderStatus === "PASSED..." || orderStatus === "AUTOPASS: No Rider Available") {
      Logger.log("Hi");
      if (totalPasses >= 3 || checkindividualpasses(rider_name) > 1) {
        dispatchSheet.getRange(i + 1, 13).setValue("Rider Assignment Problem");
        if(TL_alerted === "N"){
          checkTooManyPassForRow(i + 1);
        }
      } else {
        // ✅ Otherwise, normal pass → clear row and reassign
        clearRow(dispatchSheet, i + 1);
      }
    }
  }
}


/**
 * Checks a specific row for too many passes and alerts TL if needed
 */
function checkTooManyPassForRow(rowIndex) {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var dispatchSheet = ss.getSheetByName("Dispatching & Fulfillment");
  var row = dispatchSheet.getRange(rowIndex, 1, 1, dispatchSheet.getLastColumn()).getValues()[0];

  var customerName = row[0];   // col A
  var address = row[2];        // col C
  var riderName = row[8];      // col I
  var totalPasses = row[21];   // col V
  var TL_alerted = row[22];    // col W

  // Get riderPasses from Riders sheet, Col G
  var riderPasses = checkindividualpasses(riderName);

  if ((totalPasses >= 3 || riderPasses > 1) && TL_alerted === "N") {
    // Update order status to error

    var message = "Maybunga station has no available riders. Order for " + customerName +
                  " at " + address + " can’t be fulfilled. Please check with riders or inform customer.";

    // Lookup TL phone number (helper fn you already have)
    var tlPhone = getRiderTLPhoneNumber(riderName);

    if (tlPhone) {
      var success = SendViaViber(tlPhone, message);
      if (success) {
        // Mark W = "Y" (TL alerted)
        dispatchSheet.getRange(rowIndex, 23).setValue("Y");
      }
    }
  }
}

function checkindividualpasses(rider_name) {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var ridersSheet = ss.getSheetByName("Riders");
  var ridersData = ridersSheet.getDataRange().getValues();

  for (var i = 1; i < ridersData.length; i++) { // skip header
    var row = ridersData[i];
    var riderNameInSheet = row[0]; // Col A (zero-based index 0)

    if (riderNameInSheet && riderNameInSheet.toString().trim() === rider_name.toString().trim()) {
      return row[6]; // Col G (zero-based index 6)
    }
  }

  // If not found, return 0
  return 0;
}

function clearRow(sheet, rowIndex) {
  // Clear content from col 9 (I) through col 15 (O)
  sheet.getRange(rowIndex, 9, 1, 7).clearContent();
  Logger.log("Row " + rowIndex + " cleared (I→O) for reassignment");
}