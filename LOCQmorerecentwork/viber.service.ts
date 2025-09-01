import { sheets_v4 } from '@googleapis/sheets';
import { Injectable } from '@nestjs/common';
import axios from 'axios';
import { JWT } from 'google-auth-library';
import { ReceiveViberDto } from './dto/receive-viber.dto';
import { SendViberDto } from './dto/send-viber.dto';

@Injectable()
export class ViberService {
  private sheets: sheets_v4.Sheets;
  private spreadsheetId = '1NK5PyFmzQjPIyLIXYPj55benFnMuSaKstfnZHm9-yvw';
  constructor() {
    // Path to your private key JSON
    const auth = new JWT({
      email: process.env.GOOGLE_CLIENT_EMAIL,
      key: (process.env.GOOGLE_PRIVATE_KEY || '').replace(/\\n/g, '\n'),
      scopes: ['https://www.googleapis.com/auth/spreadsheets'],
    });
    this.sheets = new sheets_v4.Sheets({ auth });
  }
  async sendViber(dto: SendViberDto) {
    const data = await axios.get(
      `${process.env.IBAZAAR_URL}?username=seaoilviberapi&password=52d8b68f728686615b53ba20e7a5e406
      &mobilenum=0${dto.mobileNumber}&fullmesg=${dto.message}&sms_fullmesg=${dto.message}
      &originator=31504&sms_originator=SEAOIL`
    );
    console.log(data.data);
  }

  async incrementCell(range: string) {
    const res = await this.sheets.spreadsheets.values.get({
      spreadsheetId: this.spreadsheetId,
      range,
    });

    let currentVal = 0;
    if (res.data.values && res.data.values[0] && res.data.values[0][0]) {
      currentVal = parseInt(res.data.values[0][0], 10) || 0;
    }
    const newVal = currentVal + 1;
    await this.sheets.spreadsheets.values.update({
      spreadsheetId: this.spreadsheetId,
      range,
      valueInputOption: 'RAW',
      requestBody: {
        values: [[newVal]],
      },
    });

    console.log(`🔢 Incremented ${range} from ${currentVal} → ${newVal}`);
    return newVal;
  }

  async editCell(range: string, value: string) {
    await this.sheets.spreadsheets.values.update({
      spreadsheetId: this.spreadsheetId,
      range,
      valueInputOption: 'RAW', // RAW = write exactly what we pass
      requestBody: {
        values: [[value]],
      },
    });
    console.log(`Updated ${range} with "${value}"`);
  }

  async editDate(range: string, date: Date) {
    // Force local PH time (UTC+8)
    const options: Intl.DateTimeFormatOptions = {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
      timeZone: 'Asia/Manila',
    };

    const formatted = new Intl.DateTimeFormat('en-US', options).format(date);

    await this.sheets.spreadsheets.values.update({
      spreadsheetId: this.spreadsheetId,
      range,
      valueInputOption: 'USER_ENTERED', // let Sheets parse into datetime
      requestBody: {
        values: [[formatted]],
      },
    });

    console.log(`Updated ${range} with Date ${formatted}`);
  }

  /**
   * Finds the rider row (Riders sheet) and dispatch row (Dispatching sheet)
   * for a given rider mobile number. and the assumed previous state it should be on
   *
   * E.g.
   *
   * Preparing Order must preceed In Transit
   * In Transit must preceed Complete
   *
   */
  async findRiderAndDispatch(
    num: string,
    previous_status: string
  ): Promise<{ riderName: string; riderIdx: number; dispatchIdx: number }> {
    const ridersRes = await this.sheets.spreadsheets.values.get({
      spreadsheetId: this.spreadsheetId,
      range: 'Riders!A2:E',
    });
    const ridersRows = ridersRes.data.values || [];

    let riderName = '';
    let riderIdx = -1;

    for (let i = 0; i < ridersRows.length; i++) {
      const row = ridersRows[i];
      const mobile = row[4]; // col E = mobile
      if (mobile === num) {
        riderName = row[0]; // col A = name
        riderIdx = i + 2; // +2 since starts at row 2
        break;
      }
    }

    if (!riderName || riderIdx === -1) {
      throw new Error(`❌ Rider not found for mobile ${num}`);
    }

    // 2️⃣ Find dispatch row for this rider with the expected previous status
    const dispatchRes = await this.sheets.spreadsheets.values.get({
      spreadsheetId: this.spreadsheetId,
      range: 'Dispatching & Fulfillment!A2:O',
    });
    const dispatchRows = dispatchRes.data.values || [];

    let dispatchIdx = -1;
    for (let j = 0; j < dispatchRows.length; j++) {
      const row = dispatchRows[j];
      // Column mapping: A=0 ... K=10, L=11, M=12, N=13, O=14
      const riderInRow = row[10]; // col K (Rider Name)
      const status = row[14]; // col O (Status)
      if (riderInRow === riderName && status === previous_status) {
        console.log(dispatchRows[j]);
        dispatchIdx = j + 2;
        break;
      }
    }

    if (dispatchIdx === -1) {
      throw new Error(
        `❌ No dispatch row found for rider "${riderName}" with previous status "${previous_status}"`
      );
    }

    return { riderName, riderIdx, dispatchIdx };
  }

