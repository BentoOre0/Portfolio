function autoPass() {
  /*
  AutoPass sets state to AUTO PASS: No Rider Available
  and clears the row,
  Full autoreassignment occurs when assign-rider is called next time.
  */
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var dispatchSheet = ss.getSheetByName("Dispatching & Fulfillment");
  var ridersSheet = ss.getSheetByName("Riders");

  var dispatchData = dispatchSheet.getDataRange().getValues();
  var riderData = ridersSheet.getDataRange().getValues();
  var now = new Date();

  for (var i = 1; i < dispatchData.length; i++) {
    var orderStatus   = dispatchData[i][14]; // col O
    var orderTime     = dispatchData[i][8];  // col I
    var riderName     = dispatchData[i][10]; // col K
    var customerName  = dispatchData[i][2];  // col C
    var address       = dispatchData[i][4];  // col E

    if (orderStatus === "Preparing Order" && orderTime) {
      var elapsed = (now - new Date(orderTime)) / 1000 / 60; // minutes elapsed
      Logger.log(elapsed);
      if (elapsed >= 10) {
        // find rider in Riders sheet
        var j = -1;
        for (var r = 1; r < riderData.length; r++) {
          if (riderData[r][0] === riderName) { // col A (Rider Name)
            j = r;
            break;
          }
        }

        if (j !== -1) {
          var riderNumber = riderData[j][4]; // col E
          var tlNumber = riderData[j][5];    // col F

          var riderMsg = "You did not respond within 10 minutes to the order for " +
            customerName + " at " + address +
            ", so it has been reassigned to another rider." + "\u2028" +  "Your status is now Inactive. Reply “Active” to update your status back to Available when you are ready to take orders.";

          var tlMsg = "Order for " + customerName + " at " + address +
            " was auto-reassigned after no response from Rider " + riderName +
            " within 10 minutes. Please manually update Rider " + riderName +
            " status back to Available.";

          SendViaViber(riderNumber, riderMsg);
          SendViaViber(tlNumber, tlMsg);

          // --- update Riders sheet ---
          ridersSheet.getRange(j + 1, 7).setValue(riderData[j][6] + 1); // col G (increment)
          ridersSheet.getRange(j + 1, 4).setValue("Inactive");          // col D

          // --- update Dispatch sheet ---
          const currentW = Number(dispatchData[i][22]) || 0; // col W (zero-based 22)
          dispatchSheet.getRange(i + 1, 23).setValue(currentW + 1); // col W (1-based 23)
          dispatchSheet.getRange(i + 1, 13).setValue("AUTOPASS: No Rider Available");  // col M
        }
      }
    }
  }
}
