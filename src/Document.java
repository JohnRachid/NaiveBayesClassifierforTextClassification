import java.util.ArrayList;

class Document {
    private int number;
    private int actualType;
    private int predictedType;
    private ArrayList<Integer> wordsInDocument;

    Document(int number) {
        wordsInDocument = new ArrayList<Integer>();
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
        this.predictedType = predictedType;
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
