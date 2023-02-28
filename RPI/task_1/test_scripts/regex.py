import re
MOVEMENT_DICT = {
    "F": "01",
    "FL": "02",
    "FR": "03", 
    "B": "11",
    "BL":  "12",
    "BR": "13",
}

class test():
    def __init__(self):
        self.pattern = re.compile(r"([a-zA-Z]+)(\d+)")
        
    def algo_to_stm(self, data):
        str_instr = str
        mvmt_str = "{mvmt_id}{mvmt_dist}"

        match = self.pattern.match(data[0])
        str_instr = mvmt_str.format(mvmt_id=MOVEMENT_DICT[match.group(1)], 
                                    mvmt_dist=match.group(2))
        for instr in data[1:]:
            match = self.pattern.match(instr)
            str_instr +=  "," + mvmt_str.format(mvmt_id=MOVEMENT_DICT[match.group(1)], 
                                                mvmt_dist=match.group(2))

        return str_instr
        
        
def algo_to_stm(data) -> str:
        """
        Args:
            data (list): list of individual movements provided by Algorithm Team
                         ['F010', 'FL090']

        Returns:
            str: movement string "01010,02090..."
        """
        str_instr = str
        mvmt_str = "{mvmt_id}{mvmt_dist}"

        pattern = re.compile(r"([a-zA-Z]+)(\d+)")
        match = pattern.match(data[0])
        str_instr = mvmt_str.format(mvmt_id=MOVEMENT_DICT[match.group(1)], 
                                    mvmt_dist=match.group(2))
        for instr in data[1:]:
            match = pattern.match(instr)
            str_instr +=  "," + mvmt_str.format(mvmt_id=MOVEMENT_DICT[match.group(1)], 
                                                mvmt_dist=match.group(2))

        return str_instr


def main():
    ar1 = 'F020', 'FR090', 'FL090'
    print(algo_to_stm(ar1))
    t1 = test()
    print(t1.algo_to_stm(ar1))


if __name__=="__main__":
    main()