package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

    interface ExpenseManager {
        void process(double amount, String type, String date);
        void reset();
        void update();
    }

    class User implements Runnable, ExpenseManager {
        double savings;
        double balance;
        double debt = 0;

        final Label balanceLabel = new Label();
        final Label savingsLabel = new Label();
        final Label debtLabel = new Label();
        final TextArea expenseList = new TextArea();
        final Label warningLabel = new Label();
        final String name;

        ToggleGroup dateToggleGroup;

        public User(String name) {
            this.name = name;
        }

        public void run() {
            Platform.runLater(() -> {
                Stage userStage = new Stage();
                userStage.setTitle(name + "'s Expense Tracker");

                
                VBox layout = new VBox(20);
                layout.setPadding(new Insets(20));
                layout.setStyle("-fx-background-color:#FFF1DC;");

                // Pocket Money Section
                Label pocketMoneyLabel = new Label("Enter your pocket money:");
                TextField pocketMoneyField = new TextField();
                pocketMoneyField.setPromptText("Enter your pocket money");
                Button submitButton = new Button("SUBMIT");
                submitButton.setStyle("-fx-background-color: orange; -fx-text-fill: white;");

                
                TextField expenseAmountField = new TextField();
                expenseAmountField.setPromptText("Enter Expense Amount");
                expenseAmountField.setDisable(true);
                expenseAmountField.setStyle("-fx-background-color: 	#808080;");

                TextField expenseTypeField = new TextField();
                expenseTypeField.setPromptText("Enter Expense Type");
                expenseTypeField.setDisable(true);
                expenseTypeField.setStyle("-fx-background-color:	#808080;");

                TextField expenseDateField = new TextField();
                expenseDateField.setPromptText("Enter Expense Date (YYYY-MM-DD)");
                expenseDateField.setDisable(true);
                expenseDateField.setStyle("-fx-background-color: 	#808080;");

                RadioButton customDate = new RadioButton("Custom Date");
                RadioButton presentDate = new RadioButton("Present Date");
                dateToggleGroup = new ToggleGroup();
                customDate.setToggleGroup(dateToggleGroup);
                presentDate.setToggleGroup(dateToggleGroup);
                presentDate.setSelected(true);

                Button addButton = new Button("ADD EXPENSE");
                addButton.setDisable(true);
                addButton.setStyle("-fx-background-color: green; -fx-text-fill: white;"); 

                Button resetButton = new Button("RESET");
                resetButton.setDisable(true);
                resetButton.setStyle("-fx-background-color: red; -fx-text-fill: white;"); 

                Label savingsPromptLabel = new Label("A fixed saving of 20% will be kept aside as a good financial practice");
                expenseList.setEditable(false);
                expenseList.setWrapText(true);
                expenseList.setPrefHeight(200);

                submitButton.setOnAction(e -> {
                    try {
                        double totalIncome = Double.parseDouble(pocketMoneyField.getText());
                        savings = totalIncome * 0.20;
                        balance = totalIncome - savings;

                        savingsPromptLabel.setText("A fixed saving of 20% (" + String.format("%.2f", savings) +
                                ") has been kept aside as a good financial practice");
                        expenseAmountField.setDisable(false);
                        expenseTypeField.setDisable(false);
                        expenseDateField.setDisable(false);
                        addButton.setDisable(false);
                        resetButton.setDisable(false);
                        submitButton.setDisable(true);

                        update();
                    } catch (NumberFormatException ei) {
                        warningLabel.setText("Please enter a valid number for pocket money.");
                    }
                });

                addButton.setOnAction(e -> {
                    try {
                        String expenseType = expenseTypeField.getText();
                        double expenseAmount = Double.parseDouble(expenseAmountField.getText());
                        String expenseDate;

                        if (presentDate.isSelected()) {
                            expenseDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                        } else {
                            expenseDate = expenseDateField.getText();
                            if (!isValidDate(expenseDate)) {
                                warningLabel.setText("Please enter a valid date (YYYY-MM-DD).");
                                return;
                            }
                        }

                        if (expenseAmount <= 0) {
                            warningLabel.setText("Expense amount must be greater than zero.");
                            return;
                        }

                        process(expenseAmount, expenseType, expenseDate);

                        expenseAmountField.clear();
                        expenseTypeField.clear();
                        expenseDateField.clear();
                        update();
                    } catch (NumberFormatException ei) {
                        warningLabel.setText("Please enter a valid number for expense amount.");
                    }
                });

                resetButton.setOnAction(e -> {
                    pocketMoneyField.clear();
                    expenseAmountField.clear();
                    expenseTypeField.clear();
                    expenseDateField.clear();
                    balance = 0;
                    savings = 0;
                    debt = 0;
                    expenseList.clear();
                    warningLabel.setText("");
                    update();
                });

                dateToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == customDate) {
                        expenseDateField.setDisable(false);
                    } else {
                        expenseDateField.setDisable(true);
                        expenseDateField.clear();
                    }
                });

                
                GridPane pocketMoneyPane = new GridPane();
                pocketMoneyPane.setHgap(10);
                pocketMoneyPane.setVgap(10);
                pocketMoneyPane.add(pocketMoneyLabel, 0, 0);
                pocketMoneyPane.add(pocketMoneyField, 1, 0);
                pocketMoneyPane.add(submitButton, 3, 0);

                GridPane expenseEntryPane = new GridPane();
                expenseEntryPane.setHgap(10);
                expenseEntryPane.setVgap(10);
                expenseEntryPane.add(new Label("Expense Amount:"), 0, 0);
                expenseEntryPane.add(expenseAmountField, 1, 0);
                expenseEntryPane.add(new Label("Expense Type:"), 0, 1);
                expenseEntryPane.add(expenseTypeField, 1, 1);
                expenseEntryPane.add(new Label("Expense Date:"), 0, 2);
                expenseEntryPane.add(customDate, 1, 2);
                expenseEntryPane.add(presentDate, 2, 2);
                expenseEntryPane.add(expenseDateField, 1, 3, 2, 1);
                expenseEntryPane.add(addButton, 2, 4);
                expenseEntryPane.add(resetButton, 4, 4);

                VBox expensePane = new VBox(10);
                expensePane.getChildren().addAll(
                        new Label("List of expenses->"),
                        balanceLabel, savingsLabel, debtLabel
                );

                layout.getChildren().addAll(
                        pocketMoneyPane, savingsPromptLabel, expenseEntryPane,
                        expensePane, warningLabel, expenseList
                );

                Scene scene = new Scene(layout, 650, 700);
                userStage.setScene(scene);
                userStage.show();
            });
        }

        boolean isValidDate(String date) {
            try {
                LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        }

        
        public void process(double amount, String type, String date) {
            if (type == null || type.trim().isEmpty()) {
                type = "Miscellaneous";
            }

            if (balance >= amount) {
                balance -= amount;
                logExpense(String.format("%s|%s|%.2f\n", date, type, amount));
            } else if (savings > 0 && (balance + savings) >= amount) {
                double remainingFromBalance = amount - balance;
                balance = 0;
                savings -= remainingFromBalance;
                logExpense(String.format("Savings|%s|%s|%.2f\n", date, type, remainingFromBalance));

                if (savings < 0) {
                    double usedSavings = remainingFromBalance + savings;
                    logExpense("Savings exhausted. Used up all savings.\n");
                    savings = 0;
                    debt += usedSavings;
                    logExpense(String.format("Debt|%s|%s|%.2f\n", date, type, usedSavings));
                }
            } else {
                double needed = amount - (balance + savings);
                balance = 0;
                savings = 0;
                debt += needed;
                logExpense(String.format("Debt|%s|%s|%.2f\n", date, type, needed));
            }
        }

        void logExpense(String message) {
            Platform.runLater(() -> expenseList.appendText(message));
        }

       
        public void update() {
            Platform.runLater(() -> {
                if (balance >= 500) {
                    balanceLabel.setTextFill(Color.GREEN);
                } else if (balance > 0 && balance < 500) {
                    balanceLabel.setTextFill(Color.MAGENTA);
                } else {
                    balanceLabel.setTextFill(Color.RED);
                }
                balanceLabel.setText("Balance: " + String.format("%.2f", balance));
                savingsLabel.setText("Savings: " + String.format("%.2f", savings));
                debtLabel.setText("Debt: " + String.format("%.2f", debt));
            });
        }
    }

   
    public void start(Stage primaryStage) {
        Thread user1 = new Thread(new User("Abhigyan"));
        Thread user2 = new Thread(new User("Yatharth"));
        Thread user3 = new Thread(new User("Krishna"));

        user1.start();
        user2.start();
        user3.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
