'''
Simple client to emulate the phone
'''

import socket
import sys
import json
import common

SERVER_IP = '192.168.66.105'

if len(sys.argv) > 1:
    PORT = int(sys.argv[1])
else:
    PORT = 8051

START_REQ = """{
    "device": "PHONE",
    "time": {
        "year": 1999,
        "month": 7,
        "day": 11,
        "hour": 17,
        "minute": 54,
        "second": 36
    },
    "request": "START"
}"""
STOP_REQ = """{
    "device": "PHONE",
    "time": {
        "year": 1999,
        "month": 7,
        "day": 11,
        "hour": 17,
        "minute": 54,
        "second": 36
    },
    "request": "STOP"
}"""
GET_REQ = """{
    "device": "PHONE",
    "time": {
        "year": 1999,
        "month": 7,
        "day": 11,
        "hour": 17,
        "minute": 54,
        "second": 36
    },
    "request": "GET"
}"""


def main():
    '''
    %
    '''

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    activate = False
    input('Press enter to start...')
    msg = common.get_initial_msg('PHONE')
    msg['request'] = 'START'
    s.connect((SERVER_IP, PORT))
    s.send(json.dumps(msg).encode())
    print('Sent START to server!')
    rcv_raw = s.recv(512).decode('utf-8')
    activate = True
    print('Started sending data!')
    while activate is True:
        print('-'*50)
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect((SERVER_IP, PORT))
        try:
            msg = common.get_initial_msg('PHONE')
            msg['request'] = 'GET'
            s.send(json.dumps(msg).encode())
            rcv_raw = s.recv(512).decode('utf-8')
            rcv_json = json.loads(rcv_raw)

            print(f'Current position: {rcv_json["position"]}')
        except socket.error():
            print('Socket error!')
        input('Press ENTER to send another phone request...')


if __name__ == "__main__":
    main()
