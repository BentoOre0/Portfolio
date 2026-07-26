function checkStaleOrders() {
  /*
  StaleOrders are Orders that Rider is delivering and has taken longer than the wanted time (1hr) 
  */
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var dispatchSheet = ss.getSheetByName("Dispatching & Fulfillment");
  var dispatchData = dispatchSheet.getDataRange().getValues();
  var now = new Date();

  for (var i = 1; i < dispatchData.length; i++) { // skip header
    var row = dispatchData[i];
    var customerName = row[2];   // col C
    var address      = row[4];   // col E
    var assignTime   = row[8];   // col I
    var riderName    = row[10];  // col K
    var orderStatus  = row[14];  // col O
    var alerted      = row[21];  // col V (flag: alerted?)

    // ✅ Condition: has assignTime, not yet alerted, and rider status = "Out for Delivery"
    if (assignTime && alerted !== "Y" && orderStatus === "Out for Delivery") {
      var diffMs = now - new Date(assignTime);
      var diffHours = diffMs / (1000 * 60 * 60);
      Logger.log(assignTime);
      Logger.log(diffHours);

      if (diffHours > 1) {
        Logger.log("Order is taking too long");

        // Mark as alerted in col V
        dispatchSheet.getRange(i + 1, 22).setValue("Y"); // col V (1-based)

        // Update status to "With Rider Issue"
        dispatchSheet.getRange(i + 1, 15).setValue("With Rider Issue"); // col O

        // Build TL alert message
        var message = "Order for " + customerName + " at " + address +
                      " has not been delivered within 1 hour since rider assignment. " +
                      "Please check status with the rider and advise on next steps.";

        // ✅ Use helper for TL phone lookup
        var tlPhone = getRiderTLPhoneNumber(riderName);
        if (tlPhone) {
          SendViaViber(tlPhone, message);
        }
      }
    }
  }
}
