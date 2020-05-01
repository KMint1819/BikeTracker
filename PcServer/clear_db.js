conn = new Mongo();
db = conn.getDB("biketracker");
printjson(db.timeline.drop());