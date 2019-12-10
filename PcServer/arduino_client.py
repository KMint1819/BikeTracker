'''
Simple client for testing server
'''

import socket
import sys

SERVER_IP = '192.168.66.105'
if len(sys.argv) > 1:
    PORT = int(sys.argv[1])
else:
    PORT = 8051

DEVICE = 'ARDUINO'
LONGITUDE = 'E343210'
LATITUDE = 'N678924'
SEND_TIME = '1999:07:11:10:37:24'


def main():
    '''
    %
    '''

    while True:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            s.connect((SERVER_IP, PORT))
        except socket.error:
            print('Socket error!')

        msg = f'{DEVICE},{SEND_TIME},{LATITUDE},{LONGITUDE}'
        s.send(msg.encode())

        input('Press ENTER to send again...')


if __name__ == "__main__":
    main()
