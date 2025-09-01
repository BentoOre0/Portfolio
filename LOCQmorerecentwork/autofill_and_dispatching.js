/**************************************************************
 *  SEAGAS — Autofill + Dispatch (ONE FILE, dynamic dispatch)
 *  Key behavior updates:
 *   • NO header forcing in Dispatching & Fulfillment (no new/renamed columns)
 *   • Writes ONLY to columns that already exist (by header/alias)
 *   • Date is written to your existing Date column (wherever it is)
 *   • If Date column isn't found, it's skipped (no new column created)
 *   • Name clear: clears values but preserves formulas
 **************************************************************/

/***** FEATURE TOGGLE *****/
const ENABLE_DB_AUTOFILL = false; // set true to re-enable customer details autofill

/***** CONFIG *****/
const ORDER_SHEET       = 'Order-Taking';
const DISPATCH_SHEET    = 'Dispatching & Fulfillment';
const CUSTOMER_DB_SHEET = 'Customer Database';   // used only if ENABLE_DB_AUTOFILL = true

// SOF header aliases
const SOF_HEADER_ALIASES = ['SOF No.', 'SOF Number', 'SOF#', 'SOF'];

// Order-Taking headers (source)
const H_SOFO     = 'SOF No.';
const H_DATE     = 'Date';
const H_MONTH    = 'Month';
const H_WEEKNO   = 'Week No.';
const H_TIME     = 'Time';
const H_NAME     = 'Customer Name';
const H_CONTACT  = 'Contact Number';
const H_ADDRESS  = 'Address';
const H_ITEM     = 'Item Description';
const H_QTY      = 'Qty';
const H_DISPATCH = 'Dispatch Button';

const TZ = 'Asia/Manila';

/***** Dispatch header aliases (FLEXIBLE lookups; adjust if your labels differ) *****/
const D_TIME_ALIASES    = ['Time', 'Order Time', 'Dispatch Time', 'Timestamp'];
const D_NAME_ALIASES    = ['Customer Name', 'Name', 'Client Name'];
const D_CONTACT_ALIASES = ['Contact No.', 'Contact Number', 'Phone', 'Mobile'];
const D_ADDRESS_ALIASES = ['Address', 'Full Address', 'Customer Address'];
const D_ITEM_ALIASES    = ['Item Description', 'SKU', 'Product'];
const D_QTY_ALIASES     = ['Qty', 'Quantity', 'QTY'];
const D_MSG_ALIASES     = ['Order Message', 'Message'];
const D_DATE_ALIASES    = ['Date', 'Order Date', 'Delivery Date', 'Dispatch Date']; // we will write here if found

/***** (DB autofill aliases only matter if ENABLE_DB_AUTOFILL = true) *****/
const CDB_NAME_ALIASES        = ['Customer Name', 'Name', 'Customer'];
const CDB_ID_ALIASES          = ['Customer ID', 'ID', 'Cust ID'];
const CDB_CONTACT_ALIASES     = ['Contact Number', 'Contact No.', 'Mobile', 'Phone', 'Contact #', 'GCash Number'];
const CDB_ADDRESS_ALIASES     = ['Address', 'Full Address', 'Customer Address', 'Home Address'];
const CDB_SERVING_ALIASES     = ['Serving Plant/Station', 'Serving Plant/Station ', 'Serving Plant', 'Serving Station'];
const CDB_ISLAND_ALIASES      = ['Island'];
const CDB_REGION_ALIASES      = ['Region'];
const CDB_CITY_ALIASES        = ['City'];
const CDB_DISTRICT_ALIASES    = ['District'];
const CDB_BARANGAY_ALIASES    = ['Municipality/Barangay', 'Municipality / Barangay', 'Barangay', 'Brgy'];
const CDB_SUBSEG_ALIASES      = ['Subsegment', 'Market Subsegment', 'Sub-segment', 'Sub-segment'];
const CDB_ACCTACQ_ALIASES     = ['Acct Acquisition', 'Account Acquisition', 'Acquisition'];
const CDB_AO_ALIASES          = ['Account Officer/Suyod Agent', 'Account Officer', 'Suyod Agent'];

