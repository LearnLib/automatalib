package net.automatalib.counterExamples.CounterExampleSolver.GraphGenerator;

import net.automatalib.graphs.ContextFreeModalProcessSystem;
import net.automatalib.graphs.base.DefaultCFMPS;
import net.automatalib.graphs.base.compact.CompactPMPG;
import net.automatalib.graphs.base.compact.CompactPMPGEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class GraphGenerator {

    static Logger logger = LoggerFactory.getLogger(GraphGenerator.class);
    public ArrayList<DefaultCFMPS<String, Void>> cfmpsList;

    //benchmark stuff
    public ArrayList<Long> constructionTimes = new ArrayList();

    public GraphGenerator(boolean test) throws IOException {
        cfmpsList = new ArrayList<>();

        String pathname = ""; //insert path to GraphData here
        if(test){
            pathname += "\\tests";
        }
        File folder = new File(pathname);

        for (File f: folder.listFiles()) {
            if(!f.isDirectory()) {
                FileInputStream seed = new FileInputStream(folder.toPath() + "\\" + f.getName());
                try {
                    long startTime = System.nanoTime();
                    ContextFreeModalProcessSystem<String, Void> cfmps = ExternalSystemDeserializer.parse(seed);

                    cfmpsList.add( (DefaultCFMPS<String, Void>) cfmps);
                    long endTime = System.nanoTime();

                    constructionTimes.add(endTime - startTime);
                } catch (Exception e) {
                    logger.info("exception while adding");
                }
            }
        }
        logger.debug("added " + cfmpsList.size() + " objects to list");
    }

    public void printEdges(CompactPMPG pmpg) {
        Iterator nodeIterator = pmpg.getNodes().iterator();
        while(nodeIterator.hasNext()) {
            int node = (int) nodeIterator.next();
            Iterator edgeIterator = pmpg.getOutgoingEdges(node).iterator();

            while(edgeIterator.hasNext()) {
                CompactPMPGEdge edge = (CompactPMPGEdge) edgeIterator.next();
                System.out.println("from " + node + " to " + edge.getTarget() + " with label " + edge.getLabel());
            }
        }
    }
}
