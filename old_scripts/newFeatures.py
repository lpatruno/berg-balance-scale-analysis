import numpy as np
import matplotlib.pyplot as plt
import scipy.signal as signal

# First, design the Buterworth filter
N  = 2                 # Filter order
low_pass = 15.0        # Cutoff frequency
sampling = 60.0        # Sampling frequency 
Wn = low_pass/(sampling/2)  
B, A = signal.butter(N, Wn, output='ba', analog=0)

# Path to data file
f_name = '/Users/luigi/Documents/Gait/example_signal_processing_scripts/sample.csv'

# Method to extract data from the csv file
sensor_data = np.genfromtxt(f_name, delimiter=',')

# Get (end-start) number of samples
start=100
end = 256 # len(sensor_data)

# Use list comprehensions for raw data
ts = np.array([sensor_data[i][0] for i in range(start, end)])
x_a = np.array([sensor_data[i][1] for i in range(start, end)])

# absolute value
abs_x_a = np.abs(x_a)


# Apply the filter with the SciPy signal module
x_a_butter = signal.filtfilt(B,A, x_a)
abs_x_a_butter = signal.filtfilt(B,A, abs_x_a)

# Take fft of the filtered data
abs_x_fft = np.fft.fft(abs_x_a_butter)


"""
plt.figure(1)
plt.plot(ts, x_a, label='Raw', color='blue')
plt.plot(ts, x_a_butter, label='Filtered', color='red')
plt.legend()


plt.figure(2)
plt.plot(ts, abs_x_a, label='AbsVal', color='blue')
plt.plot(ts, abs_x_a_butter, label='AbsValFilter', color='red')
plt.legend()
"""

plt.subplot(211)
#plt.plot(ts, abs_x_a, label='AbsVal', color='blue')
plt.plot(ts, abs_x_a_butter, label='AbsValFilter', color='red')
plt.legend()

plt.subplot(212)
plt.plot(abs_x_fft, label='fft of filtered')
#plt.xlim(0,5)
plt.legend()

plt.show()