const ODR_ID_ALIASES          = ['Customer ID'];                 // H
const ODR_CONTACT_ALIASES     = [H_CONTACT];                     // J
const ODR_ADDRESS_ALIASES     = [H_ADDRESS];                     // K
const ODR_SERVING_ALIASES     = ['Serving Plant/Station'];       // L
const ODR_ISLAND_ALIASES      = ['Island'];                      // M
const ODR_REGION_ALIASES      = ['Region'];                      // N
const ODR_CITY_ALIASES        = ['City'];                        // O
const ODR_DISTRICT_ALIASES    = ['District'];                    // P
const ODR_BARANGAY_ALIASES    = ['Municipality/Barangay'];       // Q
const ODR_SUBSEG_ALIASES      = ['Subsegment'];                  // R
const ODR_ACCTACQ_ALIASES     = ['Acct Acquisition'];            // S
const ODR_AO_ALIASES          = ['Account Officer/Suyod Agent']; // T

/***** UTILITIES *****/
const norm    = s => String(s ?? '').replace(/\s+/g, ' ').trim();
const normKey = s => norm(s).toLowerCase();

function canonHdr(s) {
  return norm(String(s ?? ''))
    .replace(/^"+|"+$/g, '')
    .replace(/[^a-zA-Z0-9]+/g, '')
    .toLowerCase();
}

function headerIndexByName_(headers, nameOrAliases) {
  const aliases = Array.isArray(nameOrAliases) ? nameOrAliases : [nameOrAliases];
  const canonRow = headers.map(h => canonHdr(h));
  for (let a of aliases) {
    const target = canonHdr(a);
    const idx = canonRow.indexOf(target);
    if (idx !== -1) return idx + 1;
  }
  return 0;
}

function firstEmptyRowInCol_(sheet, col, startRow) {
  const lastRow = sheet.getLastRow();
  const maxRows = sheet.getMaxRows();
  const from = Math.max(startRow, 2);
  const num  = Math.max(maxRows - from + 1, 1);
  const vals = sheet.getRange(from, col, num, 1).getValues();
  const off  = vals.findIndex(r => norm(r[0]) === '');
  if (off !== -1) return from + off;
  return Math.max(lastRow + 1, from);
}

function dateKey_(v) {
  if (!v) return '';
  if (v instanceof Date) return Utilities.formatDate(v, TZ, 'yyyy-MM-dd');
  const tryDate = new Date(v);
  return isNaN(tryDate) ? norm(v) : Utilities.formatDate(tryDate, TZ, 'yyyy-MM-dd');
}

function timeKey_(v) {
  if (!v) return '';
  if (v instanceof Date) return Utilities.formatDate(v, TZ, 'HH:mm:ss');
  const t = new Date(v);
  return isNaN(t) ? norm(v) : Utilities.formatDate(t, TZ, 'HH:mm:ss');
}

function weekOfMonthLabelCappedFirstFullWeek_(date, weekStart = 1) {
  const d = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  d.setHours(0,0,0,0);
  const firstOfMonth = new Date(d.getFullYear(), d.getMonth(), 1);
  firstOfMonth.setHours(0,0,0,0);
  const firstDow = firstOfMonth.getDay();
  const advance = ((weekStart - firstDow + 7) % 7);
  const week1Start = new Date(firstOfMonth);
  week1Start.setDate(firstOfMonth.getDate() + advance);
  const daysDiff = d < week1Start ? 0 : Math.floor((d - week1Start) / 86400000);
  return `Wk${Math.min((d < week1Start ? 0 : Math.floor(daysDiff / 7)) + 1, 4)}`;
}

/***** SOF generator (dynamic reset) *****/
const SOF_PAD_WIDTH = 4;

function sofCol_(headers) {
  return headerIndexByName_(headers, SOF_HEADER_ALIASES);
}

function maxSuffixForYearInSheet_(sheet, headers, year) {
  const col = sofCol_(headers);
  if (!col) return 0;
  const last = sheet.getLastRow();
  if (last < 2) return 0;
  const vals = sheet.getRange(2, col, last - 1, 1).getValues().flat();
  let max = 0;
  const re = new RegExp('^' + year + '-(\\d+)$');
  for (const v of vals) {
    const s = String(v || '').trim();
    const m = s.match(re);
    if (m) {
      const n = parseInt(m[1], 10);
      if (!isNaN(n) && n > max) max = n;
    }
  }
  return max;
}

