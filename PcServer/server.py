'''
Simple server for PC
'''

import socket
import sys
import common


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
            data {list} -- List contains data[0] = latitude, data[1] = longitude,
                           data[2] = moved
        """
        self.latitude = data[0]
        self.longitude = data[1]
        if len(data) == 3:
            self.moved = str(data[2])
        elif len(data) == 2:
            self.moved = str(0)

    def to_string(self):
        '''
        %
        '''
        str_data = self.latitude + ',' + self.longitude + ',' + str(int(self.moved))
        return str_data


# NOT FINISHED!
def moved(old_position, new_position):
    '''
    Use some algorithms and threshold to determine whether the position is moved.
    '''
    return True


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
        rcv_raw = client_skt.recv(2048).decode('utf-8')
        client_device = rcv_raw.split(',')[0]
        if client_device not in {'ARDUINO', 'PHONE'}:
            print('Format error! Check the documentation.')
            continue

        send_time = rcv_raw.split(',')[1]
        print(f'Device type: {client_device}')
        print(f'Source: {adr}')
        print(f'Send time: {send_time}')

        if client_device == 'ARDUINO':
            pos = Position(rcv_raw.split(',')[2:])
            print(f'Position: 緯度{pos.latitude} 經度{pos.longitude}')
            if moved(current_pos, pos):
                pos.moved = True
            current_pos = pos
        elif client_device == 'PHONE':
            request_type = rcv_raw.split(',')[2]
            if request_type == 'START':
                print('Receiving START from phone!')
                server_send_time = common.get_send_time()
                client_skt.send(f'{server_send_time},OK'.encode())
            elif request_type == 'GET':
                print('Receiving GET from phone!')
                if current_pos is not None:
                    server_send_time = common.get_send_time()
                    print(
                        'Sending '+f'{server_send_time},{current_pos.to_string()} to phone')
                    client_skt.send(
                        f'{server_send_time},{current_pos.to_string()}'.encode())
        else:
            print('Format error! Check the documentation.')


if __name__ == '__main__':
    main()
