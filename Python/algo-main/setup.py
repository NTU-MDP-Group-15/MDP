from setuptools import setup

setup(name='mdpalgo',
      version='1.0',
      description='Algorithm for MDP',
      author='Group 27',
      packages=['mdpalgo', 'fastestalgo'],
      install_requires=[
        'pygame',
        'networkx',
        'parse',
        'numpy',
        'imagerec',
        'opencv-python'
      ]
     )