function sheetIsClearOfSOF_(sheet, headers) {
  const col = sofCol_(headers);
  if (!col) return true;
  const last = sheet.getLastRow();
  if (last < 2) return true;
  const vals = sheet.getRange(2, col, last - 1, 1).getValues().flat();
  return vals.every(v => String(v || '').trim() === '');
}

function sofPropKey_(year) {
  return `SOF_COUNTER_${year}`;
}

function pad_(n, width = SOF_PAD_WIDTH) {
  const s = String(n);
  return s.length >= width ? s : '0'.repeat(width - s.length) + s;
}

function syncSOFCounterWithSheet_(sheet, headers, year) {
  const sp  = PropertiesService.getScriptProperties();
  const key = sofPropKey_(year);
  if (sheetIsClearOfSOF_(sheet, headers)) {
    sp.setProperty(key, '0');       // reset if sheet is effectively clear
    return 0;
  }
  const maxFound = maxSuffixForYearInSheet_(sheet, headers, year);
  sp.setProperty(key, String(maxFound));
  return maxFound;
}

function getNextSOF_(sheet, headers, year) {
  const sp  = PropertiesService.getScriptProperties();
  const baseline = syncSOFCounterWithSheet_(sheet, headers, year);
  const next = baseline + 1;
  sp.setProperty(sofPropKey_(year), String(next));
  return `${year}-${pad_(next)}`;
}

function setSOFIfNeeded_(sheet, row, headers) {
  const cSOF = sofCol_(headers);
  if (!cSOF) return;
  const rngSOF = sheet.getRange(row, cSOF);
  const current = String(rngSOF.getValue() || '').trim();
  if (current) return;

  const dateCol = headerIndexByName_(headers, H_DATE);
  const baseDate = dateCol ? sheet.getRange(row, dateCol).getValue() : null;
  const dt = baseDate instanceof Date ? baseDate : new Date();
  const year = parseInt(Utilities.formatDate(dt, TZ, 'yyyy'), 10) || 2025;

  rngSOF.setNumberFormat('@');
  rngSOF.setValue(getNextSOF_(sheet, headers, year));
}

/***** (DISABLED) Customer DB lookup + fill *****/
function lookupCustomerFromDB_(nameRaw) {
  const result = {
    id: '', phone: '', address: '',
    serving: '', island: '', region: '', city: '', district: '',
    barangay: '', subsegment: '', acctAcq: '', accountOfficer: ''
  };
  if (!ENABLE_DB_AUTOFILL) return result;

  const ss = SpreadsheetApp.getActive();
  const db = ss.getSheetByName(CUSTOMER_DB_SHEET);
  if (!db) return result;

  const lastRow = db.getLastRow();
  const lastCol = db.getLastColumn();
  if (lastRow < 2) return result;

  const headers = db.getRange(1, 1, 1, lastCol).getValues()[0];
  const cName   = headerIndexByName_(headers, CDB_NAME_ALIASES);
  if (!cName) return result;

  const cID     = headerIndexByName_(headers, CDB_ID_ALIASES);
  const cPhone  = headerIndexByName_(headers, CDB_CONTACT_ALIASES);
  const cAddr   = headerIndexByName_(headers, CDB_ADDRESS_ALIASES);
  const cSrv    = headerIndexByName_(headers, CDB_SERVING_ALIASES);
  const cIsl    = headerIndexByName_(headers, CDB_ISLAND_ALIASES);
  const cReg    = headerIndexByName_(headers, CDB_REGION_ALIASES);
  const cCity   = headerIndexByName_(headers, CDB_CITY_ALIASES);
  const cDist   = headerIndexByName_(headers, CDB_DISTRICT_ALIASES);
  const cBrgy   = headerIndexByName_(headers, CDB_BARANGAY_ALIASES);
  const cSub    = headerIndexByName_(headers, CDB_SUBSEG_ALIASES);
  const cAcq    = headerIndexByName_(headers, CDB_ACCTACQ_ALIASES);
  const cAO     = headerIndexByName_(headers, CDB_AO_ALIASES);

  const names = db.getRange(2, cName, lastRow - 1, 1).getValues();
  const want = normKey(nameRaw);
  if (!want) return result;

  let rowIdx = -1;
  for (let i = 0; i < names.length; i++) {
    if (normKey(names[i][0]) === want) { rowIdx = i; break; }
  }
  if (rowIdx === -1) {
    for (let i = 0; i < names.length; i++) {
      const nk = normKey(names[i][0]);
      if (nk.startsWith(want) || want.startsWith(nk)) { rowIdx = i; break; }
    }
  }
  if (rowIdx === -1) return result;

  const get = (col) => (col ? norm(db.getRange(2 + rowIdx, col).getValue()) : '');
  result.id             = get(cID);
  result.phone          = get(cPhone);
  result.address        = get(cAddr);
  result.serving        = get(cSrv);
  result.island         = get(cIsl);
  result.region         = get(cReg);
  result.city           = get(cCity);
  result.district       = get(cDist);
  result.barangay       = get(cBrgy);
  result.subsegment     = get(cSub);
  result.acctAcq        = get(cAcq);
  result.accountOfficer = get(cAO);
  return result;
}

