import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.Math;
import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.Math.toIntExact;


//train and test data are formatted in document ID, word ID corresponding to vocabulary.txt,
// and  count is the frequency the word appears in the document

//train and test label is a list of label id's indicating which newsgroup each document belongs to.

//map.csv maps from label id's to label names


public class NaiveBayesClassifier {
    private static final int NUMTOPICS = 20;
    private static final String mapPath = "data/map.csv";
    private static final String trainDataPath = "data/train_data.csv";
    private static final String testDataPath = "data/test_data.csv";
    private static final String trainLabelPath = "data/train_label.csv";
    private static final String testLabelPath = "data/test_label.csv";
    private static final String vocabPath = "data/vocabulary.txt";
    private static final String DELIMITER = ",";
    private static int[] trainLabel;
    private static int[] testLabel;
    private static final double VOCABLENGTH = 61188.0;
    private static Document[] trainingDocuments;
    private static Document[] testingDocuments;



    public static void main(String[] args) throws IOException {
        Category[] categoryArray = new Category[20];
        for (int i = 0; i < categoryArray.length; i++) {
            categoryArray[i] = new Category(i);
        }

        categoryArray = excelToArrayOfList(toIntExact(findNumberLineForTrainingLabel()), categoryArray);
        categoryArray = calculateDocumentsPerClass(categoryArray);
        for (int i = 0; i < categoryArray.length; i++) {
            categoryArray[i].calculateNumTimesWordOccurs();
            categoryArray[i].calculateMaximumLikelihood();
            categoryArray[i].calculateBayesianEstimator();
        }
        fillTrainingDocuments(toIntExact(findNumberLineForTrainingLabel()));
        fillTestingDocuments(toIntExact(findNumberLineForTestingLabel()));
        evaluateModelOnTraining(categoryArray);
        evaluateModelOnTesting(categoryArray);

    }

    public static long findTotalNumberOfDistinctWords() throws IOException {
        int numwords = 0;
        long count = Files.lines(Paths.get(vocabPath)).count();
        return count;
    }

    public static long findNumberLineForTrainingLabel() throws IOException {
        int numwords = 0;
        long count = Files.lines(Paths.get(trainLabelPath)).count();

        return count;
    }
    public static long findNumberLineForTestingLabel() throws IOException {
        int numwords = 0;
        long count = Files.lines(Paths.get(testLabelPath)).count();

        return count;
    }

    public static void fillTrainingDocuments(int numLines){
        trainingDocuments = new Document[numLines];
        int documentNumber;
        int wordNumber;
        int documentType;
        int currentLine = 0;
        int currentLabelLine = 0;
        BufferedReader brData = null;
        BufferedReader brlabel = null;

        try {
            brData = new BufferedReader(new FileReader(trainDataPath));
            brlabel = new BufferedReader(new FileReader(trainLabelPath));
            String dataLine = "";
            String labelLine = "";
            while ((labelLine = brlabel.readLine()) != null) {
                Document tempDoc = new Document(Integer.parseInt(labelLine));
                trainingDocuments[currentLine] = tempDoc;
                currentLine++;
            }
            while ((dataLine = brData.readLine()) != null) {
                String[] trainDataLine = dataLine.split(DELIMITER);   //trainDataLine[1] = wordID, trainDataLine[2] = word count

                for (int i = 0; i < Integer.parseInt(trainDataLine[2]); i++) { //for everytime that word is in the document
                    currentLabelLine = Integer.parseInt(trainDataLine[0]) - 1;
                    //wordsByClass[trainLabel[currentLabelLine] - 1].add(Integer.parseInt(trainDataLine[1]));
                    trainingDocuments[trainLabel[currentLabelLine] - 1].addToWordsInDocument(Integer.parseInt(trainDataLine[1]));
                   // System.out.println("temp");
                }
            }
            //add Document here
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            try {
                brData.close();
                brlabel.close();
            } catch (IOException ie) {
                System.out.println("Error occured while closing the BufferedReaders");
                ie.printStackTrace();
            }
        }

    }

