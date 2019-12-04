'''
Simple client for testing server
'''

import socket

SERVER_IP = '192.168.66.105'
PORT = 8051

def main():
    '''
    %
    '''
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        s.connect((SERVER_IP, PORT))
    except socket.error:
        print('Socket connect failed!')
    s.send('Hello im client!'.encode())
    
if __name__ == "__main__":
    main()