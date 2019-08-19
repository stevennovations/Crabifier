# Crab Classifier Training Module

This module makes use of Convolutional Neural Networks.

It is trained to detect which classification of Mud Crab is present. 

The Classifications are 
	- Olivacea
	- Serrata
	- Tranquebarica

The Image dataset is composed of 200 images and were augmented before the training.

The training will have 2 sets one training set and one validation set will follow the 80/20 rule.

There will be 3 convolutional layer and will use softmax to get the probability of the network.

The training is set to 300 iterations but can be edited at training.

To Run the application

	1. Clone the Repository
	2. Change the Folder Path of the Model in line 199
	3. Run `python train.py`

Some improvements.

	- Augmentation of the images could be added to the training script
	- Arguments could be implemented within the script.
	- Have not tried to use Cloud Systems that could train my neural network

The Model is used in the App Crabifier
	- https://play.google.com/store/apps/details?id=edu.dlsu.censer.crabifier&hl=en
