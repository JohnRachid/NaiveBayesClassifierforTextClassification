
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.Math;
import java.util.ArrayList;

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
       // for(int i = 0; i <trainingDocuments.length;i++ ){
            //System.out.println(trainingDocuments[i].getActualType());
       // }

        evaluateModelOnTraining(categoryArray);
        evaluateModelOnTesting(categoryArray);

    }

    public static long findTotalNumberOfDistinctWords() throws IOException {
        long count = Files.lines(Paths.get(vocabPath)).count();
        return count;
    }

    private static long findNumberLineForTrainingLabel() throws IOException {

        return Files.lines(Paths.get(trainLabelPath)).count();


    }

    private static long findNumberLineForTestingLabel() throws IOException {
        return Files.lines(Paths.get(testLabelPath)).count();
    }

    private static void fillTrainingDocuments(int numLines) {
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

                    trainingDocuments[currentLabelLine].addToWordsInDocument(Integer.parseInt(trainDataLine[1]));
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

    } //TODO incorrect creating a new document every line

    private static void fillTestingDocuments(int numLines) {
        //trainLabel = new int[numLines];
        testingDocuments = new Document[numLines];
        //testLabel = new int[numLines];
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
                //tempDoc.setPredictedType(); = Integer.parseInt(labelLine);
                currentLine++;
            }
            while ((dataLine = brData.readLine()) != null) {
                String[] testDataLine = dataLine.split(DELIMITER);   //trainDataLine[1] = wordID, trainDataLine[2] = word count

                for (int i = 0; i < Integer.parseInt(testDataLine[2]); i++) { //for everytime that word is in the document
                    currentLabelLine = Integer.parseInt(testDataLine[0]) - 1;
                    //wordsByClass[testLabel[currentLabelLine] - 1].add(Integer.parseInt(trainDataLine[1]));
                    testingDocuments[currentLabelLine].addToWordsInDocument(Integer.parseInt(testDataLine[1]));
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

    } //TODO incorrect creating a new document every line

    /**
     * This method goes through every category of documents and adds every word of a category to the second dimension of the array.
     * Both train data and train label will be needed as train label contains which document the train data is for.
     *
     * @return
     */
    private static Category[] excelToArrayOfList(int numLinesForTrainData, Category[] categoryArray) { //row of train label represents doc id category
        trainLabel = new int[numLinesForTrainData];
        int currentLine = 0;
        int currentLabelLine;
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

    private static Category[] calculateDocumentsPerClass(Category[] categoryArray) {
        double totalDocuments = trainLabel.length;
        int currentLabelLine = 0;
        for (int i = 0; i < trainLabel.length; i++) {
            categoryArray[trainLabel[i] - 1].incrementDocumentsInClass();
        }
        for (int i = 0; i < categoryArray.length; i++) {
            categoryArray[i].setPrior(categoryArray[i].getDocumentsInClass() / totalDocuments); //Prior of class = number of docs for that class / total documents
        }
        for (int i = 0; i < categoryArray.length; i++) {
            System.out.println("Prior Document" + (i + 1) + ":" + categoryArray[i].getPrior()); //Prior of class = number of docs for that class / total documents
        }
        return categoryArray;
    }

    private static void evaluateModelOnTraining(Category[] categoryArray) { // argmax_wj [ln(class prior) + the sum of ln (BE/MLE)" instead of
        double[] maximumLiklihoodEstimator;
        double[] bayesianEstimator;
        double maxValue;
        double bayesianValue;
        double currentPrior;
        double currentClassValue;
        double currentWordValue;
        Boolean compute;
        int highestClass;
        double highestClassValue;
        int countCorrect = 0;
        int word;


        for (int i = 0; i < trainingDocuments.length; i++) { // per document
            highestClass = 0;
            highestClassValue = 0;
            currentClassValue = 0;

            for (int j = 0; j < categoryArray.length; j++) { //per class
                compute = true;
                maximumLiklihoodEstimator = categoryArray[j].getMaximumLikelihoodEstimator();
                bayesianEstimator = categoryArray[j].getBayesianEstimator();
                currentPrior = categoryArray[j].getPrior();
                compute = true;



                for (int k = 0; k < trainingDocuments[i].wordsSize(); k++) { // per word

                    ArrayList<Integer> wordsInDoc = trainingDocuments[i].getWordsInDocument();
                    word = wordsInDoc.get(k);


//                    System.out.println("word value"+word);
//                    System.out.println("k value:" + k);
                    if (maximumLiklihoodEstimator[word] != 0) {
                        maxValue = maximumLiklihoodEstimator[word];
                        bayesianValue = bayesianEstimator[word];


                            currentWordValue = bayesianValue / maxValue;
                            currentClassValue = currentWordValue + currentClassValue;
                            //  System.out.println(currentWordValue);


                    } else {
                        compute = false;
                    }


                }
                if (compute) {
                    currentClassValue = currentPrior + currentClassValue;
                   // System.out.println("class "+ (j+1)+ " = "+currentClassValue);
                } else {
                    currentClassValue = 0;
                    //System.out.println("class " + (i + 1) + " = " + "0");
                }
                if (currentClassValue > highestClassValue) {
                    highestClass = j;
                    highestClassValue = currentClassValue;
                }
                currentClassValue = 0;
            }

            trainingDocuments[i].setPredictedType(highestClass + 1);
            //System.out.print("Actual: " + trainingDocuments[i].getActualType() + "Predicted: " + trainingDocuments[i].getPredictedType() +"\n");

            if (trainingDocuments[i].getPredictedType() == trainingDocuments[i].getActualType()) {
                countCorrect++;
            }
        }

        System.out.println("Training data set: " + countCorrect + " correct out of " + trainingDocuments.length);

    }

    private static void evaluateModelOnTesting(Category[] categoryArray) { // argmax_wj [ln(class prior) + the sum of ln (BE/MLE)" instead of
        double[] maximumLiklihoodEstimator;
        double[] bayesianEstimator;
        double maxValue;
        double bayesianValue;
        double currentPrior;
        double currentClassValue;
        double currentWordValue;
        Boolean compute;
        int highestClass;
        double highestClassValue;
        int countCorrect = 0;
        int word;


        for (int i = 0; i < testingDocuments.length; i++) { // per document
            highestClass = -1;
            currentClassValue = 0;
            highestClassValue = 0;

            for (int j = 0; j < categoryArray.length; j++) { //per class
                maximumLiklihoodEstimator = categoryArray[j].getMaximumLikelihoodEstimator();
                bayesianEstimator = categoryArray[j].getBayesianEstimator();
                currentPrior = categoryArray[j].getPrior();
                compute = true;




                for (int k = 0; k < testingDocuments[i].wordsSize(); k++) { // per word
                    currentWordValue = 0;
                    ArrayList<Integer> wordsInDoc = testingDocuments[i].getWordsInDocument();

                    word = wordsInDoc.get(k);
                    if (maximumLiklihoodEstimator[word] != 0) {
                        maxValue = maximumLiklihoodEstimator[word];
                        bayesianValue = bayesianEstimator[word];

                       // if (compute) {
                            currentWordValue = bayesianValue / maxValue;
                            currentClassValue = currentWordValue + currentClassValue;
                            //System.out.println(currentWordValue);

                       // }
                    } else {
                        //compute = false;
                    }
                }
                if (compute) {
                    currentClassValue = currentPrior + currentClassValue;
                    System.out.println("class "+ (j+1)+ " = "+ currentClassValue);
                } else{
                    currentClassValue = 0;
                }
                if (currentClassValue > highestClassValue) {
                    highestClass = j;
                    highestClassValue = currentClassValue;
                }
                currentClassValue = 0;
            }
            //System.out.println(highestClass);
            testingDocuments[i].setPredictedType(highestClass + 1);
             System.out.print("Actual: " + testingDocuments[i].getActualType() + "Predicted: " + testingDocuments[i].getPredictedType() +"\n");
            if (testingDocuments[i].getPredictedType() == testingDocuments[i].getActualType()) {
                countCorrect++;
            }
        }

        System.out.println("Testing data set: " + countCorrect + " correct out of " + testingDocuments.length);

    }
}


