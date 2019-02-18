import java.util.ArrayList;


public class Category {
    private static final double VOCABLENGTH = 61188.0;
    private int numWordsInClass = 0;
    private int documentsInClass = 0;
    private ArrayList<Integer> wordsInClass;
    private double[] distinctWordsInClass;
    private double[] maximumLikelihoodEstimator;
    private double[] bayesianEstimator;
    private int categoryNumber;
    private double prior = 0;

    Category(int categoryNum) {

        categoryNumber = categoryNum;
        wordsInClass = new ArrayList<Integer>();
        maximumLikelihoodEstimator = new double[61189];
        bayesianEstimator = new double[61189];
        distinctWordsInClass = new double[61189]; //index [x][0] = wordValue , index[x][1] = timesItAppeard

    }

    /**
     * sets the value at indexOne equal to the number to add.
     * @param indexOne the index at which the value will be set equal to number to add
     * @param numberToAdd the number that will be added at indexOne
     */
    void addToDistinctWordsInClass(int indexOne, double numberToAdd){
        distinctWordsInClass[indexOne] = numberToAdd;
    }

    /**
     * adds the value at distinctWordsInClass to itself plus the number that will be added.
     * @param indexOne the index at which the value is being changed
     * @param numberToAdd the number which is being added
     */
    void addAmountToDistinctWordsInClass(int indexOne, double numberToAdd){
        distinctWordsInClass[indexOne] = distinctWordsInClass[indexOne] + numberToAdd;
    }

    double[] getDistinctWordsInClass(){
        return distinctWordsInClass;
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

    void setNumWordsInClass(int numWordsInClass) {
        this.numWordsInClass = numWordsInClass;
    }

    /**
     * calculates the maximum likelihood for each word in the current category.
     */
    void calculateMaximumLikelihood() { //Pmle(wk|wj) = nk/n nk = number of times word occurs in all documents in class n = total number of words in all documents in class wj
        for (int i = 0; i < VOCABLENGTH; i++) {
            maximumLikelihoodEstimator[i] = (distinctWordsInClass[i]) / wordsInClass.size();
        }

    }

    /**
     * calculates the bayestion estimator value for each word in the current category using the laplace estimate
     */
    void calculateBayesianEstimator() {
        for (int i = 0; i < VOCABLENGTH; i++) { //use laplace to find Pbe(wk|wj) = nk+1 / v + words in vocab
            bayesianEstimator[i] = (distinctWordsInClass[i] + 1) / (wordsInClass.size() + VOCABLENGTH);
            //System.out.println(bayesianEstimator[i]);
        }
    }
}
