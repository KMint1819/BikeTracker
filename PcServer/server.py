'''
Simple server for PC
'''

import socket
import sys
import json
from math import sin, cos, radians, asin, sqrt
import pymongo
import common

THRESHOLD = 10


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
        self.longitude = data['longitude']
        self.latitude = data['latitude']
        self.status = ''
        if 'moved' in data:
            self.moved = bool(data['moved'])
        else:
            self.moved = False

    def setStatus(self, flag):
        self.status = flag

    def toJson(self):
        """Return position as json format

        Returns:
            json object -- Position
        """
        obj = {}
        obj['longitude'] = self.longitude
        obj['latitude'] = self.latitude
        obj['moved'] = int(self.moved)
        obj['status'] = str(self.status)
        return obj

    @staticmethod
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
        if old is None:
            return False
        if haversine(old, new) > THRESHOLD:
            return True
        return False


class DB(object):

    @staticmethod
    def insertPosition(db, data):
        def inserted_format(data):
            form = {}
            form['device'] = data['device']
            form['data'] = {}
            form['data']['time'] = data['time']
            form['data']['position'] = data['position']
            return form
        data = inserted_format(data)
        print('Inserting: ', data)
        query = {'device': data['device']}
        collection = db['timeline']
        device = collection.find_one(query)
        if device is None:
            collection.insert_one(data)
        else:
            if not isinstance(device['data'], list):
                device['data'] = [device['data']]
            device['data'].append(data['data'])
            collection.replace_one(query, device)

    @staticmethod
    def getHistory(db):
        collection = db['timeline']
        query = {'device': 'SERVER'}
        obj = collection.find_one(query)
        if 'data' in obj:
            return obj['data']
        return []


def main():
    '''
    %
    '''
    global PORT
    s = socket.socket()
    client = pymongo.MongoClient('localhost', 27017)
    try:
        # The ismaster command is cheap and does not require auth.
        client.admin.command('ismaster')
    except pymongo.errors.ConnectionFailure:
        print("Server not available")
    print('Connected to database!')
    db = client['biketracker']

    while True:
        try:
            s.bind((HOST, PORT))
            s.listen(5)
            print(f'Server running on: {HOST}:{PORT}')
            break
        except OSError:
            PORT += 1
    current_pos = None
    status = 'END'
    while True:
        print('-'*50)
        client_skt, adr = s.accept()
        print(f'{adr} is connected...')
        rcv_json = None
        try:
            client_skt.settimeout(10.0)
            rcv_str = client_skt.recv(2048).decode('utf-8')
            print(f'Client sent: <{rcv_str}>')
            rcv_json = json.loads(rcv_str)
            print(f'To json: \nf{rcv_json}')
        except socket.timeout:
            print('Receive timeout!')
            continue
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
            if Position.moved(current_pos, pos):
                pos.moved = True
            current_pos = pos
            msg = common.get_initial_msg('SERVER')
            print('ARDUINO, status: ', status)
            if status == 'MOVING':
                current_pos.setStatus(status)
                msg['position'] = current_pos.toJson()
                DB.insertPosition(db, msg)

        elif rcv_json['device'] == 'PHONE':
            if rcv_json['request'] == 'START':
                print('Receiving START from phone!')
                status = 'STOP'
                msg = common.get_initial_msg('SERVER')
                if current_pos is not None:
                    current_pos.moved = False
                    current_pos.setStatus(status)
                    msg['position'] = current_pos.toJson()
                    DB.insertPosition(db, msg)
                msg_str = json.dumps(msg) + '\n'
                client_skt.send(msg_str.encode())

            elif rcv_json['request'] == 'GET':
                print('Receiving GET from phone!')
                msg = common.get_initial_msg('SERVER')
                if current_pos is not None:
                    msg['position'] = current_pos.toJson()
                    msg_str = json.dumps(msg) + '\n'
                    print(f'Sending {json.dumps(msg, indent=4)} to phone...')
                    client_skt.send(msg_str.encode())

            elif rcv_json['request'] == 'HISTORY':
                print('Receiving HISTORY from phone!')
                msg = common.get_initial_msg('SERVER')
                msg['history'] = DB.getHistory(db)
                msg_str = json.dumps(msg) + '\n'
                print(f'Sending {json.dumps(msg, indent=4)} to phone')
                client_skt.send(msg_str.encode())

            elif rcv_json['request'] == 'END':
                print('Receiving END from phone!')
                msg = common.get_initial_msg('SERVER')
                status = 'FIRST'
                if current_pos is not None:
                    current_pos.setStatus(status)
                    msg['position'] = current_pos.toJson()
                    DB.insertPosition(db, msg)
                status = 'MOVING'
                msg_str = json.dumps(msg) + '\n'
                client_skt.send(msg_str.encode())
                print('Status: ', status)
        else:
            print('Format error! Check the documentation.')


if __name__ == '__main__':
    main()
