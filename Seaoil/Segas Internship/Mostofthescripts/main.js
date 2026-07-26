function main() {
  // Non Happy Path checks first
  runWithRetry(checkStaleOrders,3,2000,16000);
  runWithRetry(autoPass,3,2000,16000);
  runWithRetry(PassHandling, 3, 2000, 16000);

  //happy path
  runWithRetry(updateRiderStatus, 3, 2000, 16000);   // 1️⃣ keep Riders sheet up to date first
  runWithRetry(getUnassignedRows, 3, 2000, 16000);  // 2️⃣ assign new orders next
  runWithRetry(checkMessages, 3, 2000, 16000);      // 3️⃣ process rider responses last

}


function runWithRetry(fn, retries = 3, delay = 2000, maxDelay = 16000) {
  for (let i = 0; i < retries; i++) {
    try {
      fn();
      return; // success, exit loop
    } catch (e) {
      if (i === retries - 1) {
        throw e; // rethrow after last attempt
      }
      Logger.log(`⚠️ ${fn.name} failed on attempt ${i + 1}: ${e}. Retrying in ${delay}ms...`);
      Utilities.sleep(delay);
      delay = Math.min(delay * 2, maxDelay); // exponential backoff capped at 8s
    }
  }
}