  async riderconfirmed(msg: string, num: string) {
    try {
      const previous_status = 'Preparing Order';

      // 1. Find rider + dispatch row first ORDER IS LIKE SUPERRR IMPORTANT HERE DO NOT TO
      const { riderName, riderIdx, dispatchIdx } =
        await this.findRiderAndDispatch(num, previous_status);

      console.log(
        `✅ Found rider: ${riderName} (row ${riderIdx}), dispatch row ${dispatchIdx}`
      );

      // 2. Fetch all necessary dispatch details first (customerName, address, etc.)
      const sheetRes = await this.sheets.spreadsheets.values.get({
        spreadsheetId: this.spreadsheetId,
        range: `Dispatching & Fulfillment!C${dispatchIdx}:E${dispatchIdx}`,
      });
      const values = sheetRes.data.values?.[0] || [];
      const customerName = values[0] || '';
      const address = values[2] || '';

      // 3. Build the acknowledgment message in advance
      const sep = '\u2028';
      const message = `Thank you for acknowledging the delivery request for ${customerName} at ${address}.${sep}${sep}Kindly reply with one of the following:${sep}${sep}Send photo as proof –  once delivered${sep}${sep}”Pass” – another rider to deliver${sep}${sep}“Cancel” – customer cancelled/ cannot be fulfilled${sep}${sep}“Reschedule” – move to later time`;

      // 4. Perform edits now
      const now = new Date();
      await this.editCell(`Riders!D${riderIdx}`, 'In Transit');
      await this.editCell(`Dispatching & Fulfillment!L${dispatchIdx}`, 'Y');
      await this.editCell(
        `Dispatching & Fulfillment!O${dispatchIdx}`,
        'Out for Delivery'
      );
      await this.editDate(`Dispatching & Fulfillment!M${dispatchIdx}`, now);

      // 5. Send acknowledgment message
      await this.sendViber({
        mobileNumber: num,
        message,
      });

    } catch (err: any) {
      console.error('❌ riderconfirmed error:', err.message);
    }
  }

