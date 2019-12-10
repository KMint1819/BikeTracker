'''
Simple client to emulate the phone
'''

import socket
import sys
SERVER_IP = '192.168.66.105'

if len(sys.argv) > 1:
    PORT = int(sys.argv[1])
else:
    PORT = 8051

DEVICE = 'PHONE'
SEND_TIME = '1999:07:11:10:37:24'
HEADER = f'{DEVICE},{SEND_TIME}'


def main():
    '''
    %
    '''
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    activate = False
    input('Press enter to start...')

    s.connect((SERVER_IP, PORT))
    s.send(f'{DEVICE},{SEND_TIME},START'.encode())
    print('Sent START to server!')
    rcv_raw = s.recv(128).decode('utf-8')
    if rcv_raw.split(',')[-1] == 'OK':
        activate = True
    print('Started sending data!')
    while activate is True:
        print('-'*50)
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect((SERVER_IP, PORT))
        try:
            s.send(f'{HEADER},GET'.encode())
            send_time, latitude, longitude, moved = s.recv(
                256).decode('utf-8').split(',')
            print(f'Current position: {latitude}, {longitude}')
            print(f'Moved: {moved}')
        except socket.error():
            print('Socket error!')
        input('Press ENTER to send another phone request...')


if __name__ == "__main__":
    main()
