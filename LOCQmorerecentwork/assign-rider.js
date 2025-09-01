/*
When a row is added a rider is assigned and a message is sent if the following conditions are held:
1. No Rider Assigned
2. The following are not blank Customer Name,	Contact No.,Address	Item, Description, Qty 

*/

function safeTrim(v) {
  return (v == null ? '' : String(v)).trim();
}

function getUnassignedRows() {
  const sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(
    'Dispatching & Fulfillment'
  );
  const data = sheet.getDataRange().getDisplayValues();

  for (let i = 1; i < data.length; i++) {
    const row = data[i];
    const riderName = row[10];
    const cell = sheet.getRange(i + 1, 11);

    const allFilledAtoE = row.slice(0, 7).every((val) => safeTrim(val) !== '');
    if (safeTrim(riderName) === '' ) {
      if(allFilledAtoE){
        const rider = assignNextAvailableRider();
        cell.setValue(rider);
      }
      
    }
  }
}

/*
This function is for future use when you want to trigger on a new row only
But I am not sure when to update because we will be checking entire sheet over and over for updates and
un happy path???
*/
function checkForNewRows() {
  const sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(
    'Dispatching & Fulfillment'
  );
  const lastRow = sheet.getLastRow();
  Logger.log('Last Row ' + lastRow);
  const lastChecked = parseInt(
    PropertiesService.getScriptProperties().getProperty('lastCheckedRow') || '1'
  );
  Logger.log('Last Checked ' + lastChecked);

  if (lastRow > lastChecked) {
    const rowValues = sheet.getRange(lastRow, 1, 1, 11).getDisplayValues()[0]; // A → I
    const cell = sheet.getRange(lastRow, 11);
    const value = rowValues[10]; // column I
    const allFilledAtoE = rowValues.slice(0, 7).every((val) => safeTrim(val) !== ''); // check A–E filled

    if (safeTrim(value) === '' && allFilledAtoE) {
      Logger.log('No rider detected, need to assign');
      const rider = assignNextAvailableRider();
      cell.setValue(rider);
    } else {
      Logger.log('Rider ' + value);
    }

    PropertiesService.getScriptProperties().setProperty(
      'lastCheckedRow',
      lastRow
    );
  }
}

function assignNextAvailableRider() {
  /*
  Rider assignment works as a queue First In First Out
  The moment it is found it is removed from the queue
  NOTE status of rider on Rider sheet is still "Available" UNTIL message-rider.gs is called and a message is sent
  this will change status to waiting for reply
  */
  const riderSheet =
    SpreadsheetApp.getActiveSpreadsheet().getSheetByName('Riders');
  const data = riderSheet.getDataRange().getDisplayValues();

  const header = data[0];
  const riderNameIndex = header.indexOf('Rider Name');
  const statusIndex = header.indexOf('Status');

  const props = PropertiesService.getScriptProperties();
  let queue = JSON.parse(props.getProperty('queue') || '[]');
  let presence = JSON.parse(props.getProperty('presence') || '{}');

  let assigned = null;

  while (queue.length > 0) {
    // FIFO
    const next = queue.shift();
    const sheetRow = next.index + 1;
    const name = next.name;

    // Check current status in sheet and presence flag
    const statusRaw = riderSheet.getRange(sheetRow, statusIndex + 1).getDisplayValue();
    const status = safeTrim(statusRaw).toLowerCase();
    const isPresent = !!presence[name];

    if (isPresent && status === 'available') {
      assigned = name;
      // Mark consumed so they can be re-added later by updateRiderStatus when available again
      delete presence[name];
      break;
    }

    // Not assignable right now; clear presence so updateRiderStatus can re-queue if/when they become available
    delete presence[name];
  }

  props.setProperty('queue', JSON.stringify(queue));
  props.setProperty('presence', JSON.stringify(presence));

  if (!assigned) {
    Logger.log('No available riders in queue.');
    return null;
  }

  Logger.log('Assigned Rider: ' + assigned);
  return assigned;
}