function fillFromDB_(sheet, row, headers, info) {
  if (!ENABLE_DB_AUTOFILL) return;
  const setIfBlank = (col, val, fmt) => {
    if (!col || !val) return;
    const rng = sheet.getRange(row, col);
    if (norm(rng.getValue()) === '') {
      if (fmt) rng.setNumberFormat(fmt);
      rng.setValue(val);
    }
  };
  const colID       = headerIndexByName_(headers, ODR_ID_ALIASES);
  const colContact  = headerIndexByName_(headers, ODR_CONTACT_ALIASES);
  const colAddr     = headerIndexByName_(headers, ODR_ADDRESS_ALIASES);
  const colSrv      = headerIndexByName_(headers, ODR_SERVING_ALIASES);
  const colIsland   = headerIndexByName_(headers, ODR_ISLAND_ALIASES);
  const colRegion   = headerIndexByName_(headers, ODR_REGION_ALIASES);
  const colCity     = headerIndexByName_(headers, ODR_CITY_ALIASES);
  const colDistrict = headerIndexByName_(headers, ODR_DISTRICT_ALIASES);
  const colBrgy     = headerIndexByName_(headers, ODR_BARANGAY_ALIASES);
  const colSubseg   = headerIndexByName_(headers, ODR_SUBSEG_ALIASES);
  const colAcq      = headerIndexByName_(headers, ODR_ACCTACQ_ALIASES);
  const colAO       = headerIndexByName_(headers, ODR_AO_ALIASES);

  setIfBlank(colID,      info.id, '@');
  setIfBlank(colContact, info.phone, '@');
  setIfBlank(colAddr,    info.address);
  setIfBlank(colSrv,      info.serving);
  setIfBlank(colIsland,   info.island);
  setIfBlank(colRegion,   info.region);
  setIfBlank(colCity,     info.city);
  setIfBlank(colDistrict, info.district);
  setIfBlank(colBrgy,     info.barangay);
  setIfBlank(colSubseg,   info.subsegment);
  setIfBlank(colAcq,      info.acctAcq);
  setIfBlank(colAO,       info.accountOfficer);
}

/***** HELPER: clear only values (preserve formulas) for a row + column range *****/
function clearRowRangeValuesKeepFormulas_(sheet, row, startCol, endCol) {
  if (endCol < startCol) return;
  const rng = sheet.getRange(row, startCol, 1, endCol - startCol + 1);
  const formulas = rng.getFormulas()[0]; // array of "" or "=..."
  for (let i = 0; i < formulas.length; i++) {
    if (!formulas[i]) sheet.getRange(row, startCol + i).clearContent(); // no formula → clear value
  }
}

/***** DISPATCH column locator (DYNAMIC; no renaming or adding) *****/
function getDispatchColIdx_(headers) {
  return {
    time:    headerIndexByName_(headers, D_TIME_ALIASES),
    name:    headerIndexByName_(headers, D_NAME_ALIASES),
    contact: headerIndexByName_(headers, D_CONTACT_ALIASES),
    address: headerIndexByName_(headers, D_ADDRESS_ALIASES),
    item:    headerIndexByName_(headers, D_ITEM_ALIASES),
    qty:     headerIndexByName_(headers, D_QTY_ALIASES),
    message: headerIndexByName_(headers, D_MSG_ALIASES),
    date:    headerIndexByName_(headers, D_DATE_ALIASES)
  };
}

