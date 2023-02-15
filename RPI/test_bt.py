"""
Filename: main.py
Version: 0.1

Starts all relevant threads required
! Updates (DDMMYY)
"""
import argparse
import modules

def test():
    btint = modules.BTServerInterface("MDP-Pi BT Server")
    btint.start()
    
if __name__ == '__main__':
    test()
