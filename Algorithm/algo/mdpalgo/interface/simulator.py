import logging
import os
import queue
import threading

from mdpalgo import constants
import pygame
from mdpalgo.algorithm.astar import AStar
from mdpalgo.algorithm.astar_hamiltonian import AStarHamiltonian
from mdpalgo.algorithm.hamiltonian_path_planners import ExhaustiveHamiltonianPathPlanner
from mdpalgo.algorithm.path_planning import PathPlan
from mdpalgo.communication.comms import AlgoClient
from mdpalgo.communication.message_parser import MessageParser, MessageType, TaskType
from mdpalgo.interface.panel import Panel
from mdpalgo.map.grid import Grid
from mdpalgo.robot.robot import Robot

# for saving the image
from PIL import Image

# Set the HEIGHT and WIDTH of the screen
WINDOW_SIZE = [960, 660]


class Simulator:

    def __init__(self):
        self.comms = None

        # Initialize pygame
        self.root = pygame
        self.root.init()
        self.root.display.set_caption("MDP Algorithm Simulator")
        self.screen = None
        if not constants.HEADLESS:
            self.screen = pygame.display.set_mode(WINDOW_SIZE)
            self.screen.fill(constants.SIMULATOR_BG)

        # Callback methods queue - for passing of callback functions from worker thread to main UI thread
        self.callback_queue = queue.Queue()

        # Astar class
        self.astar = None
        # Path planner class
        self.path_planner = None
        # Astar hamiltonian class
        self.astar_hamiltonian = None
        # Hamiltonian path planner class
        self.hamiltonian_path_planner = None

        # This is the margin around the left and top of the grid on screen
        # display
        self.grid_from_screen_top_left = (120, 120)
        # Initialise 20 by 20 Grid
        self.grid = Grid(20, 20, 20, self.grid_from_screen_top_left)
        if not constants.HEADLESS:
            # Draw the grid
            self.redraw_grid()

        # Initialise side panel with buttons
        self.panel = Panel(self.screen)

        # Used to manage how fast the screen updates
        # self.clock = pygame.time.Clock()
        self.startTime = pygame.time.get_ticks() / 1000
        self.ticks = 0

        # Car printing process
        current_dir = os.path.dirname(os.path.abspath(__file__))
        image_path = os.path.join(current_dir, "car.png")
        car_image = pygame.image.load(image_path)
        self.car = Robot(self, self.screen, self.grid, constants.ROBOT_W, constants.ROBOT_H,
                         constants.ROBOT_STARTING_X, constants.ROBOT_STARTING_Y, constants.ROBOT_STARTING_ANGLE,
                         car_image)
        # Draw the car
        self.car.draw_car()

        # parser to parse messages from RPi
        self.parser = MessageParser()

        # count of 'no image result' exception
        self.no_image_result_count = 0

    def redraw_grid(self):
        self.grid_surface = self.grid.get_updated_grid_surface()
        self.screen.blit(self.grid_surface, self.grid_from_screen_top_left)

    def run(self):
        # Loop until the user clicks the close button.
        done = False

        # -------- Main Program Loop -----------
        if constants.HEADLESS:  # to simplify implementation, we use 2 threads even if headless
            print("Waiting to connect")
            self.start_algo_client()
            while True:
                try:
                    self.handle_worker_callbacks()
                except queue.Empty:  # raised when queue is empty
                    continue

        else:
            while not done:
                # Check for callbacks from worker thread
                while True:
                    try:
                        self.handle_worker_callbacks()
                    except queue.Empty:  # raised when queue is empty
                        break

                for event in pygame.event.get():
                    if event.type == pygame.QUIT:
                        done = True
                    elif event.type == pygame.MOUSEBUTTONDOWN:
                        # User clicks the mouse. Get the position
                        pos = pygame.mouse.get_pos()
                        if self.is_pos_clicked_within_grid(pos):
                            self.grid.grid_clicked(pos[0], pos[1])
                            self.redraw_grid()
                            self.car.draw_car()  # Redraw the car

                        else:  # otherwise, area clicked is outside of grid
                            self.check_button_clicked(pos)

                now = pygame.time.get_ticks() / 1000
                if now - self.startTime > 1 / constants.FPS:
                    self.startTime = now
                    self.root.display.flip()

        # Be IDLE friendly. If you forget this line, the program will 'hang' on
        # exit.
        print("closing")
        self.root.quit()

    def is_pos_clicked_within_grid(self, pos):
        grid_from_screen_left = self.grid_from_screen_top_left[0]
        grid_from_screen_top = self.grid_from_screen_top_left[1]

        grid_pixel_size_x, grid_pixel_size_y = self.grid.get_total_pixel_size()
        if grid_from_screen_left < pos[0] < grid_from_screen_left + grid_pixel_size_x and \
           grid_from_screen_top < pos[1] < grid_from_screen_top + grid_pixel_size_y:
            return True
        return False

    def start_algo_client(self):
        """Connect to RPi wifi server and start a thread to receive messages """
        self.comms = AlgoClient()
        self.comms.connect()
        constants.RPI_CONNECTED = True
        self.receiving_process()

    def handle_worker_callbacks(self):
        """Check for callbacks from worker thread and handle them

        Raises:
            queue.Empty if the callback queue is empty
        """
        callback = self.callback_queue.get(False)  # doesn't block
        if isinstance(callback, list):
            logging.info("Current callback: \n%s", callback)
            logging.info("Logging 1: ", str(callback[1]))
            callback[0](callback[1])
        else:
            callback()

    def receiving_process(self):
        """
        Method to be run in a separate thread to listen for commands from the socket
        Methods that update the UI must be passed into self.callback_queue for running in the main UI thread
        Running UI updating methods in a worker thread will cause a flashing effect as both threads attempt to update the UI
        """

        while constants.RPI_CONNECTED:
            try:
                if self.comms.client_socket is not None:
                    txt = self.comms.client_socket.recv(1024)
                    print("Text: ", txt)
                    if (txt == None):
                        continue

                message_type_and_data = self.parser.parse(txt.decode())
                message_data = message_type_and_data["data"]
                if message_type_and_data["type"] == MessageType.START_TASK:  # From Android
                    self.on_receive_start_task_message(message_data)
                    return

                elif message_type_and_data["type"] == MessageType.UPDATE_ROBOT_POSE:
                    self.on_receive_update_robot_pose(message_data)

                elif message_type_and_data["type"] == MessageType.IMAGE_TAKEN:
                    self.on_receive_image_taken_message(message_data)

            except (IndexError, ValueError) as e:
                print("Invalid command: " + txt.decode())

    def on_receive_start_task_message(self, message_data: dict):
        task = message_data["task"]

        if task == TaskType.TASK_EXPLORE:  # Week 8 Task
            # Reset first
            self.reset_button_clicked()
            # Set robot starting pos
            robot_params = message_data['robot']
            logging.info("Setting robot position: %s", robot_params)
            robot_x, robot_y, robot_dir = int(robot_params["x"]), int(robot_params["y"]), int(robot_params["dir"])
            self.car.update_robot(robot_dir, self.grid.grid_to_pixel((robot_x, robot_y)))
            self.car.redraw_car_refresh_screen()

            # Create obstacles given parameters
            logging.info("Creating obstacles...")
            for obstacle in message_data["obs"]:
                logging.info("Obstacle: %s", obstacle)
                id, grid_x, grid_y, dir = obstacle["id"], int(obstacle["x"]), int(obstacle["y"]), int(obstacle["dir"])
                self.grid.create_obstacle([id, grid_x, grid_y, dir])

            # Update grid, start explore
            self.car.redraw_car_refresh_screen()

            logging.info("[AND] Doing path calculation...")
            self.start_button_clicked()

    def on_receive_update_robot_pose(self, message_data: dict):
        print("Received updated robot pose")
        status = message_data["status"]
        if status == "DONE":
            self.path_planner.update_num_move_completed(message_data["num_move"])
        else:
            raise ValueError("Unimplemented response for updated robot pose")

    def reprint_screen_and_buttons(self):
        self.screen.fill(constants.SIMULATOR_BG)
        self.panel.redraw_buttons()

    def check_button_clicked(self, pos):
        # Check if start button was pressed first:
        start_button = self.panel.buttons[-1]
        x, y, l, h = start_button.get_xy_and_lh()
        if (x < pos[0] < (l + x)) and (y < pos[1] < (h + y)):
            self.start_button_clicked()
            return

        for button in self.panel.buttons[0:-1]:
            x, y, l, h = button.get_xy_and_lh()
            if (x < pos[0] < (l + x)) and (y < pos[1] < (h + y)):
                button_func = self.panel.get_button_clicked(button)
                if button_func == "RESET":
                    print("Reset button pressed.")
                    self.reset_button_clicked()
                if button_func == "CONNECT":
                    print("Connect button pressed.")
                    self.start_algo_client()
                elif button_func == "DISCONNECT":
                    print("Disconnect button pressed.")
                    self.comms.disconnect()
                    constants.RPI_CONNECTED = False
                    self.comms = None

                # for testing purposes
                elif button_func == "FORWARD":
                    self.car.move_forward()
                elif button_func == "BACKWARD":
                    self.car.move_backward()
                elif button_func == "FORWARD_RIGHT":
                    self.car.move_forward_steer_right()
                elif button_func == "FORWARD_LEFT":
                    self.car.move_forward_steer_left()
                elif button_func == "BACKWARD_RIGHT":
                    self.car.move_backward_steer_right()
                elif button_func == "BACKWARD_LEFT":
                    self.car.move_backward_steer_left()
                else:
                    return
            else:
                pass

    def start_button_clicked(self):
        print("START button clicked!")

        # Get fastest route (currently not using this)
        self.astar = AStar(self.grid, self.car.grid_x, self.car.grid_y)

        # Get fastest route using AStar Hamiltonian
        if len(self.grid.get_target_locations()) != 0:
            self.astar_hamiltonian = AStarHamiltonian(self.grid, self.car.grid_x, self.car.grid_y)
            graph = self.astar_hamiltonian.create_graph()
            self.hamiltonian_path_planner = ExhaustiveHamiltonianPathPlanner(graph, "start")
            shortest_path, path_length = self.hamiltonian_path_planner.find_path()
            optimized_fastest_route = self.astar_hamiltonian.convert_shortest_path_to_ordered_targets(shortest_path)
            logging.info("Astar route: " + str(optimized_fastest_route))

            self.car.optimized_target_locations = optimized_fastest_route[1:]
            logging.info("Optimized Astar route: " + str(optimized_fastest_route))

            # Path finding
            self.path_planner = PathPlan(self, self.grid, self.car, optimized_fastest_route)
            logging.debug("Fastest Route: ", optimized_fastest_route)
            self.path_planner.start_robot()

    def reset_button_clicked(self):
        self.grid.reset_data()
        self.redraw_grid()
        self.car.reset()

if __name__ == "__main__":
    # Set info logging mode
    logging.basicConfig(level=logging.INFO)

    x = Simulator()

    # Test the method to parse Android messages
    message = "START/EXPLORE/(R,1,1,0)/(00,04,15,-90)/(01,16,17,90)/(02,12,11,180)/(03,07,03,0)/(04,17,04,90)"
    data_dict = x.parser.parse(message)["data"]
    # Test the threading without Android connected
    thread = threading.Thread(target=lambda: x.on_receive_start_task_message(data_dict))
    thread.start()
    x.run()
