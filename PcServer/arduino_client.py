'''
Simple client for testing server
'''

import socket
import sys
import json
import random
import common

SERVER_IP = '192.168.66.105'
if len(sys.argv) > 1:
    PORT = int(sys.argv[1])
else:
    PORT = 8051


def get_random_position():
    """Returns a tuple of random position base on format.
        ex. (N123456, E415123)

    Returns:
        tuple
    """
    latitude_choice = ['N', 'S']
    latitude_direction = latitude_choice[random.randrange(0, 2)]
    latitude_value = random.randrange(0, 1000000)
    longitude_choice = ['E', 'W']
    longitude_direction = longitude_choice[random.randrange(0, 2)]
    longitude_value = random.randrange(0, 1000000)

    ret = (
        latitude_direction + str(latitude_value).zfill(6),
        longitude_direction + str(longitude_value).zfill(6)
    )
    return ret


def main():
    '''
    %
    '''

    device = 'ARDUINO'
    latitude = 'N678924'
    longitude = 'E343210'
    while True:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            s.connect((SERVER_IP, PORT))
        except socket.error:
            print('Socket error!')
        rand_position = get_random_position()
        position = {}
        position['latitude'] = rand_position[0]
        position['longitude'] = rand_position[1]
        msg = common.get_initial_msg('ARDUINO')
        msg['position'] = position

        print(f'Sending {json.dumps(msg).encode()}')
        s.send(json.dumps(msg).encode())
        input('Press ENTER to send again...')


if __name__ == "__main__":
    main()
