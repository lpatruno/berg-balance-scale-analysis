#from pandas import Series, DataFrame
#import pandas as pd
import numpy as np

f_name = '/Users/luigi/Documents/Gait/example_signal_processing_scripts/sample.csv'

# Method to extract data from the csv file
sensor_data = np.genfromtxt(f_name, delimiter=',')

# Get (end-start) number of samples
start=0
end = len(sensor_data)

# Use list comprehensions for data
ts = [sensor_data[i][0] for i in range(start, end)]
x_a = [sensor_data[i][1] for i in range(start, end)]
y_a = [sensor_data[i][2] for i in range(start, end)]
z_a = [sensor_data[i][3] for i in range(start, end)]
#x_g = [sensor_data[i][1] for i in range(start, end)]
#y_g = [sensor_data[i][2] for i in range(start, end)]
#z_g = [sensor_data[i][3] for i in range(start, end)]

#   pandas stuff    
#data = {'ts': ts, 'x_a': x_a, 'y_a': y_a, 'z_a': z_a}
#data = {'ts': ts, 'x_a': x_a, 'y_a': y_a, 'z_a': z_a, 'x_g': x_g, 'y_g': y_g, 'z_g': z_g}

#frame = DataFrame(data)

#print frame.describe()
