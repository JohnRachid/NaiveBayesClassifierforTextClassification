This is a Naive Bayes Classifier for Text Classification with the goal of correctly predicting the topics of news
articles based on past training examples. Running the NaiveBayesClassifier will train the model with both bayesian
estimation and maximum likelihood estimation values. It will also output the total performance, class performance and
confusion matrix for the training data bayesian and maximum likelihood estimation as well as the training data. I do wish
I had more time for this project as some of my code could be cleaner and more efficient.

This was developed using Intellij with java jdk 1.8.0_121
The project organization is as follows:
MainProject folder
    data
        map.csv
        test_data.csv
        test_label.csv
        train_data.csv
        train_label.csv
        vocabulary.txt
    src
        Category
        Document
        NaiveBayesClassifier
HOW TO RUN
------------------------------------------
Ensure the file paths for the training/testing/vocab data are properly set or organize your project like mine is above.
Run naiveBayesClassifier