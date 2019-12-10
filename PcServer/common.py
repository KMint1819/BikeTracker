'''
%
'''
from datetime import datetime


def get_send_time():
    '''
    %
    '''
    return datetime.now().strftime('%Y:%m:%d:%H:%M:%S')


def test():
    '''
    %
    '''
    print(get_send_time())


if __name__ == "__main__":
    test()
