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


public class Category extends NaiveBayesClassifier {
    private int numWordsInClass = 0;

    public int getDocumentsInClass() {
        return documentsInClass;
    }

    public double getProir() {
        return proir;
    }

    public void setProir(double proir) {
        this.proir = proir;
    }

    private int documentsInClass = 0;
    protected ArrayList<Integer> wordsInClass;
    private int categoryNumber;
    private double proir = 0;

    public Category(int categoryNum) {
        super();
        categoryNumber = categoryNum;
        wordsInClass = new ArrayList<Integer>();

    }

    public void add(int numberToAdd) {
        wordsInClass.add(numberToAdd);
    }

    public void increment() {
        documentsInClass = documentsInClass + 1;
    }

    public int size() {
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
}
