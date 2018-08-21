package com.github.megachucky.kafka.streams.machinelearning;

import java.util.Arrays;

import hex.genmodel.GenModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.AutoEncoderModelPrediction;
import io.confluent.ksql.function.udf.Udf;
import io.confluent.ksql.function.udf.UdfDescription;


@UdfDescription(name = "anomaly", description = "anomaly detection using deep learning")
public class Anomaly {
	
	
	// Model built with H2O R API:
	  // anomaly_model <- h2o.deeplearning(x = names(train_ecg),training_frame =
	  // train_ecg,activation = "Tanh",autoencoder = TRUE,hidden =
	  // c(50,20,50),sparse = TRUE,l1 = 1e-4,epochs = 100)

	  // Name of the generated H2O model
	  private static String modelClassName = "io.confluent.ksql.function.udf.ml"
	                                         + ".DeepLearning_model_R_1509973865970_1";	
	
  @Udf(description = "apply analytic model to sensor input")
  public String anomaly(String sensorinput) {
	  
	  System.out.println("Kai: DL-UDF starting");
	     
	  GenModel rawModel;
	    try {
			rawModel = (hex.genmodel.GenModel) Class.forName(modelClassName).newInstance();
		
	    EasyPredictModelWrapper model = new EasyPredictModelWrapper(rawModel);
	    
	    // Prepare input sensor data to be in correct data format for the autoencoder model (double[]):
		String[] inputStringArray = sensorinput.split("#");
		double[] doubleValues = Arrays.stream(inputStringArray)
	            .mapToDouble(Double::parseDouble)
	            .toArray();
	    
	    RowData row = new RowData();
	    int j = 0;
	    for (String colName : rawModel.getNames()) {
	      row.put(colName, doubleValues[j]);
	      j++;
	    }

	    AutoEncoderModelPrediction p = model.predictAutoEncoder(row);
	    // System.out.println("original: " + java.util.Arrays.toString(p.original));
	    // System.out.println("reconstructedrowData: " + p.reconstructedRowData);
	    // System.out.println("reconstructed: " + java.util.Arrays.toString(p.reconstructed));

	    double sum = 0;
	    for (int i = 0; i < p.original.length; i++) {
	      sum += (p.original[i] - p.reconstructed[i]) * (p.original[i] - p.reconstructed[i]);
	    }
	    // Calculate Mean Square Error => High reconstruction error means anomaly
	    double mse = sum / p.original.length;
	    System.out.println("MSE: " + mse);

	    String mseString = "" + mse;

	    return (mseString);
	    
	    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
	    	System.out.println(e.toString());
			
		} catch (PredictException e) {
			System.out.println(e.toString());
		}
	    
	    return null;
	  
  }


}

