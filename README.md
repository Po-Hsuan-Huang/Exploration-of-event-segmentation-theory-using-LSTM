# Exploration-of-event-segmentation-theory-using-LSTM
Publication : https://www.researchgate.net/publication/306323997_Exploration_of_event_segmentation_theory_using_LSTM  

Java code folder contains Java projects, while Matlab code folder contains files of data plotting for the thesis.

Check The Wiki for further information of each Java project.



Abstract: People tend to perceive ongoing continuous activity as a series of discrete events. Event Segmentation Theory (EST) postulates humans systematically partition continuous sensorimotor information flow into events and event boundaries (Reynolds, Zacks, and Braver, 2006). Gumbsch et.al. (Gumbsch and , 2016) investigated the basis of EST in the own motor interaction capabilities, and provided a computational model that learned events and event transitions while interacting with the environment. Their architecture uses a linear forward model as event models. We proposed that Long Short Term Memory (LSTM) neural network can augment the learning capability of forward models, and learn event transitions by forming gates. Gates are the cells of LSTM whose states switch on/off when the agent detects the event boundary. The computational model can then use the gates to predict event transitions and plan actions to achieve goals. We investigated the proper parameters for gate formation in a simple scenario. These parameters include length of buffer sequence, duration of training, and size of hidden layer. We also investigated the effect of weights, and found several classes of gates. We found weights on peephole to forget gate, input neuron to output gate, and cell output to output gate the defining weights of gate formation. They selectively open and close input gates, forget gates, and output gates at event boundaries. We also found output cell the most eligible candidate of gates for event boundary prediction. This findings can serve as guidelines of designing computational models that based on LSTM .
