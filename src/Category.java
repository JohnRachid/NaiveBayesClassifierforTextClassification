import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import java.io.FileReader;


public class Category  {
    private static final double VOCABLENGTH = 61188.0;
    private int numWordsInClass = 0;
    private int documentsInClass = 0;
    protected ArrayList<Integer> wordsInClass;
    protected double[] timesWordAppearsInClass;
    protected double[] maximumLikelihoodEstimator;
    protected double[] bayesianEstimator;
    private int categoryNumber;
    private double proir = 0;

    public Category(int categoryNum) {

        categoryNumber = categoryNum;
        wordsInClass = new ArrayList<Integer>();
        timesWordAppearsInClass = new double[61188];
        maximumLikelihoodEstimator = new double[61188];
        bayesianEstimator = new double[61188];

    }
    public double[] getBayesianEstimator(){
       return bayesianEstimator;
    }
    public double[] getMaximumLikelihoodEstimator(){
        return maximumLikelihoodEstimator;
    }

    public int getDocumentsInClass() {
        return documentsInClass;
    }

    public double getProir() {
        return proir;
    }

    public void setProir(double proir) {
        this.proir = proir;
    }

    public int getFromWordsInClass(int indexToGet) {
        return wordsInClass.get(indexToGet);
    }

    public void addToWordsInClass(int numberToAdd) {
        wordsInClass.add(numberToAdd);
    }

    public void incrementDocumentsInClass() {
        documentsInClass = documentsInClass + 1;
    }

    public int getSizeOfWordsInClass() {
        return wordsInClass.size();
    }


    public int getNumWordsInClass() {
        return numWordsInClass;
    }

    public void setNumWordsInClass(int numWordsInClass) {
        this.numWordsInClass = numWordsInClass;
    }


    public int getCategoryNumber() {
        return categoryNumber;
    }

    public void setCategoryNumber(int categoryNumber) {
        this.categoryNumber = categoryNumber;
    }

    public void calculateNumTimesWordOccurs(){ //nk
        for(int i = 0; i < wordsInClass.size(); i++){
            timesWordAppearsInClass[wordsInClass.get(i)] = timesWordAppearsInClass[wordsInClass.get(i)] + 1;

        }
    }
    public void calculateMaximumLikelihood(){ //Pmle(wk|wj) = nk/n
        for(int i = 0; i < timesWordAppearsInClass.length; i++){
            maximumLikelihoodEstimator[i] = timesWordAppearsInClass[i] / numWordsInClass;
            //System.out.println(maximumLikelihoodEstimator[i]);
        }
    }

    public void calculateBayesianEstimator(){
        for(int i = 0; i < timesWordAppearsInClass.length; i++){ //use laplace to find Pbe(wk|wj) = nk+1 / v + words in vocab
            bayesianEstimator[i] = (timesWordAppearsInClass[i] + 1) / (numWordsInClass + VOCABLENGTH);
            //System.out.println(bayesianEstimator[i]);
        }
    }
}
