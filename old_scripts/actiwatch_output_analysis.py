from numpy import genfromtxt
import numpy as np
from matplotlib import pyplot as plt


def distance_data(time_series):
    """
    Return the (average, max, min, std) of consecutive distance between a list of points
    """
    if len(time_series) == 0:
        return (0,0,0)
        
    n = len(time_series)
    dist = []
    
    for i in range(n-1):
        d = time_series[i+1] - time_series[i]
        #d = time_series[i] - time_series[i+1]
        dist.append(d)
    
    avg = sum(dist) / float(len(dist))
    dist.sort()
    
    return (avg, max(dist), min(dist), np.std(dist))
    
###############################################################################    

direc = '/Users/swaggins/Desktop/watch_phone_data/3_6_2015/'

watch_accel = direc + 'test_accel_K_watch.txt'
watch_gyro =  direc + 'test_gyro_K_watch.txt'

phone_accel = direc + 'test_accel_K_phone.txt'
phone_gyro =  direc + 'test_gyro_K_phone.txt'

watch_accel_data = genfromtxt(watch_accel, delimiter=',')
watch_gyro_data = genfromtxt(watch_gyro, delimiter=',')

phone_accel_data = genfromtxt(phone_accel, delimiter=',')
phone_gyro_data = genfromtxt(phone_gyro, delimiter=',')

start_watch_accel = 0
start_watch_gyro = 0
start_phone_accel = 0
start_phone_gyro = 0

end_watch_accel = len(watch_accel_data)
end_watch_gyro = len(watch_gyro_data)
end_phone_accel = len(phone_accel_data)
end_phone_gyro = len(phone_gyro_data)

ts_watch_accel = [watch_accel_data[i][0] for i in range(start_watch_accel, end_watch_accel)]
x_watch_accel = [watch_accel_data[i][1] for i in range(start_watch_accel, end_watch_accel)]
y_watch_accel = [watch_accel_data[i][2] for i in range(start_watch_accel, end_watch_accel)]
z_watch_accel = [watch_accel_data[i][3] for i in range(start_watch_accel, end_watch_accel)]

ts_watch_gyro = [watch_gyro_data[i][0] for i in range(start_watch_gyro, end_watch_gyro)]
x_watch_gyro = [watch_gyro_data[i][1] for i in range(start_watch_gyro, end_watch_gyro)]
y_watch_gyro = [watch_gyro_data[i][2] for i in range(start_watch_gyro, end_watch_gyro)]
z_watch_gyro = [watch_gyro_data[i][3] for i in range(start_watch_gyro, end_watch_gyro)]

ts_phone_accel = [phone_accel_data[i][0] for i in range(start_phone_accel, end_phone_accel)]
x_phone_accel = [phone_accel_data[i][1] for i in range(start_phone_accel, end_phone_accel)]
y_phone_accel = [phone_accel_data[i][2] for i in range(start_phone_accel, end_phone_accel)]
z_phone_accel = [phone_accel_data[i][3] for i in range(start_phone_accel, end_phone_accel)]

ts_phone_gyro = [phone_gyro_data[i][0] for i in range(start_phone_gyro, end_phone_gyro)]
x_phone_gyro = [phone_gyro_data[i][1] for i in range(start_phone_gyro, end_phone_gyro)]
y_phone_gyro = [phone_gyro_data[i][2] for i in range(start_phone_gyro, end_phone_gyro)]
z_phone_gyro = [phone_gyro_data[i][3] for i in range(start_phone_gyro, end_phone_gyro)]


## Time series statistics
watch_accel_stats = distance_data(ts_watch_accel)
print 'Average distance between watch accel (ms): ', watch_accel_stats[0]
print 'Max distance: ', watch_accel_stats[1]
print 'Min distance: ', watch_accel_stats[2]
print 'Standard deviation: ', watch_accel_stats[3]
print 'Number of records: ', len(ts_watch_accel)

print

watch_gyro_stats = distance_data(ts_watch_gyro)
print 'Average distance between watch gyro (ms): ', watch_gyro_stats[0]
print 'Max distance: ', watch_gyro_stats[1]
print 'Min distance: ', watch_gyro_stats[2]
print 'Standard deviation: ', watch_gyro_stats[3]
print 'Number of records: ', len(ts_watch_gyro)
print

phone_accel_stats = distance_data(ts_phone_accel)
print 'Average distance between phone accel (ms): ', phone_accel_stats[0]
print 'Max distance: ', phone_accel_stats[1]
print 'Min distance: ', phone_accel_stats[2]
print 'Standard deviation: ', phone_accel_stats[3]
print 'Number of records: ', len(ts_phone_accel)

print

phone_gyro_stats = distance_data(ts_phone_gyro)
print 'Average distance between phone gyro (ms): ', phone_gyro_stats[0]
print 'Max distance: ', phone_gyro_stats[1]
print 'Min distance: ', phone_gyro_stats[2]
print 'Standard deviation: ', phone_gyro_stats[3]
print 'Number of records: ', len(ts_phone_gyro)


## Plotting data
r=3
c=2
i=1

plt.subplot(r,c,i)
plt.title("x watch accel")
plt.tight_layout()
plt.plot(ts_watch_accel,x_watch_accel, color='blue')
i += 1

plt.subplot(r,c,i)
plt.title("x phone accel")
plt.tight_layout()
plt.plot(ts_phone_accel,x_phone_accel, color='blue')
i += 1

plt.subplot(r,c,i)
plt.title("y watch accel")
plt.tight_layout()
plt.plot(ts_watch_accel, y_watch_accel, color='red')
i += 1

plt.subplot(r,c,i)
plt.title("y phone accel")
plt.tight_layout()
plt.plot(ts_phone_accel, y_phone_accel, color='red')
i += 1

plt.subplot(r,c,i)
plt.title("z watch accel")
plt.tight_layout()
plt.plot(ts_watch_accel, z_watch_accel, color='green')
i += 1

plt.subplot(r,c,i)
plt.title("z phone accel")
plt.tight_layout()
plt.plot(ts_phone_accel, z_phone_accel, color='green')


plt.show()















