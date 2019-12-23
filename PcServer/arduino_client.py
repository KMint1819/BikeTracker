'''
Simple client for testing server
'''

import socket
import sys
import json
import random
import common

SERVER_IP = '10.115.50.31'
if len(sys.argv) > 1:
    PORT = int(sys.argv[1])
else:
    PORT = 8051


def get_random_position():
    """Returns a tuple of random position base on format.
        ex. (27.123456, -130.765432)

    Returns:
        tuple
    """
    rand_lat = random.randrange(-180000000, 180000000)
    rand_lng = random.randrange(-180000000, 180000000)
    rand_lat = float(rand_lat) / 1000000.
    rand_lng = float(rand_lng) / 1000000.
    return rand_lat, rand_lng


def main():
    '''
    %
    '''

    device = 'ARDUINO'
    latitude = '27.123456'
    longitude = '-130.765432'
    while True:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            s.connect((SERVER_IP, PORT))
        except socket.error:
            print('Socket error!')
        latlng = input(
            'Enter the longitude and latitude (Default is random): ')
        if latlng == '':
            position = {}
            rand_position = get_random_position()
            position['latitude'] = rand_position[0]
            position['longitude'] = rand_position[1]
        else:
            lng = latlng.split(',')[0]
            lat = latlng.split(',')[1]
            position['latitude'] = lat
            position['longitude'] = lng
        msg = common.get_initial_msg('ARDUINO')
        msg['position'] = position

        print(f'Sending {json.dumps(msg).encode()}')
        s.send(json.dumps(msg).encode())
        input('Press ENTER to send again...')


if __name__ == "__main__":
    main()
