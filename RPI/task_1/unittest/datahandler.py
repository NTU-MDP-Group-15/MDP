import modules
import unittest

class TestDataHandler(unittest.TestCase):
     
     def test_algo_to_stm(self):
          dh = modules.DataHandler()
          s1 = "['1', 'B010', 'FR010', 'F050', 'FL010']"
          a1 = "11010,03010,01050,02010"
          s2 = "['2', 'F010', 'BL020', 'B020', 'BR010', 'F010']"
          a2 = "01010,12020,11020,13010,01010"
          
          self.assertEqual(dh.algo_to_stm(s1), a1)
          self.assertEqual(dh.algo_to_stm(s2), a2)

if __name__ == '__main__':
    unittest.main()