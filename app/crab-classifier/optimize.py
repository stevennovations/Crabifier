import tensorflow as tf
import sys
import os

graph_def_file = "/tmp/output_graph.pb"
input_arrays = ["Placeholder"] 
output_arrays = ["final_result"]
converter = tf.lite.TFLiteConverter.from_frozen_graph(
  str(graph_def_file), input_arrays, output_arrays)
converter.optimizations = [tf.lite.Optimize.OPTIMIZE_FOR_SIZE]
resnet_tflite_file = "mobnet_v2_101_quantized.tflite"
resnet_tflite_file.write_bytes(converter.convert())