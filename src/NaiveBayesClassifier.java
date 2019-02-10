import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import static java.lang.Math.toIntExact;


//train and test data are formatted in document ID, word ID corresponding to vocabulary.txt,
// and  count is the frequency the word appears in the document

//train and test label is a list of label id's indicating which newsgroup each document belongs to.

//map.csv maps from label id's to label names


public class NaiveBayesClassifier{
    private static final int NUMTOPICS = 20;
    private static final String mapPath = "data/map.csv";
    private static final String trainDataPath = "data/train_data.csv";
    private static final String testDataPath = "data/test_data.csv";
    private static final String trainLabelPath = "data/train_label.csv";
    private static final String testLabelPath = "data/test_label.csv";
    private static final String vocabPath = "data/vocabulary.txt";
    private static final String DELIMITER = ",";
    private static int[] trainLabel;


    public static void main(String[] args) throws IOException {
        Category[] categoryArray = new Category[20];
        for(int i = 0; i < categoryArray.length; i++){
            categoryArray[i] = new Category(i);
        }
        //categoryArray[1].getFromWordsInClass(50);

        categoryArray = excelToArrayOfList(toIntExact(findNumberLineForLabel()),categoryArray);
        categoryArray = calculateDocumentsPerClass(categoryArray);
        for(int i = 0; i < categoryArray.length; i++){
            categoryArray[i].calculateNumTimesWordOccurs();
            categoryArray[i].calculateMaximumLikelihood();

        }

    }
    public static long findTotalNumberOfDistinctWords() throws IOException {
        int numwords = 0;
        long count = Files.lines(Paths.get(vocabPath)).count();
        return count;
    }

    public static long findNumberLineForLabel() throws IOException {
        int numwords = 0;
        long count = Files.lines(Paths.get(trainLabelPath)).count();

        return count;
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

                for(int i = 0; i < Integer.parseInt(trainDataLine[2]); i++){ //for everytime that word is in the document
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
        for(int i = 0; i < categoryArray.length; i++){
            categoryArray[i].setNumWordsInClass(categoryArray[i].getSizeOfWordsInClass());
        }
        return categoryArray;

    }

    public static Category[] calculateDocumentsPerClass(Category[] categoryArray) {
        double totalDocuments = trainLabel.length;
        int currentLabelLine = 0;
        for(int i = 0; i < trainLabel.length; i++){
          categoryArray[trainLabel[i]-1].incrementDocumentsInClass();
        }
        for(int i = 0; i < categoryArray.length; i++){
            categoryArray[i].setProir(categoryArray[i].getDocumentsInClass() / totalDocuments); //Prior of class = number of docs for that class / total documents
        }
        for(int i = 0; i < categoryArray.length; i++){
            System.out.println("Prior Document" + (i + 1) + ":" + categoryArray[i].getProir()); //Prior of class = number of docs for that class / total documents
        }
        return categoryArray;
    }
}


