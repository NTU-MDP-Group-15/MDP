# Collection of colours
BLACK = (0, 0, 0)
WHITE = (255, 255, 255)
GREEN = (0, 255, 0)
RED = (255, 0, 0)
GRAY = (192, 192, 192)
BLUE = (50, 100, 150)
LIGHT_BLUE = (100, 255, 255)
LIGHT_RED = (255, 144, 144)
LIGHT_GREEN = (154, 247, 182)

# Collection of robot constants
NORTH = 0
SOUTH = 180
EAST = -90
WEST = 90
ROBOT_W = 30
ROBOT_H = 30

# Starting grid positions of car
ROBOT_STARTING_X = 1
ROBOT_STARTING_Y = 1
ROBOT_STARTING_ANGLE = NORTH
TURNING_RADIUS = 3

# this is the buffer from the boundary of the grid
# negative values mean the cell representing the robot can move outside of the grid
BOUNDARY_BUFFER = -1

FPS = 60

# RPI Connection
RPI_CONNECTED = False

# Headless execution
HEADLESS = False

# WIFI server IP address
RPI_IP = "10.91.234.84" # real RPi server
RPI_IP_WIFI = "192.168.15.1"
RPI_IP_WIFI_NEW = "192.168.15.69"
MY_IP = "192.168.1.8"
TEST_IP = "10.91.96.223" # Use this for easier testing RPi integration without RPi
WIFI_IP = RPI_IP_WIFI
PORT = 12345
