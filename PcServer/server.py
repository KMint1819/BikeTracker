'''
Simple server for PC
'''

import socket

PORT = 8051
HOST = ''

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

def main():
    '''
    %
    '''
    global HOST
    HOST = get_host_ip()

    s = socket.socket()
    s.bind((HOST, PORT))
    s.listen(5)
    print(f'Server running on: {HOST}:{PORT}')
    while True:
        client_skt, adr = s.accept()
        msg = client_skt.recv(1024).decode('utf-8')
        print(f'Client {adr} sending: "{msg}"')

if __name__ == '__main__':
    main()