import csv
import numpy as np
from sklearn.decomposition import PCA 
import pylab as pl
import sys

#inputPath is the path of input file
#result path is the path of result file

inputpath = "/Users/falgunibharadwaj/Downloads/iyer.txt"
resultpath = "/Users/falgunibharadwaj/Downloads/iyer_result.txt"
inputreader = csv.reader(open(inputpath),delimiter="\t")
ip = list(inputreader)
for i in ip:
    del i[0]
    del i[1]
    
resultreader= reader = csv.reader(open(resultpath),delimiter="\t")
op=list(resultreader)   

ip=np.array(ip)
ip=ip.astype(np.float)
op=np.array(op)
op=op.astype(np.float)


pca=PCA(n_components=2)

X_r=pca.fit(ip).transform(ip)

pl.scatter(X_r[:,0],X_r[:,1],c=op) 
pl.legend(loc='best', shadow=False, scatterpoints=1)
pl.title('PCA') 
pl.show()