/***** DISPATCH I/O (USES EXISTING COLUMNS ONLY) *****/
function removeFromDispatchByKey_(ss, keyOrName, dateVal, timeVal) {
  const dest = ss.getSheetByName(DISPATCH_SHEET);
  if (!dest) return;

  const lastRow = dest.getLastRow();
  if (lastRow < 2) return;

  const lastCol = dest.getLastColumn();
  const headers = dest.getRange(1, 1, 1, lastCol).getValues()[0];
  const idx = getDispatchColIdx_(headers);

  // We can match with whatever exists (name/date/time are preferred)
  const hasObject = typeof keyOrName === 'object' && keyOrName !== null;
  const target = {
    name:    hasObject ? norm(keyOrName.name)       : norm(keyOrName),
    date:    hasObject ? dateKey_(keyOrName.date)   : dateKey_(dateVal),
    time:    hasObject ? timeKey_(keyOrName.time)   : timeKey_(timeVal),
    address: hasObject ? norm(keyOrName.address||''): '',
    item:    hasObject ? norm(keyOrName.item||'')   : '',
    qty:     hasObject ? String(keyOrName.qty ?? ''): ''
  };

  // Build a read range wide enough to include the farthest needed column
  const neededCols = [idx.time, idx.name, idx.date, idx.address, idx.item, idx.qty].filter(Boolean);
  const width = neededCols.length ? Math.max.apply(null, neededCols) : 1;
  const rows  = dest.getRange(2, 1, lastRow - 1, width).getValues();

  const toDelete = [];
  for (let i = 0; i < rows.length; i++) {
    const r = rows[i];
    const rName = idx.name    ? norm(r[idx.name - 1])    : '';
    const rDate = idx.date    ? dateKey_(r[idx.date - 1]): '';
    const rTime = idx.time    ? timeKey_(r[idx.time - 1]): '';

    // Start permissive match: if a key column exists, it must match; if not, ignore it
    let match = true;
    if (idx.name) match = match && (rName === target.name);
    if (idx.date) match = match && (rDate === target.date);
    if (idx.time) match = match && (rTime === target.time);

    // If we supplied extras and columns exist, include them for precision
    if (match && hasObject) {
      const rAddr = idx.address ? norm(r[idx.address - 1]) : '';
      const rItem = idx.item    ? norm(r[idx.item - 1])    : '';
      const rQty  = idx.qty     ? String(r[idx.qty - 1])   : '';
      if (idx.address) match = match && (rAddr === target.address);
      if (idx.item)    match = match && (rItem === target.item);
      if (idx.qty)     match = match && (rQty  === target.qty);
    }

    if (match) toDelete.push(2 + i);
  }

  // Delete matches (protect row 2 formulas by clearing content instead of deleting row)
  toDelete.sort((a,b)=>b-a).forEach(r => {
    if (r === 2) {
      // clear values across all columns (preserve any formulas)
      const rng = dest.getRange(2, 1, 1, dest.getLastColumn());
      const formulas = rng.getFormulas()[0];
      for (let c = 0; c < formulas.length; c++) {
        if (!formulas[c]) dest.getRange(2, 1 + c).clearContent();
      }
    } else {
      dest.deleteRow(r);
    }
  });
}

