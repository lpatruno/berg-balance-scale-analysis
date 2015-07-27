from numpy import genfromtxt
import numpy as np
from matplotlib import pyplot as plt
import scipy.signal as signal

f_name = '/Users/luigi/Documents/Gait/example_signal_processing_scripts/sample.csv'

# Method to extract data from the csv file
sensor_data = genfromtxt(f_name, delimiter=',')

# Get (end-start) number of samples
start=100
end = 356

# Use list comprehensions for data
s_ts_l = [sensor_data[i][0] for i in range(start, end)]
s_x_acc_l = [sensor_data[i][1] for i in range(start, end)]
s_y_acc_l = [sensor_data[i][2] for i in range(start, end)]
s_z_acc_l = [sensor_data[i][3] for i in range(start, end)]

s_x_gyro_l = [sensor_data[i][4] for i in range(start, end)]
s_y_gyro_l = [sensor_data[i][5] for i in range(start, end)]
s_z_gyro_l = [sensor_data[i][6] for i in range(start, end)]


# First, design the Buterworth filter
N  = 2                 # Filter order
low_pass = 15.0        # Cutoff frequency
sampling = 60.0        # Sampling frequency 
Wn = low_pass/(sampling/2)  
B, A = signal.butter(N, Wn, output='ba', analog=0)

# Second, apply the filter with the SciPy signal module
xab = signal.filtfilt(B,A, s_x_acc_l)
yab = signal.filtfilt(B,A, s_y_acc_l)
zab = signal.filtfilt(B,A, s_z_acc_l)

xgb = signal.filtfilt(B,A, s_x_gyro_l)
ygb = signal.filtfilt(B,A, s_y_gyro_l)
zgb = signal.filtfilt(B,A, s_z_gyro_l)

# Third, visualize the data with matplotlib;)

# r = rows, c = cols, i = counter for the subplots
r=2
c=3
i=1

plt.subplot(r,c,i)
plt.title("x accel")  
plt.plot(s_ts_l,s_x_acc_l, color='red', label='Signal')  
plt.plot(s_ts_l, xab, color='blue', label='Filter')
plt.legend()

i=i+1

plt.subplot(r,c,i)
plt.title("y accel")  
plt.plot(s_ts_l,s_y_acc_l, color='red', label='Signal')  
plt.plot(s_ts_l, yab, color='blue', label='Filter')

i=i+1

plt.subplot(r,c,i)
plt.title("z accel")  
plt.plot(s_ts_l,s_z_acc_l, color='red', label='Signal')  
plt.plot(s_ts_l, zab, color='blue', label='Filter')

i=i+1

plt.subplot(r,c,i)
plt.title("x gyro")  
plt.plot(s_ts_l,s_x_gyro_l, color='red', label='Signal')  
plt.plot(s_ts_l, xgb, color='blue', label='Filter')
plt.legend()

i=i+1

plt.subplot(r,c,i)
plt.title("y gyro")  
plt.plot(s_ts_l,s_y_gyro_l, color='red', label='Signal')  
plt.plot(s_ts_l, ygb, color='blue', label='Filter')

i=i+1

plt.subplot(r,c,i)
plt.title("z gyro")  
plt.plot(s_ts_l,s_z_gyro_l, color='red', label='Signal')  
plt.plot(s_ts_l, zgb, color='blue', label='Filter')

# Show the plot
plt.show()
