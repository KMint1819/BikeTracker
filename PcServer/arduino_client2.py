'''
Simple client for demo.
'''

import socket
import sys
import json
import random
import time

import common

SERVER_IP = '192.168.43.89'
if len(sys.argv) > 1:
    PORT = int(sys.argv[1])
else:
    PORT = 8051

def readData(path='data.txt'):
    d = []
    with open(path, 'r') as f:
        while True:
            line = f.readline()
            if not line:
                break
            d.append(line)
    return d

def main():
    '''
    %
    '''
    idx = 0
    data = readData('data.txt')
    while idx < len(data):
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            print('Trying to connect...')
            s.connect((SERVER_IP, PORT))
            print('Connected!')
        except socket.error:
            print('Socket error!')
        latlng = data[idx]
        idx += 1
        position = {}
        lat = latlng.split(',')[0]
        lng = latlng.split(',')[1]
        position['latitude'] = lat
        position['longitude'] = lng
        msg = common.get_initial_msg('ARDUINO')
        msg['position'] = position

        print(f'Sending {json.dumps(msg).encode()}')
        s.send(json.dumps(msg).encode())
        time.sleep(0.1)

if __name__ == "__main__":
    main()
