# Project Slide
https://docs.google.com/presentation/d/1rhiyHuJBUHd2zr5PsOnRjZU6bZsIwiP2GltipoE7IJ4/edit?fbclid=IwAR1vS-l-snuMYF4c9KJSDdL5u5psn0LHU2Tl2OGnc6CUnso7STWhYJkCS6I#slide=id.g13c463fa2a257a66_418

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
        - `Moved`: Whether the device is moved. ("1" for true and "0" for false.)

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
