import tensorflow as tf
from tensorflow.python.tools import freeze_graph
from tensorflow.python.tools import optimize_for_inference_lib

freeze_graph.freeze_graph(input_graph = 'crab.pbtxt',  
						input_saver = '',
             			input_binary = False, 
             			input_checkpoint = 'crab-model', 
             			output_node_names = 'y_pred',
            			restore_op_name = 'save/restore_all', 
            			filename_tensor_name = 'save/const:0',
             			output_graph = 'crab-mode.pb', 
             			clear_devices = True, 
             			initializer_nodes = '',)

input_graph_def = tf.GraphDef()
with tf.gfile.Open('crab-mode.pb', 'rb') as f:
    data = f.read()
    input_graph_def.ParseFromString(data)

output_graph_def = optimize_for_inference_lib.optimize_for_inference(
        input_graph_def= input_graph_def,
        input_node_names= ['x'], 
        output_node_names= ['y_pred'],
        placeholder_type_enum= tf.float32.as_datatype_enum)

f = tf.gfile.FastGFile("crab-model2.pb", "w")
f.write(output_graph_def.SerializeToString())