function writeToDispatch_(ss, payload) {
  const dest = ss.getSheetByName(DISPATCH_SHEET);
  if (!dest) throw new Error(`Sheet not found: "${DISPATCH_SHEET}"`);

  // Read headers once and find existing columns dynamically
  const headers = dest.getRange(1, 1, 1, dest.getLastColumn()).getValues()[0];
  const idx = getDispatchColIdx_(headers);

  // Choose an anchor column we know exists to find the next empty row
  const anchorCol = idx.time || idx.name || idx.contact || idx.address || idx.item || idx.qty || 1;
  const nextRow   = firstEmptyRowInCol_(dest, anchorCol, 2);

  // Normalize inputs
  const time    = payload.time    != null ? payload.time    : '';
  const name    = payload.name    != null ? payload.name    : '';
  const contact = payload.contact != null ? String(payload.contact) : '';
  const address = payload.address != null ? String(payload.address) : '';
  const item    = payload.item    != null ? String(payload.item)    : '';
  const qty     = payload.qty     != null && payload.qty !== '' ? payload.qty : '';
  const dateOut = (payload.date instanceof Date) ? payload.date : (payload.date ? new Date(payload.date) : '');

  // Write ONLY into columns that already exist (no new columns, no renaming)
  if (idx.time)    dest.getRange(nextRow, idx.time   ).setValue(time);
  if (idx.name)    dest.getRange(nextRow, idx.name   ).setValue(name);
  if (idx.contact) dest.getRange(nextRow, idx.contact).setValue(contact);
  if (idx.address) dest.getRange(nextRow, idx.address).setValue(address);
  if (idx.item)    dest.getRange(nextRow, idx.item   ).setValue(item);
  if (idx.qty)     dest.getRange(nextRow, idx.qty    ).setValue(qty);
  if (idx.date)    dest.getRange(nextRow, idx.date   ).setValue(dateOut);  // ← uses existing Date column only
}

/***** NAME-EDIT LOGIC (Order-Taking) *****/
function handleNameEdit_(sheet, row, headers, oldNameValue) {
  const dateCol     = headerIndexByName_(headers, H_DATE);
  const monthCol    = headerIndexByName_(headers, H_MONTH);
  const weekCol     = headerIndexByName_(headers, H_WEEKNO);
  const timeCol     = headerIndexByName_(headers, H_TIME);
  const nameCol     = headerIndexByName_(headers, H_NAME);
  const contactCol  = headerIndexByName_(headers, H_CONTACT);
  const addrCol     = headerIndexByName_(headers, H_ADDRESS);
  const sofColIdx   = sofCol_(headers);
  if (!dateCol || !monthCol || !weekCol || !nameCol || !sofColIdx) return;

  const nameVal = sheet.getRange(row, nameCol).getValue();

  // Name cleared → remove in Dispatch and clear values in A–E & H–T (keep formulas)
  if (norm(nameVal) === '') {
    const dateVal = sheet.getRange(row, dateCol).getValue();
    const timeVal = timeCol ? sheet.getRange(row, timeCol).getValue() : '';

    const itemCol_ = headerIndexByName_(headers, H_ITEM);
    const skuCol_  = headerIndexByName_(headers, 'SKU');
    const qtyCol_  = headerIndexByName_(headers, H_QTY);

    const contactVal_ = contactCol ? sheet.getRange(row, contactCol).getValue() : '';
    const addrVal_    = addrCol    ? sheet.getRange(row, addrCol).getValue()    : '';
    const itemVal_    = (itemCol_ ? sheet.getRange(row, itemCol_).getValue() : '') ||
                        (skuCol_  ? sheet.getRange(row, skuCol_ ).getValue() : '') || '';
    const qtyVal_     = qtyCol_ ? sheet.getRange(row, qtyCol_).getValue() : '';

    removeFromDispatchByKey_(SpreadsheetApp.getActive(), {
      name: norm(oldNameValue) || '',
      date: dateVal,
      time: timeVal,
      address: String(addrVal_ || ''),
      item: String(itemVal_ || ''),
      qty: (qtyVal_ === '' || qtyVal_ == null) ? '' : qtyVal_
    });

    const tCol = headerIndexByName_(headers, ODR_AO_ALIASES) || 20; // fallback assume T=20
    clearRowRangeValuesKeepFormulas_(sheet, row, 1, 5);                  // A..E
    if (tCol > 7) clearRowRangeValuesKeepFormulas_(sheet, row, 8, tCol); // H..T
    return;
  }

  // Ensure Date/Month/Week/Time are populated
  const now        = new Date();
  const monthValue = Utilities.formatDate(now, TZ, 'MMMM-yyyy');
  const weekValue  = weekOfMonthLabelCappedFirstFullWeek_(now, 1);
  const timeValue  = Utilities.formatDate(now, TZ, 'HH:mm:ss');

  if (norm(sheet.getRange(row, dateCol).getValue())  === '') sheet.getRange(row, dateCol).setValue(now);
  if (norm(sheet.getRange(row, monthCol).getValue()) === '') sheet.getRange(row, monthCol).setValue(monthValue);
  if (norm(sheet.getRange(row, weekCol).getValue())  === '') sheet.getRange(row, weekCol).setValue(weekValue);
  if (timeCol && norm(sheet.getRange(row, timeCol).getValue()) === '') sheet.getRange(row, timeCol).setValue(timeValue);

  // Optional DB autofill
  if (ENABLE_DB_AUTOFILL) {
    const info = lookupCustomerFromDB_(nameVal);
    fillFromDB_(sheet, row, headers, info);
  }

  // Assign SOF if missing
  setSOFIfNeeded_(sheet, row, headers);
}

