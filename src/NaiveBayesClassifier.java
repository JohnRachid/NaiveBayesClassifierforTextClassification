
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
    private static final String trainDataPath = "data/train_data.csv";
    private static final String testDataPath = "data/test_data.csv";
    private static final String trainLabelPath = "data/train_label.csv";
    private static final String testLabelPath = "data/test_label.csv";
    private static final String DELIMITER = ",";
    private static int[] trainLabel;
    private static Document[] trainingDocuments;
    private static Document[] testingDocuments;


    public static void main(String[] args) throws IOException {
        Category[] categoryArray = new Category[20]; //create new array of categories or classes
        for (int i = 0; i < categoryArray.length; i++) {
            categoryArray[i] = new Category(i); //fill the array
        }

        excelToArrayOfList(toIntExact(findNumberLineForTrainingLabel()), categoryArray); //get the words of each category of document into the category representation
        calculateDocumentsPerClass(categoryArray); //find amount of documents in each class. this is used to find the class prior
        for (int i = 0; i < categoryArray.length; i++) {
            categoryArray[i].calculateMaximumLikelihood(); // calculate maximum likelihood estimation for each word in the class
            categoryArray[i].calculateBayesianEstimator(); // calculate bayesian estimation for each word in the class
        }
        fillTrainingDocuments(toIntExact(findNumberLineForTrainingLabel())); //fill training array of documents with each document in the training data and each documents words
        fillTestingDocuments(toIntExact(findNumberLineForTestingLabel()));//fill testing array of documents with each document in the training data and each documents words
        // for(int i = 0; i <trainingDocuments.length;i++ ){
        //System.out.println(trainingDocuments[i].getActualType());
        // }

        evaluateModelOnTraining(categoryArray, true); //evaluate each of the models and output statistics based on their results
        evaluateModelOnTraining(categoryArray, false);
        evaluateModelOnTesting(categoryArray, true);
        evaluateModelOnTesting(categoryArray, false);

    }

    /**
     * finds total amount of lines in the training label set
     *
     * @return total amount of lines in the training label set
     * @throws IOException
     */
    private static long findNumberLineForTrainingLabel() throws IOException {
        return Files.lines(Paths.get(trainLabelPath)).count();
    }

    /**
     * finds total amount of lines in the testing label set
     *
     * @return total amount of lines in the testing label set
     * @throws IOException if the path is invalue
     */
    private static long findNumberLineForTestingLabel() throws IOException {
        return Files.lines(Paths.get(testLabelPath)).count();
    }
    /**
     * fills the training Documents with their words according to train_date.csv and train_label.csv
     * @param numLines the total amount of lines in train_label.csv
     */
    private static void fillTrainingDocuments(int numLines) {
        trainingDocuments = new Document[numLines];
        int currentLine = 0;
        int currentLabelLine;
        BufferedReader brData = null;
        BufferedReader brlabel = null;

        try {
            brData = new BufferedReader(new FileReader(trainDataPath));
            brlabel = new BufferedReader(new FileReader(trainLabelPath));
            String dataLine; //line for the train_data.csv
            String labelLine;//line for the train_label.csv
            while ((labelLine = brlabel.readLine()) != null) {
                Document tempDoc = new Document(Integer.parseInt(labelLine)); //creates a new document with the actual type of the label
                trainingDocuments[currentLine] = tempDoc; //sets the the index of currentLine in trainingdata = to the new doc that was just created.
                currentLine++;
            }
            while ((dataLine = brData.readLine()) != null) {
                String[] trainDataLine = dataLine.split(DELIMITER);   //Splits the data line into a array based on where the commas where in that line. trainDataLine[0] = document id, trainDataLine[1] = wordID, trainDataLine[2] = word count

                for (int i = 0; i < Integer.parseInt(trainDataLine[2]); i++) { //for everytime that word is in the document
                    currentLabelLine = Integer.parseInt(trainDataLine[0]) - 1;
                    trainingDocuments[currentLabelLine].addToWordsInDocument(Integer.parseInt(trainDataLine[1])); //adds to the documents wordsInDocument
                }
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            try {
                brData.close(); //closing the BufferedReaders
                brlabel.close();
            } catch (IOException ie) {
                System.out.println("Error occured while closing the BufferedReaders");
                ie.printStackTrace();
            }
        }

    }

    /**
     * fills the testingDocuments with their words according to test_date.csv and test_label.csv
     * @param numLines the total amount of lines in test_label.csv
     */
    private static void fillTestingDocuments(int numLines) {
        testingDocuments = new Document[numLines];
        int currentLine = 0;
        int currentLabelLine;
        BufferedReader brData = null;
        BufferedReader brlabel = null;

        try {
            brData = new BufferedReader(new FileReader(testDataPath));
            brlabel = new BufferedReader(new FileReader(testLabelPath));
            String dataLine;//line for the test_label.csv
            String labelLine;//line for the test_label.csv
            while ((labelLine = brlabel.readLine()) != null) {
                Document tempDoc = new Document(Integer.parseInt(labelLine));//creates a new document with the actual type of the label
                testingDocuments[currentLine] = tempDoc;//sets the the index of currentLine in trainingdata = to the new doc that was just created.
                currentLine++;
            }
            while ((dataLine = brData.readLine()) != null) {
                String[] testDataLine = dataLine.split(DELIMITER);   //Splits the data line into a array based on where the commas where in that line. testDataLine[0] = document id, testDataLine[1] = wordID, testDataLine[2] = word count

                for (int i = 0; i < Integer.parseInt(testDataLine[2]); i++) { //for everytime that word is in the document
                    currentLabelLine = Integer.parseInt(testDataLine[0]) - 1;
                    testingDocuments[currentLabelLine].addToWordsInDocument(Integer.parseInt(testDataLine[1])); //adds each word to the testingDocuments wordsInDocument
                }
            }
        } catch (Exception ee) {
            ee.printStackTrace();
        } finally {
            try { //close the buffers
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
     *
     */
    private static void excelToArrayOfList(int numLinesForTrainData, Category[] categoryArray) { //row of train label represents doc id category
        trainLabel = new int[numLinesForTrainData];
        int currentLine = 0;
        int currentLabelLine;

        BufferedReader brData = null;
        BufferedReader brlabel = null;
        try {
            brData = new BufferedReader(new FileReader(trainDataPath));
            brlabel = new BufferedReader(new FileReader(trainLabelPath));

            String dataLine;
            String labelLine;
            while ((labelLine = brlabel.readLine()) != null) { // creating a array for the train_label csv data
                trainLabel[currentLine] = Integer.parseInt(labelLine);
                currentLine++;
            }
            while ((dataLine = brData.readLine()) != null) {
                String[] trainDataLine = dataLine.split(DELIMITER);   //trainDataLine[0] = wordID, trainDataLine[1] = word count
                currentLabelLine = Integer.parseInt(trainDataLine[0]) - 1;
                categoryArray[trainLabel[currentLabelLine] - 1].addAmountToDistinctWordsInClass(Integer.parseInt(trainDataLine[1]), Integer.parseInt(trainDataLine[2]));

                for (int i = 0; i < Integer.parseInt(trainDataLine[2]); i++) { //for everytime that word is in the document
                    currentLabelLine = Integer.parseInt(trainDataLine[0]) - 1;
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
    }

    /**
     * calculates the amount of documents per class from the training data according to the training labels. This also caluclates the prior for each class
     *
     * @param categoryArray the array of categories which contain words from each class
     */
    private static void calculateDocumentsPerClass(Category[] categoryArray) {
        double totalDocuments = trainLabel.length;
        for (int i = 0; i < trainLabel.length; i++) {
            categoryArray[trainLabel[i] - 1].incrementDocumentsInClass();
        }
        for (int i = 0; i < categoryArray.length; i++) {
            categoryArray[i].setPrior(categoryArray[i].getDocumentsInClass() / totalDocuments); //Prior of class = number of docs for that class / total documents
        }
        for (int i = 0; i < categoryArray.length; i++) {
            System.out.println("Prior Class " + (i + 1) + ": " + categoryArray[i].getPrior()); //Prior of class = number of docs for that class / total documents
        }
    }

    /**
     * evaluates the trained model on the training data. The performance when evaluating the models on the same data it was trained on should be high.
     * @param categoryArray array of categories
     * @param bayesian if true evaluate the bayesian model if not calculate the maximum likelihood model
     */
    private static void evaluateModelOnTraining(Category[] categoryArray, Boolean bayesian) { // argmax_wj [ln(class prior) + the sum of ln (BE/MLE)" instead of
        double[] maximumLiklihoodEstimator;
        double[] bayesianEstimator;
        double maxValue;
        double bayesianValue;
        double currentPrior;
        double currentClassValue;
        double currentWordValue;
        int highestClass;
        double highestClassValue;
        int countCorrect = 0;
        int word;


        for (int i = 0; i < trainingDocuments.length; i++) { // per document
            highestClass = -2;
            highestClassValue = -50000;
            currentClassValue = 0;

            for (int j = 0; j < categoryArray.length; j++) { //per class
                maximumLiklihoodEstimator = categoryArray[j].getMaximumLikelihoodEstimator(); //maximumLiklihoodEstimator = array of all maximum Liklihood Estimator values for each word in the category
                bayesianEstimator = categoryArray[j].getBayesianEstimator(); //bayesianEstimator = array of all bayesian Estimator values for each word in the category
                currentPrior = categoryArray[j].getPrior(); //gets prior for the current category

                for (int k = 0; k < trainingDocuments[i].wordsSize(); k++) { // per word
                    ArrayList<Integer> wordsInDoc = trainingDocuments[i].getWordsInDocument(); //gets the arraylist of words for each document
                    word = wordsInDoc.get(k); //current word value = the next word in wordsInDoc



                        if (bayesian) {
                            bayesianValue = bayesianEstimator[word]; //gets the current bayesian value for each word
                            currentWordValue = Math.log(bayesianValue);
                        } else {
                            maxValue = maximumLiklihoodEstimator[word];//gets the maximum likelihood estimation value for each word
                            currentWordValue = Math.log(maxValue);
                        }

                        currentClassValue = currentWordValue + currentClassValue; //adds the words value to the current class value
                }
                    currentClassValue = Math.log(currentPrior) + currentClassValue; // adds the log of the current categories prior + the sum on each value for every position in the document

                if (currentClassValue > highestClassValue) { //if the current class value is higher then the highest class value
                    highestClass = j;                        //highest class = current class
                    highestClassValue = currentClassValue;
                }
                currentClassValue = 0; //set current class value to zero to restart the process for the next class
            }
            trainingDocuments[i].setPredictedType(highestClass + 1); //set the highest class to the predicted type. + 1 since it starts at zero instead of one like in the label.csv

            if (trainingDocuments[i].getPredictedType() == trainingDocuments[i].getActualType()) { // if the prediction is correct increment count to get total amount of correct predictions
                countCorrect++;
            }
        }
        if (bayesian) {
            System.out.println("Training data set Bayesian: " + countCorrect + " correct out of " + trainingDocuments.length);
            statistics(countCorrect, trainingDocuments, categoryArray);
        } else {
            System.out.println("Training data set Maximum Likelihood: " + countCorrect + " correct out of " + trainingDocuments.length);
            statistics(countCorrect, trainingDocuments, categoryArray);
        }
    }

    /**
     * evaluates the trained model on the testing data. This performance should be lower then when the models are evaluated on the training data.
     * @param categoryArray array of categories
     * @param bayesian if true evaluate the bayesian model if not calculate the maximum likelihood model
     */
    private static void evaluateModelOnTesting(Category[] categoryArray, Boolean bayesian) { // argmax_wj [ln(class prior) + the sum of ln (BE/MLE)" instead of
        double[] maximumLiklihoodEstimator;
        double[] bayesianEstimator;
        double maxValue;
        double bayesianValue;
        double currentPrior;
        double currentClassValue;
        double currentWordValue;

        int highestClass;
        double highestClassValue;
        int countCorrect = 0;
        int word;

        for (int i = 0; i < testingDocuments.length; i++) { // per document
            highestClass = -2;
            highestClassValue = -50000;
            currentClassValue = 0;

            for (int j = 0; j < categoryArray.length; j++) { //per class
                maximumLiklihoodEstimator = categoryArray[j].getMaximumLikelihoodEstimator();//maximumLiklihoodEstimator = array of all maximum Liklihood Estimator values for each word in the category
                bayesianEstimator = categoryArray[j].getBayesianEstimator();//bayesianEstimator = array of all bayesian Estimator values for each word in the category
                double[] distinctWords = categoryArray[j].getDistinctWordsInClass(); //gets array of amount of word appearances per word per class
                currentPrior = categoryArray[j].getPrior();//gets prior for the current category

                for (int k = 0; k < testingDocuments[i].wordsSize(); k++) { // per word
                    ArrayList<Integer> wordsInDoc = testingDocuments[i].getWordsInDocument(); //gets the arraylist of words for each document
                    word = wordsInDoc.get(k);//current word value = the next word in wordsInDoc

                        if (bayesian) {
                            bayesianValue = bayesianEstimator[word];//gets the current bayesian value for each word
                            currentWordValue = Math.log(bayesianValue);
                        } else {
                            maxValue = maximumLiklihoodEstimator[word];//gets the maximum likelihood estimation value for each word
                            if (distinctWords[word] == 0) { //if the word is not found in the class set currentwordValue to an extremly small value to not count the class
                                currentWordValue = -500000;
                            } else {
                                currentWordValue = Math.log(maxValue);
                            }
                        }
                        currentClassValue = currentWordValue + currentClassValue; //adds the words value to the current class value

                }
                    currentClassValue = Math.log(currentPrior) + currentClassValue;// adds the log of the current categories prior + the sum on each value for every position in the document
                if(currentClassValue > highestClassValue) {//if the current class value is higher then the highest class value
                    highestClass = j;                       //highest class = current class
                    highestClassValue = currentClassValue;
                }
                currentClassValue = 0; //set current class value to zero to restart the process for the next class
            }
            testingDocuments[i].setPredictedType(highestClass + 1);//set the highest class to the predicted type. + 1 since it starts at zero instead of one like in the label.csv
            if (testingDocuments[i].getPredictedType() == testingDocuments[i].getActualType()) {// if the prediction is correct increment count to get total amount of correct predictions
                countCorrect++;
            }
        }
        if (bayesian) {
            System.out.println("Testing data set Bayesian: " + countCorrect + " correct out of " + testingDocuments.length);
            statistics(countCorrect, testingDocuments, categoryArray);
        } else {
            System.out.println("Testing data set Maximum Likelihood: " + countCorrect + " correct out of " + testingDocuments.length);
            statistics(countCorrect, testingDocuments, categoryArray);
        }
    }

    /**
     * calculates the confusion matrix, performance per category and overall accuracy
     * @param numCorrect number of documents which were identified correctly
     * @param documents array of documents
     * @param categoryArray array or categories
     */
    private static void statistics(double numCorrect, Document[] documents, Category[] categoryArray) {
        System.out.println("Overall Accuracy = " + (numCorrect / documents.length));
        System.out.println(" Class Accuracy");
        int[][] arrayOfNumCorrect = new int[21][21];
        int[] oneClassPredicted = new int[21];
        int[] categoryCorrect = new int[21];
        double performance;
        for (int i = 0; i < categoryArray.length; i++) {
            for (int j = 0; j < documents.length; j++) {
                if (documents[j].getPredictedType() == documents[j].getActualType() && documents[j].getActualType() == (i + 1)) {
                    categoryCorrect[i] = categoryCorrect[i] + 1; // if docuent in category was correctly predicted and the current category is being checked increment predicted amount
                }
            }
            performance = ((double) categoryCorrect[i]) / (categoryArray[i].getDocumentsInClass()); //calulated performance of each category
            System.out.println("Group " + (i + 1) + ": " + performance);
        }
        for (int j = 0; j < 20; j++) { //one row = all predicted types of documents per class
            for (int k = 0; k < 20; k++) {
                for (int i = 0; i < documents.length; i++) {
                    if (documents[i].getActualType() == k + 1 && documents[i].getPredictedType() != -1) {
                        oneClassPredicted[documents[i].getPredictedType() - 1] += 1; //builds each row of the confusion matrix per class
                    }
                }
                for (int q = 0; q < 20; q++) {
                    arrayOfNumCorrect[k][q] = oneClassPredicted[q]; //copies oneclassPredicted to a row of arrayOfNumCorrect
                }
                oneClassPredicted = new int[20]; //empties oneClassPredicted
            }
        }

        for (int j = 0; j < 20; j++) {
            for (int k = 0; k < 20; k++) {
                System.out.format("%3d ", (arrayOfNumCorrect[j][k])); //prints out confusion matrix with proper spacing
            }
            System.out.println();
        }
        System.out.println();
    }

}




