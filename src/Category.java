import java.util.ArrayList;


public class Category {
    private static final double VOCABLENGTH = 61188.0;
    private int numWordsInClass = 0;
    private int documentsInClass = 0;
    private ArrayList<Integer> wordsInClass;
    private double[] timesWordAppearsInClass;
    private double[] maximumLikelihoodEstimator;
    private double[] bayesianEstimator;
    private int categoryNumber;
    private double prior = 0;

    Category(int categoryNum) {

        categoryNumber = categoryNum;
        wordsInClass = new ArrayList<Integer>();
        timesWordAppearsInClass = new double[61189];
        maximumLikelihoodEstimator = new double[61189];
        bayesianEstimator = new double[61189];

    }

    double[] getBayesianEstimator() {
        return bayesianEstimator;
    }

    double[] getMaximumLikelihoodEstimator() {
        return maximumLikelihoodEstimator;
    }

    int getDocumentsInClass() {
        return documentsInClass;
    }

    double getPrior() {
        return prior;
    }

    public double getValueFromTimeswordAppearsInClass(int value) {
        return timesWordAppearsInClass[value];
    }

    void setPrior(double prior) {
        this.prior = prior;
    }

    public int getFromWordsInClass(int indexToGet) {
        return wordsInClass.get(indexToGet);
    }

    void addToWordsInClass(int numberToAdd) {
        wordsInClass.add(numberToAdd);
    }

    void incrementDocumentsInClass() {
        documentsInClass = documentsInClass + 1;
    }

    int getSizeOfWordsInClass() {
        return wordsInClass.size();
    }


    public int getNumWordsInClass() {
        return numWordsInClass;
    }

    void setNumWordsInClass(int numWordsInClass) {
        this.numWordsInClass = numWordsInClass;
    }


    public int getCategoryNumber() {
        return categoryNumber;
    }

    public void setCategoryNumber(int categoryNumber) {
        this.categoryNumber = categoryNumber;
    }

    void calculateNumTimesWordOccurs() { //nk
        for (int i = 0; i < wordsInClass.size(); i++) {
            timesWordAppearsInClass[wordsInClass.get(i)] = timesWordAppearsInClass[wordsInClass.get(i)] + 1;
            // System.out.println(timesWordAppearsInClass[0]);
            if (i == 7) {
                // System.out.println(timesWordAppearsInClass[wordsInClass.get(i)]); //this isint printing out the correct number of times a specific word appears
            }
        }
    }

    void calculateMaximumLikelihood() { //Pmle(wk|wj) = nk/n
        for (int i = 0; i < timesWordAppearsInClass.length; i++) {
            maximumLikelihoodEstimator[i] = timesWordAppearsInClass[i] / numWordsInClass;
            //System.out.println(maximumLikelihoodEstimator[i]);
        }
    }

    void calculateBayesianEstimator() {
        for (int i = 0; i < timesWordAppearsInClass.length; i++) { //use laplace to find Pbe(wk|wj) = nk+1 / v + words in vocab
            bayesianEstimator[i] = (timesWordAppearsInClass[i] + 1) / (numWordsInClass + VOCABLENGTH);
            //System.out.println(bayesianEstimator[i]);
        }
    }
}
