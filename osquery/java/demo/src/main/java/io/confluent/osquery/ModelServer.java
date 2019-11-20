package io.confluent.osquery;

import cc.mallet.pipe.SerialPipes;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import spark.Request;

import javax.servlet.MultipartConfigElement;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;

import static spark.Spark.*;

/**
 * Model Decorator
 */
class ModelDecorator implements Serializable {
    private ParallelTopicModel model;
    private Calendar creation;
    private String id = "";
    public ModelDecorator(Calendar creation, ParallelTopicModel model) {
        this.creation = creation;
        this.model = model;
        this.id = UUID.randomUUID().toString();
    }
    public ParallelTopicModel getModel() {
        return model;
    }

    public void setModel(ParallelTopicModel model) {
        this.model = model;
    }
    public Calendar getCreation() {
        return creation;
    }

    public void setCreation(Calendar creation) {
        this.creation = creation;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.toString().equals(this.toString());
    }
}

/**
 * Model Server
 *
 */
public class ModelServer
{
    private static Stack<ModelDecorator> models = new Stack<>(); // extends Vector which is synchronized
    private static ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

    public static void main( String[] args )
    {
        exec.schedule(() -> clean(), 1, TimeUnit.MINUTES);
        initExceptionHandler((e) -> System.out.println("Uh-oh"));

        /**
         *  http://localhost:4567/status
         */
        get("/status", (req, res) -> String.format("Latest model: %s",
                models.empty() ? "empty" : models.peek().getCreation().toString()));

        delete("/model/rollback", (req, res) -> {
            if(!models.empty()) {
                ModelDecorator mm = models.pop();
                return String.format("rolledback model [%tT]", mm.getCreation());
            }
            else
                return "No models are available";
        });

        get("/model/latest/score", (req, res) -> score(req));

        post("/model/latest", (request, response) -> {
            request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
            try (InputStream is = request.raw().getPart("uploaded_file").getInputStream()) {
                ObjectInputStream in = new ObjectInputStream(is);
                models.push(new ModelDecorator(Calendar.getInstance(), (ParallelTopicModel)in.readObject()));
            }
            return "Model uploaded";
        });
    }

    private static void clean() {
        while(models.size() > 5) {
            models.remove(models.firstElement());
        }
    }

    private static double score(Request req) {
        String doc = req.body();
        if(!models.isEmpty()) return -1;
        ParallelTopicModel m = models.peek().getModel();
        ArrayList pipeList = new ArrayList();
        SerialPipes sp = new SerialPipes(pipeList);
        InstanceList event = new InstanceList(sp);
        event.addThruPipe(new Instance(doc, null, "instance", null));

        TopicInferencer inferencer = m.getInferencer();
        double[] probabilities = inferencer.getSampledDistribution(event.get(0), 10, 1, 5);

        DoubleStream stream = Arrays.stream(probabilities);
        return stream.max().getAsDouble(); // find the max probability. we don't care which topic it belongs
    }
}
