# BikeTracker
A little module for tracking objects(assuming bicycles) and prevent them from been stolen.

## Sending format:
- Arduino data:
    - ```json=
        {
            "device": <Device name>,
            "time": {
                "year": <Year>,
                "month": <Month>,
                "day": <Day>,
                "hour": <Hour>,
                "minute": <Minute>,
                "second": <Second>
            },
            "position": {
                "longitude": <Longitude>,
                "latitude": <Latitude>,
                "moved": <Moved>
            }
        }
      ```
        - Format of `longitude`, `latitude` is still unknown
        - `Moved`: Whether the device is moved. (Options are "1" for true and "0" for false.)

- Phone request:
    - ```json=
        {
            "device": <Device name>,
            "time": {
                "year": <Year>,
                "month": <Month>,
                "day": <Day>,
                "hour": <Hour>,
                "minute": <Minute>,
                "second": <Second>
            },
            "request": <Request type>
        }
      ```
        - `request`: Type of request. (Options are "GET", "START", "STOP")
- Formats:
    - time:
        - `year` is 4 digit.
        - `hour` is 24-hour clock. 
- See examples in `common/examples/format`
- Examples:
    - Phone -> Server:
        "PHONE,1999:07:11:10:37:24,GET"
    - Arduino -> Server:
        "ARDUINO,1999:07:11:10:37:24,N678924,E343210"
    - Server -> Phone:
        "SERVER,1999:07:11:10:37:24,N678924,E343210,1"
    
## Notes:
- 緯度是latitude, 經度是longitude