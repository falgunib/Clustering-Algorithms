import numpy as np
import csv
from numpy import linalg as la

#Uncomment next 2 lines if you want to ask user to enter path at the begining of program
path = raw_input('Enter file path: ')
dataset = open(path,'rb').readlines()

#e = 3
#r = 2
#print dataset.shape

e = int(raw_input('Enter value of e: '))
r = float(raw_input('Enter value of r: '))

#Path of each file
#dataset = open("/Users/falgunibharadwaj/Downloads/physics_collaboration_net.txt",'rb').readlines()
#dataset = open("/Users/falgunibharadwaj/Downloads/yeast_undirected_metabolic.txt",'rb').readlines()
#dataset = open("/Users/falgunibharadwaj/Downloads/attweb_net.txt",'rb').readlines()

node_id = {} 
dupl_size = 0
unique_size = 0

left = []
right = []
unique_nodes = []

#Creating matrix
#Adiing data into lists from input file
for pw in dataset:
	p = pw.split()
	left.append(p[0])
	right.append(p[1])
	if p[0] not in unique_nodes:
		unique_nodes.append(p[0])
		unique_size = unique_size + 1
	if p[1] not in unique_nodes:
		unique_nodes.append(p[1])
		unique_size = unique_size + 1

	dupl_size += 1

print "Size of input nodes is " + str(unique_size)
print unique_nodes

name_to_id = 0
d = 0

#dictionary mapping of each node to its respective id in the order in which it appears
for i in range(0, unique_size):
	z = unique_nodes[i]
	if z in node_id:
		d = d + 1
	else:
		node_id[z] = name_to_id
		name_to_id = name_to_id + 1

size = unique_size
a = np.zeros((size, size))

#adding self loop and edges connection wise
for i in range(0, dupl_size):
	x = left[i]
	y = right[i]
	a[node_id[x]][node_id[y]] = 1
	a[node_id[y]][node_id[x]] = 1
	a[node_id[x]][node_id[x]] = 1
	a[node_id[y]][node_id[y]] = 1

print "Matrix created!"


#Normalization
col_sum = a.sum(axis=0)
#print col_sum

a = a/col_sum
new_a = np.zeros((size, size))
prev = a
#print "prev"
#print prev
i = 0
#i to keep track of iterations

print "Starting Markov Clustering!"
#starting the mcl algo of expansion, inflation, normalization
while(np.array_equal(a,new_a)!=True):
	a = prev
	#print "in for loop"
	#print i
	
	#print a
	#print "other matrix: "
	#print new_a
	
	#!! Expansion
	a2 = np.linalg.matrix_power(a, e)	

	#!! Inflation
	a2 = np.power(a2,r)
	#print a2
	new_sum = a2.sum(axis=0)
	#print "This is new sum"
	#print new_sum

	#!! Normalization
	new_a = a2/new_sum 
	#new_a = new_a.round(4)
	#print "Final new_a"
	#print new_a
	prev = new_a

	if i==35: 
		break
	i = i+1
	
print "Finished!"
print "Iterations: " + str(i) 

#Creating clusters
cluster = {}
count = []
print "Creating Clusters!"
size_new_a = len(new_a)
for i in range(size_new_a):
	#print i
	for j in range(size_new_a):
		#print j
		#print a[i][j]
		if i != j and new_a[i][j] != 0.0:
			cluster[j] = i	
			if i not in count:
				count.append(i)		

t = 0
for i in range(size_new_a):
	if i in cluster:
		t = t + 1

	else:
		cluster[i] = i

print cluster
print "Number of clusters: " + str(len(count))
print count

#Storing clusters in .clu file
type = "_" + str(e) + "_" + str(r)
result_clusters = open(("result" + type + ".clu"),'w')
result_clusters.write("*Vertices " + str(len(new_a)))
result_clusters.write("\n")

for i in range(size_new_a):
	result_clusters.write(str(cluster[i]) + "\n")

print "Output file successfully created!"
result_clusters.close()
