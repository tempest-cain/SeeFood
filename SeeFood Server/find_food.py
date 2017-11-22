"""
A script to ask SeeFood if it sees food in the image at 
path specified by the command line argument.

For CEG 4110 by: Derek Doran
contact: derek.doran@wright.edu
Aug 9 2017

Changes made from original file made by James Thacker
"""
import argparse
import numpy as np
import tensorflow as tf
from PIL import Image
import sys
import io
import struct
import socket

# Create a socket to send the filename of a picture to analyze
s = socket.socket()
host = 'localhost'
port = 4000
s.connect((host,port))

#######################################################
####### CHANGE PATHS BELOW WHEN MOVING TO SERVER
#######################################################

###### Initialization code - we only need to run this once and keep in memory.
sess = tf.Session()
#saver = tf.train.import_meta_graph('/home/james/SeeFood/CEG4110-Fall2017/saved_model/model_epoch5.ckpt.meta')
#saver.restore(sess, tf.train.latest_checkpoint('/home/james/SeeFood/CEG4110-Fall2017/saved_model/'))

saver = tf.train.import_meta_graph('/home/ec2-user/seefood-core-ai/saved_model/model_epoch5.ckpt.meta')
saver.restore(sess, tf.train.latest_checkpoint('/home/ec2-user/seefood-core-ai/saved_model/'))

graph = tf.get_default_graph()
x_input = graph.get_tensor_by_name('Input_xn/Placeholder:0')
keep_prob = graph.get_tensor_by_name('Placeholder:0')
class_scores = graph.get_tensor_by_name("fc8/fc8:0")
######

# Send Server notice that the AI is ready
f = s.makefile()
f.write(struct.pack(">b", True))
f.flush()

loop = True

# Loop to Analyze pictures from Server
while(loop):

    try:
        # Receive filename of picture to analyze
        image_path = s.recv(256)


    # Work in RGBA space (A=alpha) since png's come in as RGBA, jpeg come in as RGB
    # so convert everything to RGBA and then to RGB.

        image = Image.open(image_path)  # .convert('RGB')
        image = image.resize((227, 227), Image.BILINEAR)
        img_tensor = [np.asarray(image, dtype=np.float32)]
    except Exception:
        break

    # Run the image in the model.
    scores = sess.run(class_scores, {x_input: img_tensor, keep_prob: 1.})

    # Calcuate AI confidence rating as a percent expressed as an integer
    confidence = int(round(max(scores[0][0], scores[0][1]) / (abs(scores[0][0]) + abs(scores[0][1])) * 100))

    isFood = 0

    if (scores[0][0] > scores[0][1]):
        isFood = 1

    f.write(struct.pack(">i", isFood))
    f.write(struct.pack(">i", confidence))
    f.flush()

    # Confirm that find_food executed correctly to server
    f.write(struct.pack(">b", True))
    f.flush()

    # Create filename of results file to create
    path = image_path[0:-3]
    path = path + "bin"

    # Actual Results file, binary file
    file = open(path, "wb")

    file.write(struct.pack('>i', isFood))

    # Write the AI confidence rating to the Results file
    file.write(struct.pack('>i', confidence))
    file.close()


