import tensorflow as tf
from tensorflow.python.tools import freeze_graph, optimize_for_inference_lib

freeze_graph.freeze_graph(input_graph='crab.pbtxt',
                          input_saver='',
                          input_binary=False,
                          input_checkpoint='crab-model',
                          output_node_names='y_pred',
                          restore_op_name='save/restore_all',
                          filename_tensor_name='save/Const:0',
                          output_graph='crb1.pb',
                          clear_devices=True,
                          initializer_nodes='',
                          variable_names_blacklist='')

input_graph_def = tf.GraphDef()
with tf.gfile.Open('crb1.pb', 'rb') as f:
    data = f.read()
    input_graph_def.ParseFromString(data)

output_graph_def = optimize_for_inference_lib.optimize_for_inference(input_graph_def=input_graph_def,
                                                                     input_node_names=['x'],
                                                                     output_node_names=['y_pred'],
                                                                     placeholder_type_enum=tf.float32.as_datatype_enum)

f = tf.gfile.FastGFile(name='ocrb1.pb',
                       mode='w')
f.write(file_content=output_graph_def.SerializeToString())
