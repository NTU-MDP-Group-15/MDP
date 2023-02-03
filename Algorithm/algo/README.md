# Algorithm package for MDP

## Install the package in development mode

Ensure you have cloned and install `imagerec` package from
[this repo](https://github.com/CZ3004Group27/image_rec).

In the root folder containing `setup.py` file, run

```sh
pip install -r requirements.txt
pip install -e .
```

The flag `-e` is short for --editable, and `.`  refers to the current working
directory, so together, it means to install the current directory
in editable mode. This will also install any dependencies declared with install_requires

## Week 8 task

**Note:** Before running the main file, make sure to remove the images in "images" folder.

```sh
python -m mdpalgo
```

## Week 9 task

**Note:** Before running the main file, make sure to remove the images in "images" folder.

```sh
python -m fastestalgo
```

## Details to note

The arena:

- The map is a 20 by 20 grid of cells
- Each cell represents 10cm x 10cm

The robot:

- Modeled as a 3x3 on the grid
- Actual robot size is about 20cm x 21cm
- Turning radius:
  - Notes suggested about 25cm turning radius
  - In the simulator, the turning radius used is 3x3 which is 30cm by 30cm

The "obstacle" model:

- Physical size is identical to size on grid (1x1) (10cm x 10cm)
- Obstacle border given for astar path planning is about 20cm (2 cells away)

## References

This repo is based upon a previous
repo: https://github.com/CZ3004-Group-12/mdp-algorithm working on the same
project in the past. Having to keep the developments private during CZ3004 course,
we did not fork the repo, but we tried to keep the commit history intact.

We have contributed significantly, especially on the following aspects:

1. Fix the path planning algorithm: the A star algorithm in the previous repo
   did not immediately generate a sequence of feasible movements, but an
   idealised path for a holonomic robot, and converted to the actual path using
   a lot of hardcoding. Lots of bugs in the actual path, and the car sometimes crashed
   into the obstacle. We have completely revamped the A start path planning
   algorithm to immediately generate a sequence of feasible movements, with no
   collision in the simulator.

2. Develop our own approach for week 9 task. The algorithm side simply handles
   communication with RPi and calls the image recognition and leaves
   the planning to the RPi and STM.

3. Repackage the entire repo as two packages that are easy to install and
   deploy.

We also added a lot of unit tests and fixed numerous bugs in the process.
