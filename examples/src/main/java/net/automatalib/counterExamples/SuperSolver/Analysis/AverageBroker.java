package net.automatalib.counterExamples.SuperSolver.Analysis;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class AverageBroker {
    public static void main(String[] args) throws FileNotFoundException {
        try {
            for(int j=0;j<4;j++) {
                PrintWriter writer = new PrintWriter(new FileOutputStream(
                        new File(System.getProperty("user.dir") + "//GraphAVG_" + j + ".txt"),
                        true /* append = true */));
                CSVReader reader = new CSVReader(new FileReader(System.getProperty("user.dir") + "//GraphTEST_" + j + ".txt"));
                List<String[]> r = reader.readAll();
                int[] toAverage = {0, 0, 0, 0, 0, 0, 0, 0};
                double[] m3cTimes = {0, 0, 0, 0, 0, 0, 0, 0};
                double[] treeTimes = {0, 0, 0, 0, 0, 0, 0, 0};
                double[] pathTimes = {0, 0, 0, 0, 0, 0, 0, 0};
                for (String[] line : r) {
                    switch (line[0]) {
                        case "1":
                            toAverage[0]++;
                            m3cTimes[0] += Double.parseDouble(line[1]);
                            treeTimes[0] += Double.parseDouble(line[2]);
                            pathTimes[0] += Double.parseDouble(line[3]);
                            break;
                        case "2":
                            toAverage[1]++;
                            m3cTimes[1] += Double.parseDouble(line[1]);
                            treeTimes[1] += Double.parseDouble(line[2]);
                            pathTimes[1] += Double.parseDouble(line[3]);
                            break;
                        case "3":
                            toAverage[2]++;
                            m3cTimes[2] += Double.parseDouble(line[1]);
                            treeTimes[2] += Double.parseDouble(line[2]);
                            pathTimes[2] += Double.parseDouble(line[3]);
                            break;
                        case "4":
                            toAverage[3]++;
                            m3cTimes[3] += Double.parseDouble(line[1]);
                            treeTimes[3] += Double.parseDouble(line[2]);
                            pathTimes[3] += Double.parseDouble(line[3]);
                            break;
                        case "5":
                            toAverage[4]++;
                            m3cTimes[4] += Double.parseDouble(line[1]);
                            treeTimes[4] += Double.parseDouble(line[2]);
                            pathTimes[4] += Double.parseDouble(line[3]);
                            break;
                        case "6":
                            toAverage[5]++;
                            m3cTimes[5] += Double.parseDouble(line[1]);
                            treeTimes[5] += Double.parseDouble(line[2]);
                            pathTimes[5] += Double.parseDouble(line[3]);
                            break;
                        case "7":
                            toAverage[6]++;
                            m3cTimes[6] += Double.parseDouble(line[1]);
                            treeTimes[6] += Double.parseDouble(line[2]);
                            pathTimes[6] += Double.parseDouble(line[3]);
                            break;
                        case "8":
                            toAverage[7]++;
                            m3cTimes[7] += Double.parseDouble(line[1]);
                            treeTimes[7] += Double.parseDouble(line[2]);
                            pathTimes[7] += Double.parseDouble(line[3]);
                            break;
                    }
                    //System.out.println(Arrays.toString(line));
                }
                for (int i = 0; i < 8; i++) {
                    m3cTimes[i] = Math.log(m3cTimes[i] / toAverage[i]);
                    treeTimes[i] = Math.log(treeTimes[i] / toAverage[i]);
                    pathTimes[i] = Math.log(pathTimes[i] / toAverage[i]);
                }

                for (int i = 0; i < 8; i++) {
                    writer.append((i + 1) + "," + m3cTimes[i] + "," + treeTimes[i] + "," + pathTimes[i] + "\n");
                    writer.flush();
                }
                writer.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvException e) {
            e.printStackTrace();
        }
    }
}