/***** MAIN TRIGGER *****/
function onEdit(e) {
  try {
    if (!e || !e.range || !e.source) return;

    const sh = e.range.getSheet();
    if (sh.getName() !== ORDER_SHEET) return;

    const row = e.range.getRow();
    if (row === 1) return;

    const headers = sh.getRange(1, 1, 1, sh.getLastColumn()).getValues()[0];
    const col     = e.range.getColumn();

    const nameCol     = headerIndexByName_(headers, H_NAME);
    const dispatchCol = headerIndexByName_(headers, H_DISPATCH);

    // 1) Name edits → autofill + (optional) DB fill + SOF
    if (col === nameCol) {
      handleNameEdit_(sh, row, headers, e.oldValue);
    }

    // 2) Dispatch toggle → write/remove in Dispatch sheet (using existing columns only)
    if (col === dispatchCol) {
      const contactCol = headerIndexByName_(headers, H_CONTACT);
      const addrCol    = headerIndexByName_(headers, H_ADDRESS);
      const itemCol    = headerIndexByName_(headers, H_ITEM);
      const skuCol     = headerIndexByName_(headers, 'SKU'); // optional fallback
      const qtyCol     = headerIndexByName_(headers, H_QTY);
      const dateCol    = headerIndexByName_(headers, H_DATE);
      const timeCol    = headerIndexByName_(headers, H_TIME);

      const get = c => c ? sh.getRange(row, c).getValue() : '';

      const nameValIn = get(nameCol);

      if (ENABLE_DB_AUTOFILL && norm(nameValIn)) {
        const info = lookupCustomerFromDB_(nameValIn);
        fillFromDB_(sh, row, headers, info);
      }

      const nameVal    = get(nameCol);
      let   contactVal = get(contactCol);
      let   addrVal    = get(addrCol);
      let   itemVal    = get(itemCol) || get(skuCol) || '';
      let   qtyVal     = get(qtyCol);
      let   dateVal    = get(dateCol);
      let   timeVal    = get(timeCol);

      contactVal = contactVal != null ? String(contactVal) : '';
      addrVal    = addrVal    != null ? String(addrVal)    : '';
      itemVal    = itemVal    != null ? String(itemVal)    : '';
      qtyVal     = (qtyVal === '' || qtyVal == null) ? '' : qtyVal;

      const now = new Date();
      if (!dateVal) {
        dateVal = now;
        sh.getRange(row, dateCol).setValue(dateVal);
      }
      if (!timeVal) {
        const t = Utilities.formatDate(now, TZ, 'HH:mm:ss');
        timeVal = t;
        if (timeCol) sh.getRange(row, timeCol).setValue(t);
      }

      if (e.value === 'TRUE') {
        writeToDispatch_(e.source, { time: timeVal, name: nameVal, contact: contactVal, address: addrVal, item: itemVal, qty: qtyVal, date: dateVal });
        const freshHeaders = sh.getRange(1, 1, 1, sh.getLastColumn()).getValues()[0];
        setSOFIfNeeded_(sh, row, freshHeaders);
        SpreadsheetApp.getActive().toast(`Dispatched row ${row} → ${DISPATCH_SHEET}`, 'OK', 3);
      }

      if (e.value === 'FALSE' || (e.value === undefined && e.oldValue === 'TRUE')) {
        removeFromDispatchByKey_(e.source, nameVal, dateVal, timeVal);
        SpreadsheetApp.getActive().toast(`Removed from ${DISPATCH_SHEET}`, 'OK', 3);
      }
    }

  } catch (err) {
    SpreadsheetApp.getActive().toast(`Action failed: ${err.message}`, 'Error', 8);
  }
}