    public static void fillTestingDocuments(int numLines){
        testingDocuments = new Document[numLines];
        int documentNumber;
        int wordNumber;
        int documentType;
        int currentLine = 0;
        int currentLabelLine = 0;
        BufferedReader brData = null;
        BufferedReader brlabel = null;

        try {
            brData = new BufferedReader(new FileReader(testDataPath));
            brlabel = new BufferedReader(new FileReader(testLabelPath));
            String dataLine = "";
            String labelLine = "";
            while ((labelLine = brlabel.readLine()) != null) {
                Document tempDoc = new Document(Integer.parseInt(labelLine));
                testingDocuments[currentLine] = tempDoc;
                currentLine++;
            }
            while ((dataLine = brData.readLine()) != null) {
                String[] trainDataLine = dataLine.split(DELIMITER);   //trainDataLine[1] = wordID, trainDataLine[2] = word count

                for (int i = 0; i < Integer.parseInt(trainDataLine[2]); i++) { //for everytime that word is in the document
                    currentLabelLine = Integer.parseInt(trainDataLine[0]) - 1;
                    //wordsByClass[trainLabel[currentLabelLine] - 1].add(Integer.parseInt(trainDataLine[1]));
                    testingDocuments[trainLabel[currentLabelLine] - 1].addToWordsInDocument(Integer.parseInt(trainDataLine[1]));
                     //System.out.println(train);
                }
            }
            //add Document here
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            try {
                brData.close();
                brlabel.close();
            } catch (IOException ie) {
                System.out.println("Error occured while closing the BufferedReaders");
                ie.printStackTrace();
            }
        }

    }