  async finishedorder(msg: string, num: string, attatchment: string) {
    try {
      const allowedStatuses = ['Out for Delivery', 'With Rider Issue'];
      let riderData: {
        riderName: string;
        riderIdx: number;
        dispatchIdx: number;
      } | null = null;
      for (const status of allowedStatuses) {
        try {
          riderData = await this.findRiderAndDispatch(num, status);
          if (riderData) break;
        } catch {
          // Ignore error, try next status
        }
      }

      if (!riderData) {
        throw new Error(
          `❌ Could not find rider/dispatch for ${num} with statuses ${allowedStatuses.join(
            ' or '
          )}`
        );
      }

      const { riderName, riderIdx, dispatchIdx } = riderData;

      const statusRes = await this.sheets.spreadsheets.values.get({
        spreadsheetId: this.spreadsheetId,
        range: `Dispatching & Fulfillment!O${dispatchIdx}`,
      });

      const currentStatus = statusRes.data.values?.[0]?.[0] || '';
      if (!allowedStatuses.includes(currentStatus)) {
        throw new Error(`❌ Invalid status: "${currentStatus}"`);
      }
      await this.editCell(`Riders!D${riderIdx}`, 'Available');
      const now = new Date();
      await this.editDate(`Dispatching & Fulfillment!N${dispatchIdx}`, now);

      // If there's an attachment, save it as a hyperlink in column U
      if (attatchment && attatchment.toString().trim().length > 0) {
        const url = attatchment.toString().trim();
        const safeUrl = url.replace(/"/g, '""');
        const value = url.startsWith('http')
          ? `=HYPERLINK("${safeUrl}", "Proof")`
          : url; // if not a URL, store raw text
        await this.sheets.spreadsheets.values.update({
          spreadsheetId: this.spreadsheetId,
          range: `Dispatching & Fulfillment!U${dispatchIdx}`,
          valueInputOption: 'USER_ENTERED',
          requestBody: {
            values: [[value]],
          },
        });
      }

      await this.editCell(
        `Dispatching & Fulfillment!O${dispatchIdx}`,
        'Complete'
      );

      console.log(
        `✅ Finished order for rider: ${riderName}, dispatch row ${dispatchIdx}`
      );
    } catch (err) {
      console.error(err.message);
    }
  }

  async cancelorder(msg: string, num: string) {
    try {
      const allowedStatuses = ['Out for Delivery', 'With Rider Issue'];
      let riderData: { riderName: string; riderIdx: number; dispatchIdx: number } | null = null;
      for (const status of allowedStatuses) {
        try {
          riderData = await this.findRiderAndDispatch(num, status);
          if (riderData) break;
        } catch {}
      }
      if (!riderData) {
        throw new Error(
          `❌ Could not find rider/dispatch for ${num} with statuses ${allowedStatuses.join(' or ')}`
        );
      }
      const { riderName, riderIdx, dispatchIdx } = riderData;

      // Validate against the status column (O), similar to finishedorder
      const statusRes = await this.sheets.spreadsheets.values.get({
        spreadsheetId: this.spreadsheetId,
        range: `Dispatching & Fulfillment!O${dispatchIdx}`,
      });
      const currentStatus = statusRes.data.values?.[0]?.[0] || '';
      if (!allowedStatuses.includes(currentStatus)) {
        throw new Error(`❌ Cannot cancel. Current status = "${currentStatus}"`);
      }

      await this.editCell(`Riders!D${riderIdx}`, 'Available');
      await this.editCell(`Dispatching & Fulfillment!O${dispatchIdx}`, 'Canceled');
      const now = new Date();
      await this.editDate(`Dispatching & Fulfillment!X${dispatchIdx}`, now); // cancellation timestamp
      console.log(`✅ Order canceled for rider: ${riderName}, dispatch row ${dispatchIdx}`);
    } catch (err) {
      console.error(err.message);
    }
  }

  async rescheduleorder(msg: string, num: string) {
    try {
      const allowedStatuses = ['Out for Delivery', 'With Rider Issue'];
      let riderData: { riderName: string; riderIdx: number; dispatchIdx: number } | null = null;
      for (const status of allowedStatuses) {
        try {
          riderData = await this.findRiderAndDispatch(num, status);
          if (riderData) break;
        } catch {}
      }
      if (!riderData) {
        throw new Error(
          `❌ Could not find rider/dispatch for ${num} with statuses ${allowedStatuses.join(' or ')}`
        );
      }
      const { riderName, riderIdx, dispatchIdx } = riderData;

      // Validate against the status column (O), similar to finishedorder
      const statusRes = await this.sheets.spreadsheets.values.get({
        spreadsheetId: this.spreadsheetId,
        range: `Dispatching & Fulfillment!O${dispatchIdx}`,
      });
      const currentStatus = statusRes.data.values?.[0]?.[0] || '';
      if (!allowedStatuses.includes(currentStatus)) {
        throw new Error(`❌ Cannot reschedule. Current status = "${currentStatus}"`);
      }

      await this.editCell(`Riders!D${riderIdx}`, 'Available');
      await this.editCell(`Dispatching & Fulfillment!O${dispatchIdx}`, 'Rescheduled');
      const now = new Date();
      await this.editDate(`Dispatching & Fulfillment!X${dispatchIdx}`, now); // reschedule timestamp
      console.log(`✅ Order rescheduled for rider: ${riderName}, dispatch row ${dispatchIdx}`);
    } catch (err) {
      console.error(err.message);
    }
  }

  async passorder(msg: string, num: string) {
    try {
      const allowedStatuses = ['Preparing Order', 'With Rider Issue'];
      let riderData: { riderName: string; riderIdx: number; dispatchIdx: number } | null = null;

      for (const status of allowedStatuses) {
        try {
          riderData = await this.findRiderAndDispatch(num, status);
          if (riderData) break;
        } catch {
          
        }
      }

      if (!riderData) {
        throw new Error(
          `❌ Could not find rider/dispatch for ${num} with statuses ${allowedStatuses.join(' or ')}`
        );
      }

      const { riderName, riderIdx, dispatchIdx } = riderData;

      const sheetRes = await this.sheets.spreadsheets.values.get({
        spreadsheetId: this.spreadsheetId,
        range: `Dispatching & Fulfillment!C${dispatchIdx}:E${dispatchIdx}`,
      });
      const values = sheetRes.data.values?.[0] || [];
      const customerName = values[0] || '';
      const address = values[2] || '';
      const message = `The delivery request for ${customerName} at ${address} will be reassigned to another rider. Your status remains Available for the next order.`;

      await this.editCell(`Riders!D${riderIdx}`, 'Available');
      await this.editCell(`Dispatching & Fulfillment!O${dispatchIdx}`, 'PASSED...');
      await this.incrementCell(`Riders!G${riderIdx}`);
      await this.incrementCell(`Dispatching & Fulfillment!W${dispatchIdx}`);

      console.log(`✅ Order passed for rider: ${riderName}, dispatch row ${dispatchIdx}`);

      await this.sendViber({
        mobileNumber: num,
        message,
      });

    } catch (err: any) {
      console.error('❌ passorder error:', err.message);
    }
  }


  async activaterider(msg: string, num: string) {
    try {
      // Find rider row by mobile number only
      const ridersRes = await this.sheets.spreadsheets.values.get({
        spreadsheetId: this.spreadsheetId,
        range: 'Riders!A2:E',
      });
      const ridersRows = ridersRes.data.values || [];
      let riderIdx = -1;
      for (let i = 0; i < ridersRows.length; i++) {
        const row = ridersRows[i];
        const mobile = row[4]; // col E = mobile
        if (mobile === num) {
          riderIdx = i + 2; // +2 since starts at row 2
          const currentStatus = row[3]; // col D = status
          if (currentStatus === 'Inactive') {
            await this.editCell(`Riders!D${riderIdx}`, 'Available');
            await this.sendViber({
              mobileNumber: num,
              message: 'Your status is now set to Available, and you can receive new delivery orders.',
            });
          }
          return;
        }
      }
    } catch (err) {
      console.error('Failed to set rider active:', err.message);
    }
  }

  async receiveViber(dto: ReceiveViberDto) {
    let msg = dto.fullmesg
    if(dto.fullmesg != null) {
      msg = dto.fullmesg.trim();
    }
    const num = dto.mobilenum.slice(2);
    const attatch = dto.attachment;
    console.log('Received Viber message:', dto);
    console.log('Message:', msg);
    console.log('Attachment:', attatch);
    console.log('Mobile Number:', num);

    // Preliminary check: is this number a Rider?
    let isRider = false;
    try {
      const ridersRes = await this.sheets.spreadsheets.values.get({
        spreadsheetId: this.spreadsheetId,
        range: 'Riders!E2:E',
      });
      const riderNumbers = (ridersRes.data.values || []).map(row => row[0]);
      isRider = riderNumbers.includes(num);
    } catch (err) {
      console.error('Failed to check rider number:', err.message);
    }
    if (!isRider) { //if not even a rider do nothing
      return;
    }

    if (msg === 'Ok') {
        await this.riderconfirmed(msg, num);
        // Get dispatch row for this rider
        try {
          const previous_status = 'Out for Delivery';
          const { dispatchIdx } = await this.findRiderAndDispatch(num, previous_status);
          // Get customer name (C) and address (E) from Dispatching & Fulfillment
          const sheetRes = await this.sheets.spreadsheets.values.get({
            spreadsheetId: this.spreadsheetId,
            range: `Dispatching & Fulfillment!C${dispatchIdx}:E${dispatchIdx}`,
          });
          const values = sheetRes.data.values?.[0] || [];
          const customerName = values[0] || '';
          const address = values[2] || '';
          const sep = '\u2028';
          const message = `Thank you for acknowledging the delivery request for ${customerName} at ${address}.${sep}${sep}Kindly reply with one of the following:${sep}${sep}Send photo as proof –  once delivered${sep}${sep}”Pass” – another rider to deliver${sep}${sep}“Cancel” – customer cancelled/ cannot be fulfilled${sep}${sep}“Reschedule” – move to later time`;
            await this.sendViber({
              mobileNumber: num,
            message,
          });
        } catch (err) {
          console.error('Failed to send acknowledgment message:', err.message);
        }
    } else if (msg === 'Cancel') {
      //cancel order
      await this.cancelorder(msg, num);
    } else if (msg === 'Pass') {
      await this.passorder(msg, num);
    } else if (msg === 'Reschedule') {
      await this.rescheduleorder(msg, num);
    } else if (msg === 'Active') {
      await this.activaterider(msg, num);
    } else {
      if(attatch !== null){
        await this.finishedorder(msg, num, attatch);
      }
    }

    /*
    add more conditions here
    
    */
  }
}
