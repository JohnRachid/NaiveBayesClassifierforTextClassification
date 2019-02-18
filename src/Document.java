import java.util.ArrayList;

class Document {
    private int actualType;
    private int predictedType;
    private ArrayList<Integer> wordsInDocument;

    /**
     * a documents representation. Each document that is created has a array list of each word in the document and the label of the document
     * @param number the actualType of the document according to the label csv
     */
    Document(int number) {
        wordsInDocument = new ArrayList<Integer>(); // this is a arraylist of all words in this document
        actualType = number;
    }

    int getActualType() {
        return actualType;
    }

    void setActualType(int actualType) {
        actualType = actualType;
    }

    int getPredictedType() {
        return predictedType;
    }

    void setPredictedType(int predictedType) {
        this.predictedType = predictedType; //sets the estimated type of the document
    }

    ArrayList<Integer> getWordsInDocument() {
        return wordsInDocument;
    }

    void addToWordsInDocument(int wordNumber) {
        wordsInDocument.add(wordNumber);
    }

    int wordsSize() {
        return wordsInDocument.size();
    }

}