    /**
     * This method goes through every category of documents and adds every word of a category to the second dimension of the array.
     * Both train data and train label will be needed as train label contains which document the train data is for.
     *
     * @return
     */
    public static Category[] excelToArrayOfList(int numLinesForTrainData, Category[] categoryArray) throws IOException { //row of train label represents doc id category
        trainLabel = new int[numLinesForTrainData];
        int currentLine = 0;
        int currentLabelLine = 0;
//        List<Integer>[] wordsByClass = new List[20];
//        Arrays.setAll(wordsByClass, element -> new ArrayList<>());


        BufferedReader brData = null;
        BufferedReader brlabel = null;
        try {
            brData = new BufferedReader(new FileReader(trainDataPath));
            brlabel = new BufferedReader(new FileReader(trainLabelPath));

            String dataLine = "";
            String labelLine = "";
            while ((labelLine = brlabel.readLine()) != null) { // creating a array for the train_label csv data
                trainLabel[currentLine] = Integer.parseInt(labelLine);
                currentLine++;
            }
            while ((dataLine = brData.readLine()) != null) {
                String[] trainDataLine = dataLine.split(DELIMITER);   //trainDataLine[1] = wordID, trainDataLine[2] = word count

                for (int i = 0; i < Integer.parseInt(trainDataLine[2]); i++) { //for everytime that word is in the document
                    currentLabelLine = Integer.parseInt(trainDataLine[0]) - 1;
                    //wordsByClass[trainLabel[currentLabelLine] - 1].add(Integer.parseInt(trainDataLine[1]));
                    categoryArray[trainLabel[currentLabelLine] - 1].addToWordsInClass(Integer.parseInt(trainDataLine[1]));
                }
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            try {
                brData.close();
                brlabel.close();
            } catch (IOException ie) {
                System.out.println("Error occured while closing the BufferedReaders");
                ie.printStackTrace();
            }
        }
        for (int i = 0; i < categoryArray.length; i++) {
            categoryArray[i].setNumWordsInClass(categoryArray[i].getSizeOfWordsInClass());
        }
        return categoryArray;

    }

    public static Category[] calculateDocumentsPerClass(Category[] categoryArray) {
        double totalDocuments = trainLabel.length;
        int currentLabelLine = 0;
        for (int i = 0; i < trainLabel.length; i++) {
            categoryArray[trainLabel[i] - 1].incrementDocumentsInClass();
        }
        for (int i = 0; i < categoryArray.length; i++) {
            categoryArray[i].setProir(categoryArray[i].getDocumentsInClass() / totalDocuments); //Prior of class = number of docs for that class / total documents
        }
        for (int i = 0; i < categoryArray.length; i++) {
            System.out.println("Prior Document" + (i + 1) + ":" + categoryArray[i].getProir()); //Prior of class = number of docs for that class / total documents
        }
        return categoryArray;
    }

    public static void evaluateModelOnTraining(Category[] categoryArray) { // argmax_wj [ln(class prior) + the sum of ln (BE/MLE)" instead of
        double[] maximumLiklihoodEstimator;
        double[] bayesianEstimator;
        double maxValue = 0;
        double bayesianValue = 1;
        double currentPrior = 0;
        double currentClassValue;
        double currentWordValue = 0;
        double totalWordValues;
        Boolean compute = true;
        int count;
        int highestClass;
        double highestClassValue;
        int countCorrect = 0;
        int word;


        for(int i = 0; i < trainingDocuments.length; i++){ // per document
            highestClass = 0;
            highestClassValue = 0;

            for(int j = 0; j < categoryArray.length; j++){ //per class
                compute = true;
                maximumLiklihoodEstimator = categoryArray[j].getMaximumLikelihoodEstimator();
                bayesianEstimator = categoryArray[j].getBayesianEstimator();
                currentPrior = categoryArray[j].getProir();
                currentClassValue = 0;


                totalWordValues = 0;
                for(int k = 0; k < trainingDocuments[i].wordsSize(); k++){ // per word
                    maxValue = 0;

                    ArrayList<Integer> wordsInDoc = trainingDocuments[i].getWordsInDocument();
                    currentWordValue = 0;
                    maxValue = 0;
                    bayesianValue = 0;
                    word = wordsInDoc.get(k);

                    compute = true;
                    if(wordsInDoc.get(word) != 0) {
                        maxValue = maximumLiklihoodEstimator[word];
                        bayesianValue = bayesianEstimator[word];

                        if (compute) {
                            currentWordValue = Math.log(bayesianValue / maxValue);
                            currentClassValue = currentWordValue + currentClassValue;
                            //  System.out.println(currentWordValue);

                        }
                    }else{
                        compute = false;
                    }


                }
                if(compute){
                    currentClassValue = Math.log(currentPrior) + currentClassValue;
                    //System.out.println("class "+ (i+1)+ " = "+currentClassValue);
                }else{
                    System.out.println("class "+ (i+1)+ " = "+ "0");
                }
                if(currentClassValue > highestClassValue){
                    highestClass = j;
                }
        }
           // System.out.print("Actual: " + trainingDocuments[i].getActualType() + "Predicted: " + trainingDocuments[i].getPredictedType() +"\n");
            trainingDocuments[i].setPredictedType(highestClass);
            if(trainingDocuments[i].getPredictedType() == trainingDocuments[i].getActualType()){
               countCorrect++;
            }
    }

        System.out.println("Training data set: " +countCorrect + " correct out of "+ trainingDocuments.length);

}

    public static void evaluateModelOnTesting(Category[] categoryArray) { // argmax_wj [ln(class prior) + the sum of ln (BE/MLE)" instead of
        double[] maximumLiklihoodEstimator;
        double[] bayesianEstimator;
        double maxValue = 0;
        double bayesianValue = 1;
        double currentPrior = 0;
        double currentClassValue;
        double currentWordValue = 0;
        double totalWordValues;
        Boolean compute = true;
        int count;
        int highestClass;
        double highestClassValue;
        int countCorrect = 0;
        int word;


        for(int i = 0; i < testingDocuments.length; i++){ // per document
            highestClass = 0;
            highestClassValue = 0;

            for(int j = 0; j < categoryArray.length; j++){ //per class
                compute = true;
                maximumLiklihoodEstimator = categoryArray[j].getMaximumLikelihoodEstimator();
                bayesianEstimator = categoryArray[j].getBayesianEstimator();
                currentPrior = categoryArray[j].getProir();
                currentClassValue = 0;


                totalWordValues = 0;
                for(int k = 0; k < testingDocuments[i].wordsSize(); k++){ // per word
                    maxValue = 0;

                    ArrayList<Integer> wordsInDoc = testingDocuments[i].getWordsInDocument();
                    currentWordValue = 0;
                    maxValue = 0;
                    bayesianValue = 0;

                    compute = true;

                    word = wordsInDoc.get(k);
                    //System.out.println(word);
                    if(wordsInDoc.get(word) != 0) {
                        maxValue = maximumLiklihoodEstimator[word];
                        bayesianValue = bayesianEstimator[word];

                        if (compute) {
                            currentWordValue = Math.log(bayesianValue / maxValue);
                            currentClassValue = currentWordValue + currentClassValue;
                            //  System.out.println(currentWordValue);

                        }
                    }else{
                        compute = false;
                    }


                }
                if(compute){
                    currentClassValue = Math.log(currentPrior) + currentClassValue;
                    //System.out.println("class "+ (i+1)+ " = "+currentClassValue);
                }else{
                    //System.out.println("class "+ (i+1)+ " = "+ "0");
                }
                if(currentClassValue > highestClassValue){
                    highestClass = j;
                }
            }
            testingDocuments[i].setPredictedType(highestClass);
           // System.out.print("Actual: " + testingDocuments[i].getActualType() + "Predicted: " + testingDocuments[i].getPredictedType() +"\n");
            if(testingDocuments[i].getPredictedType() == testingDocuments[i].getActualType()){
                countCorrect++;
            }
        }

        System.out.println("Testing data set: " +countCorrect + " correct out of "+ testingDocuments.length);

    }
}


