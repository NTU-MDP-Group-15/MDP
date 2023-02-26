import re

txt = "0: 480x640 1 0, 1 40 1814.6ms"

x = re.findall("(1\s\d{1,2})", txt)

print(x[0], x[1])