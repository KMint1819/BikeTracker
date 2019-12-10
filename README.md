# BikeTracker
A little module for tracking objects(assuming bicycles) and prevent them from been stolen.

## Sending format:

- Arduino data:
"`Device`,`YYYY`:`MM`:`DD`:`hh`:`mm`:`ss`,`latitude`,`longitude`,`Moved`"
    - `Device`: Type of device. (Options are "ARDUINO" and "PHONE", "SERVER")
    - `Moved`: Whether the device is moved. (Options are "1" for true and "0" for false.)
    - Format of `longitude`, `latitude` is still unknown
- Phone request:
"`Device`,`YYYY`:`MM`:`DD`:`hh`:`mm`:`ss`,`Request type`"
    - `Device`: Type of device. (Options are "ARDUINO", "PHONE", "SERVER")
    - `Request type`: Type of request. (Options are "GET", "START", "STOP")
- Split with commas
- Examples:
    - Phone -> Server:
        "PHONE,1999:07:11:10:37:24,GET"
    - Arduino -> Server:
        "ARDUINO,1999:07:11:10:37:24,N678924,E343210"
    - Server -> Phone:
        "SERVER,1999:07:11:10:37:24,N678924,E343210,1"
    
## Notes:
- 緯度是latitude, 經度是longitude