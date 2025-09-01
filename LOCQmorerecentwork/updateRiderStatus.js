/*
Every time ran,
It will check which riders are within their shift if not given a special status
Any other non special status will be overwritten on the next call to unavailable or available
it checks for availability, adds them to persistent availability queue

*/

function updateRiderStatus() {
  const sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName('Riders');
  const rows = sheet.getDataRange().getDisplayValues();
  const now = new Date();

  Logger.log('running update rider status...');

  // Persistent storage
  const props = PropertiesService.getScriptProperties();
  let queue = JSON.parse(props.getProperty('queue') || '[]');
  let presence = JSON.parse(props.getProperty('presence') || '{}');

  for (let rowIndex = 1; rowIndex < rows.length; rowIndex++) {
    const [name, timeInStr, timeOutStr, currentStatus] = rows[rowIndex];
    const timeIn = parseTime(timeInStr, now);
    const timeOut = parseTime(timeOutStr, now);

    // Invalid check
    if (timeOut <= timeIn) {
      sheet.getRange(rowIndex + 1, 4).setValue('ERROR');
      presence[name] = false; // mark not present on invalid schedule
      continue;
    }
    if (currentStatus === 'Leave') {
      presence[name] = false; // not available -> presence false
      continue;
    }
    if (currentStatus === 'Inactive') {
      presence[name] = false; // not available -> presence false
      continue;
    }

    if (currentStatus === 'In Transit') {
      presence[name] = false; // not available -> presence false
      continue;
    }
    if (currentStatus === 'Awaiting reply') {
      presence[name] = false; // not available -> presence false
      continue;
    }

    // Default check based on schedule
    const newStatus =
      now >= timeIn && now <= timeOut ? 'Available' : 'Unavailable';
    sheet.getRange(rowIndex + 1, 4).setValue(newStatus);

    // Queue logic: if available and not in queue, add to queue and presence
    if (newStatus === 'Available' && !presence[name]) {
      queue.push({ index: rowIndex, name });
      presence[name] = true;
    } else if (newStatus !== 'Available') {
      presence[name] = false; // ensure presence is false for any non-Available status
    }
  }
  // Persist updated queue and presence
  props.setProperty('queue', JSON.stringify(queue));
  props.setProperty('presence', JSON.stringify(presence));
}

function parseTime(timeStr, referenceDate) {
  const [hours, minutes] = timeStr.toString().split(':').map(Number);
  const date = new Date(referenceDate);
  date.setHours(hours, minutes, 0, 0);
  return date;
}
