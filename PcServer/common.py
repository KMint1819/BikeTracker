'''
%
'''
from datetime import datetime
import json


def get_send_time_json():
    '''
    Gets current time in json string format
    '''
    now_time = datetime.now().strftime('%Y:%m:%d:%H:%M:%S')
    now_arr = now_time.split(':')
    now_json = {}
    now_json['year'] = int(now_arr[0])
    now_json['month'] = int(now_arr[1])
    now_json['day'] = int(now_arr[2])
    now_json['hour'] = int(now_arr[3])
    now_json['minute'] = int(now_arr[4])
    now_json['second'] = int(now_arr[5])
    return now_json


def get_initial_msg(device):
    """Returns a dict for initial data

    Arguments:
        device {str} -- Device name(SERVER, PHONE, ARDUINO)

    Returns:
        dict -- Basic data for every message.
    """
    msg = {}
    msg['device'] = device
    msg['time'] = get_send_time_json()
    return msg


def test():
    '''
    %
    '''
    print(get_send_time())


if __name__ == "__main__":
    test()
