function view() {
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
