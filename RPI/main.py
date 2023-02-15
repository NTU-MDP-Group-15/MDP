"""
Filename: main.py
Version: 0.1

Starts all relevant threads required
! Updates (DDMMYY)
"""
import argparse
import modules

from argparse import RawTextHelpFormatter, RawDescriptionHelpFormatter

def arg_parser(test_arg=None):
    descriptor = ""

    # parser = argparse.ArgumentParser(description=descriptor,
    #                                 formatter_class=RawTextHelpFormatter)
    
    parser = argparse.ArgumentParser(description=descriptor)
    parser.add_argument('max_client', metavar='[0-10]', type=int,
                        help='Maximum number of clients')

    # parser.print_help() 
    if test_arg != None:
        return parser.parse_args(test_arg)  # arg_parser
    
    return parser.parse_args()  # arg_parser   

class MDPPi():
    def __init__():
        #self.imint = modules.ImageRecInterface()
        #self.stmint = modules.STMInterface()
        self.btint = modules.BTServerInterface("MDP-Pi BT Server")          # thread
        #self.algoint = modules.AlgoServerInterface("MDP-Pi Algo Server")    # thread
        #self.algoint.start()
        self.btint.start()


def test():    
    args = arg_parser(test_arg="2")
    max_client = args.max_client
    #imgint = modules.ImageRecInterface()
    #stmint = modules.STMInterface()
    btint = modules.BTServerInterface("MDP-Pi BT Server")
    algoint = modules.AlgoServerInterface("MDP-Pi Algo Server")    # thread
    algoint.start()
    btint.start()
    
if __name__ == '__main__':
    test()
