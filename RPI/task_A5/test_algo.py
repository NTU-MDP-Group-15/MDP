"""
Filename: main.py
Version: 0.1

Starts all relevant threads required
! Updates (DDMMYY)
"""
import argparse
import modules


def test():
    algoint = modules.AlgoServerInterface("MDP-Pi Algo Server")    # thread
    algoint.start()
    
if __name__ == '__main__':
    test()
