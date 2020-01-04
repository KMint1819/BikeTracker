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
    
## Notes:
- 緯度是latitude, 經度是longitude
## Encountered problems:
1. LORA gateway out of budget
2. 原本要使用LORA p2p, but 手機sim卡ip 隨時變動, 無法得知位址
3. Arduino requires more power supply
5. Cost a lot of phone energy