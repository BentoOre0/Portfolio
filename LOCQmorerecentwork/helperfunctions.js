function SendViaViber(phoneNumber, message) {
  var viberApiUser = "admin"; 
  var viberApiPass = "secret"; 
  var authString = Utilities.base64Encode(viberApiUser + ":" + viberApiPass); 

  // Replace line breaks and encode message
  var processedMessage = message.replace(/\n/g, '\u2028'); 
  var encodedMessage = encodeURIComponent(processedMessage);

  var viberUrl = "https://dev.api.jpci.io/ms-communication/viber/send"
               + "?mobileNumber=" + phoneNumber 
               + "&message=" + encodedMessage;

  var options = {
    method: "get",
    headers: { "Authorization": "Basic " + authString },
    muteHttpExceptions: true
  };

  var response = UrlFetchApp.fetch(viberUrl, options);
  var statusCode = response.getResponseCode();

  Logger.log("Sent to " + phoneNumber);
  Logger.log("Status: " + statusCode);
  Logger.log("Body: " + response.getContentText());

  return {
    status: statusCode,
    body: response.getContentText()
  };
}

function getRiderPhoneNumber(riderName) {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var ridersSheet = ss.getSheetByName("Riders");
  var riderData = ridersSheet.getDataRange().getValues();

  for (var j = 1; j < riderData.length; j++) {
    if (riderData[j][0] === riderName) { // col A match
      return riderData[j][4]; // col E (Rider phone)
    }
  }
  return null; // not found
}

function getRiderTLPhoneNumber(riderName) {
  var ss = SpreadsheetApp.getActiveSpreadsheet();
  var ridersSheet = ss.getSheetByName("Riders");
  var riderData = ridersSheet.getDataRange().getValues();

  for (var j = 1; j < riderData.length; j++) {
    if (riderData[j][0] === riderName) { // col A match
      return riderData[j][5]; // col F (TL phone)
    }
  }
  return null; // not found
}


function clearQueue() {
  const props = PropertiesService.getScriptProperties();
  props.deleteProperty("queue");
  props.deleteProperty("presence");
}

function printQueue() {
  const props = PropertiesService.getScriptProperties();
  const queue = JSON.parse(props.getProperty("queue") || "[]");

  if (queue.length === 0) {
    Logger.log("Queue is empty.");
    return;
  }

  Logger.log("Current Rider Queue:");
  queue.forEach((entry, i) => {
    Logger.log(`${i + 1}. Row ${entry.index + 1} - ${entry.name}`);
  });
}

