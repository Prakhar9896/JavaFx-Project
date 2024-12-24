package com.app.demo2;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class is extends Application {
    private Inventory inventory;
    private TextArea statisticsArea;  
    private TextArea managerNotificationArea;  

    @Override
    public void start(Stage primaryStage) {
        inventory = new Inventory();

        VBox loginLayout = new VBox(10);
        loginLayout.setPadding(new Insets(10));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        Button managerLoginButton = new Button("Login as Manager");
        Button customerLoginButton = new Button("Login as Customer");

        managerLoginButton.setOnAction(e -> {
            if (usernameField.getText().equals("manager")) {
                createManagerWindow();
            } else {
                showAlert("Invalid username for Manager.");
            }
        });

        customerLoginButton.setOnAction(e -> {
            if (usernameField.getText().equals("customer")) {
                createCustomerWindow();
            } else {
                showAlert("Invalid username for Customer.");
            }
        });

        loginLayout.getChildren().addAll(new Label("Login"), usernameField, managerLoginButton, customerLoginButton);
        Scene loginScene = new Scene(loginLayout, 300, 200);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Inventory System Login");
        primaryStage.show();
    }

    private void createManagerWindow() {
        Stage managerStage = new Stage();
        managerNotificationArea = new TextArea();
        managerNotificationArea.setEditable(false);
        managerNotificationArea.setPrefHeight(150);

        VBox managerView = new VBox(10);
        managerView.setPadding(new Insets(10));

        TextField idField = new TextField();
        idField.setPromptText("Product ID");

        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        ComboBox<String> shipmentModeComboBox = new ComboBox<>();
        shipmentModeComboBox.getItems().addAll("land", "sea");
        shipmentModeComboBox.setPromptText("Shipment Mode");

        Button addProductButton = new Button("Add Product");
        addProductButton.setOnAction((ActionEvent e) -> {
            try {
                String id = idField.getText();
                String name = nameField.getText();
                int quantity = Integer.parseInt(quantityField.getText());
                double price = Double.parseDouble(priceField.getText());
                String shipmentMode = shipmentModeComboBox.getValue();

                if (shipmentMode == null) {
                    managerNotificationArea.appendText("Please select a shipment mode.\n");
                    return;
                }

                Product product = new Product(id, name, quantity, shipmentMode, price);
                inventory.addProduct(product);
                managerNotificationArea.appendText("Product added: " + name + "\n");

                if (quantity < 5) {
                    managerNotificationArea.appendText("Low stock alert for: " + name + ". Quantity left: " + quantity + "\n");
                }

                idField.clear();
                nameField.clear();
                quantityField.clear();
                priceField.clear();
                shipmentModeComboBox.setValue(null);

                updateStatistics(); 
            } catch (NumberFormatException ex) {
                managerNotificationArea.appendText("Invalid input. Please enter valid numbers for quantity and price.\n");
            }
        });

       
        Label statisticsLabel = new Label("Statistics:");
        statisticsArea = new TextArea();  
        statisticsArea.setEditable(false);
        statisticsArea.setPrefHeight(100);

        
        Button refreshStatsButton = new Button("Refresh Statistics");
        refreshStatsButton.setOnAction(e -> updateStatistics());

        managerView.getChildren().addAll(new Label("Manager - Add New Product"), idField, nameField, quantityField, priceField, shipmentModeComboBox, addProductButton, new Label("Manager Notifications:"), managerNotificationArea, statisticsLabel, statisticsArea, refreshStatsButton);
        Scene managerScene = new Scene(managerView, 400, 500);
        managerStage.setScene(managerScene);
        managerStage.setTitle("Manager Window");
        managerStage.show();
    }

    private void updateStatistics() {
        int cargoCount = 0;
        int goodsCount = 0;

        for (Product product : inventory.getProducts()) {
            if ("sea".equalsIgnoreCase(product.getShipmentMode())) {
                cargoCount += product.getQuantity();
            } else if ("land".equalsIgnoreCase(product.getShipmentMode())) {
                goodsCount += product.getQuantity();
            }
        }

        statisticsArea.setText("Cargo Products Left: " + cargoCount + "\nGoods Products Left: " + goodsCount);
    }

    private void createCustomerWindow() {
        Stage customerStage = new Stage();
        TextArea customerNotificationArea = new TextArea();
        customerNotificationArea.setEditable(false);
        customerNotificationArea.setPrefHeight(150);

        VBox customerView = new VBox(10);
        customerView.setPadding(new Insets(10));

        TableView<Product> productTable = new TableView<>(inventory.getProducts());
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Product, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));

        TableColumn<Product, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));

        TableColumn<Product, Integer> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());

        TableColumn<Product, String> shipmentModeColumn = new TableColumn<>("Shipment Mode");
        shipmentModeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getShipmentMode()));

        TableColumn<Product, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));

        TableColumn<Product, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());

        productTable.getColumns().addAll(idColumn, nameColumn, quantityColumn, shipmentModeColumn, categoryColumn, priceColumn);

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        Button orderButton = new Button("Place Order");
        orderButton.setOnAction((ActionEvent e) -> {
            Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduct != null && !quantityField.getText().isEmpty()) {
                try {
                    int orderQuantity = Integer.parseInt(quantityField.getText());

                    if (orderQuantity <= selectedProduct.getQuantity()) {
                        double totalPrice = orderQuantity * selectedProduct.getPrice();

                        Order order = new Order(selectedProduct.getId(), orderQuantity, selectedProduct.getShipmentMode(), totalPrice);
                        inventory.recordOrder(order);

                    
                        selectedProduct.setQuantity(selectedProduct.getQuantity() - orderQuantity);
                        productTable.refresh();

                        customerNotificationArea.appendText("Order placed for " + selectedProduct.getName() + ". Quantity: " + orderQuantity + ". Total Price: $" + totalPrice + "\n");

                        if (selectedProduct.getQuantity() < 5) {
                            managerNotificationArea.appendText("Low stock alert: " + selectedProduct.getName() + " now has only " + selectedProduct.getQuantity() + " left.\n");
                        }
                    } else {
                        customerNotificationArea.appendText("Insufficient stock for " + selectedProduct.getName() + ".\n");
                    }
                } catch (NumberFormatException ex) {
                    customerNotificationArea.appendText("Invalid quantity. Please enter a valid number.\n");
                }
            } else {
                customerNotificationArea.appendText("Select a product and enter a quantity.\n");
            }
        });

        customerView.getChildren().addAll(new Label("Customer - Available Products"), productTable, new Label("Order Quantity"), quantityField, orderButton, new Label("Customer Notifications:"), customerNotificationArea);
        Scene customerScene = new Scene(customerView, 500, 400);
        customerStage.setScene(customerScene);
        customerStage.setTitle("Customer Window");
        customerStage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


class Inventory {
    private ObservableList<Product> products;
    private ObservableList<Order> orders;

    public Inventory() {
        products = FXCollections.observableArrayList();
        orders = FXCollections.observableArrayList();
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public ObservableList<Product> getProducts() {
        return products;
    }

    public void recordOrder(Order order) {
        orders.add(order);
    }
}

class Product {
    private String id;
    private String name;
    private int quantity;
    private String shipmentMode;
    private double price;

    public Product(String id, String name, int quantity, String shipmentMode, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.shipmentMode = shipmentMode;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getShipmentMode() {
        return shipmentMode;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
       
        return "sea".equalsIgnoreCase(shipmentMode) ? "Cargo" : "Goods";
    }
}

class Order {
    private String productId;
    private int quantity;
    private String shipmentMode;
    private double totalPrice;

    public Order(String productId, int quantity, String shipmentMode, double totalPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.shipmentMode = shipmentMode;
        this.totalPrice = totalPrice;
    }
}
