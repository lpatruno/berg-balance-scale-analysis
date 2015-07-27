from numpy import genfromtxt
import numpy as np
from matplotlib import pyplot as plt

f_name = '/Users/luigi/Documents/Gait/example_signal_processing_scripts/sample.csv'

# Method to extract data from the csv file
sensor_data = genfromtxt(f_name, delimiter=',')

# Get (end-start) number of samples
start = 100
end = 2148

# Use list comprehensions for data
s_ts_l = [sensor_data[i][0] for i in range(start, end)]
s_x_acc_l = [sensor_data[i][1] for i in range(start, end)]
s_y_acc_l = [sensor_data[i][2] for i in range(start, end)]
s_z_acc_l = [sensor_data[i][3] for i in range(start, end)]


# Apply FFT to the data
# NOTE: This is not a good way to filter the data. This is just to showcase the
# methods.

x_fft = np.fft.fft(s_x_acc_l)
y_fft = np.fft.fft(s_y_acc_l)
z_fft = np.fft.fft(s_z_acc_l)

x_bp = x_fft[:]  
y_bp = y_fft[:] 
z_bp = z_fft[:] 

pass_freq = 10

for i in range(len(x_bp)):
    if i >= pass_freq:
        x_bp[i] = 0  

for i in range(len(y_bp)):
    if i >= pass_freq:
        y_bp[i] = 0 

for i in range(len(z_bp)):
    if i >= pass_freq:
        z_bp[i] = 0 
        
x_ibp = np.fft.ifft(x_bp) 
y_ibp = np.fft.ifft(y_bp) 
z_ibp = np.fft.ifft(z_bp) 

print len(x_ibp)

# Visualize the data with matplotlib
# r = rows, c = cols, i = counter for the subplots

r=3
c=2

plt.subplot(r,c,1)
plt.title("Original x accel")  
plt.plot(s_ts_l,s_x_acc_l, color='red')  
plt.subplot(r,c,2)
plt.title("x accel low-pass (" + str(pass_freq) + "Hz)")  
plt.plot(x_ibp, color='red')

plt.subplot(r,c,3)
plt.title("Original y accel")  
plt.plot(s_ts_l,s_y_acc_l, color='blue')  
plt.subplot(r,c,4)
plt.title("y accel low-pass (" + str(pass_freq) + "Hz)")  
plt.plot(y_ibp, color='blue')

plt.subplot(r,c,5)
plt.title("Original z accel")  
plt.plot(s_ts_l,s_z_acc_l, color='green')  
plt.subplot(r,c,6)
plt.title("z accel low-pass (" + str(pass_freq) + "Hz)")  
plt.plot(z_ibp, color='green')

# Show the plot
plt.show()



