'''
Simple server for PC
'''

import socket
import sys
import json
from math import sin, cos, radians, asin, sqrt
import common

THRESHOLD = 5
def get_host_ip():
    '''
    %
    '''
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(('8.8.8.8', 80))
        ip = s.getsockname()[0]
    finally:
        s.close()

    return ip


if len(sys.argv) > 1:
    PORT = int(sys.argv[1])
else:
    PORT = 8051
HOST = get_host_ip()


class Position(object):
    '''
    Class for storing position data, including longitude, latitude, and moved.
    '''

    def __init__(self, data):
        """%

        Arguments:
            data {json} -- json object which contains longitude, latitude, and moved
        """
        self.longitude = float(data['longitude'])
        self.latitude = float(data['latitude'])

        if 'moved' in data:
            self.moved = bool(data['moved'])
        else:
            self.moved = False

    def to_dict(self):
        """Return position as dict

        Returns:
            dict -- Position
        """
        obj = {}
        obj['longitude'] = str(self.longitude)
        obj['latitude'] = str(self.latitude)
        obj['moved'] = int(self.moved)
        return obj


def moved(old, new):
    '''
    Use some algorithms and threshold to determine whether the position is moved.
    '''
    def haversine(old, new):
        """An implementation for haversine formula which calculates distance between
        coordinates.

        Arguments:
            old {Position} -- Old position
            new {Position} -- New position

        Returns:
            float -- Distance for two coordintates. (Meters)
        """
        lng1 = radians(float(old.longitude))
        lat1 = radians(float(old.latitude))
        lng2 = radians(float(new.longitude))
        lat2 = radians(float(new.latitude))

        dlng = lng2 - lng1
        dlat = lat2 - lat1
        a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlng/2)**2
        c = 2 * asin(sqrt(a))
        r = 6371
        distance = c * r * 1000
        print(f'Distance between old and new: {distance}m.')
        return distance
    if haversine(old, new) > THRESHOLD:
        return True
    return False


def main():
    '''
    %
    '''
    global PORT
    s = socket.socket()
    while True:
        try:
            s.bind((HOST, PORT))
            s.listen(5)
            print(f'Server running on: {HOST}:{PORT}')
            break
        except OSError:
            PORT += 1
    current_pos = None
    while True:
        print('-'*50)
        client_skt, adr = s.accept()
        print(f'{adr} is connected...')
        rcv_json = None
        try:
            rcv_str = client_skt.recv(2048).decode('utf-8')
            # rcv_str = client_skt.recv(2048)
            print(f'Client sent: <{rcv_str}>')
            # print('Removing the first and second character...')
            # rcv_str = rcv_str[2:]
            # print(f'Becomes {rcv_str}')
            rcv_json = json.loads(rcv_str)
            print(f'To json: \nf{rcv_json}')
        except json.decoder.JSONDecodeError:
            print('Format error! Check the documentation.')
            continue
        if rcv_json['device'] not in {'ARDUINO', 'PHONE'}:
            print('Format error! Check the documentation.')
            continue

        print(f'Device type: {rcv_json["device"]}')
        print(f'Source: {adr}')
        print(f'Send time: {rcv_json["time"]}')

        if rcv_json['device'] == 'ARDUINO':
            pos = Position(rcv_json['position'])
            print(f'Position: 緯度{pos.latitude} 經度{pos.longitude}')
            if current_pos is not None:
                pos.moved = moved(current_pos, pos)
            current_pos = pos
        elif rcv_json['device'] == 'PHONE':
            if rcv_json['request'] == 'START':
                print('Receiving START from phone!')
                msg = common.get_initial_msg('SERVER')
                msg_str = json.dumps(msg) + '\n'
                client_skt.send(msg_str.encode())
            elif rcv_json['request'] == 'GET':
                print('Receiving GET from phone!')
                if current_pos is not None:
                    msg = common.get_initial_msg('SERVER')
                    msg['position'] = current_pos.to_dict()
                    msg_str = json.dumps(msg) + '\n'
                    print(f'Sending {json.dumps(msg, indent=4)} to phone...')
                    client_skt.send(msg_str.encode())
        else:
            print('Format error! Check the documentation.')


if __name__ == '__main__':
    main()
