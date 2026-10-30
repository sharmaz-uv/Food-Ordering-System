import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

class FoodItem implements Serializable {
    int id;
    String name;
    String category;
    double price;
    int stock;
    boolean rescueAvailable;

    FoodItem(int id, String name, String category, double price, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.rescueAvailable = false;
    }

    double getRescuePrice() {
        return price * 0.80;
    }
}

class Customer implements Serializable {
    String name;
    String phone;

    Customer(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }
}

class OrderItem implements Serializable {
    String name;
    int quantity;
    double price;
    boolean rescue;

    OrderItem(String name, int quantity, double price, boolean rescue) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.rescue = rescue;
    }

    double getAmount() {
        return price * quantity;
    }
}

class Order implements Serializable {
    int id;
    Customer customer;
    ArrayList<OrderItem> items;
    String status;
    String date;

    Order(int id, Customer customer, ArrayList<OrderItem> items) {
        this.id = id;
        this.customer = customer;
        this.items = new ArrayList<>(items);
        this.status = "PLACED";
        this.date = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    double getSubtotal() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getAmount();
        }
        return total;
    }

    double getTax() {
        return getSubtotal() * 0.05;
    }

    double getTotal() {
        return getSubtotal() + getTax();
    }
}

class RestaurantData implements Serializable {
    ArrayList<FoodItem> menu = new ArrayList<>();
    ArrayList<Order> orders = new ArrayList<>();
    int nextOrderId = 1001;
}

public class FoodFlowApp {
    static Scanner sc = new Scanner(System.in);
    static RestaurantData data = new RestaurantData();
    static final String FILE_NAME = "foodflow.dat";

    public static void main(String[] args) {
        loadData();

        if (data.menu.isEmpty()) {
            addSampleItems();
            saveData();
        }

        while (true) {
            System.out.println("\nFOODFLOW RESTAURANT SYSTEM");
            System.out.println("1. View Food Menu");
            System.out.println("2. Place an Order");
            System.out.println("3. Track an Order");
            System.out.println("4. Update Order Status");
            System.out.println("5. Manage Food Items");
            System.out.println("6. View Order History");
            System.out.println("7. Sales Report");
            System.out.println("8. Smart Meal Rescue");
            System.out.println("9. Exit");

            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    showMenu();
                    break;
                case 2:
                    placeOrder();
                    break;
                case 3:
                    trackOrder();
                    break;
                case 4:
                    updateOrderStatus();
                    break;
                case 5:
                    manageMenu();
                    break;
                case 6:
                    orderHistory();
                    break;
                case 7:
                    salesReport();
                    break;
                case 8:
                    mealRescue();
                    break;
                case 9:
                    saveData();
                    System.out.println("Thank you for using FoodFlow!");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    static int readInt(String message) {
        while (true) {
            System.out.print(message);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid whole number.");
            }
        }
    }

    static double readDouble(String message) {
        while (true) {
            System.out.print(message);
            try {
                double value = Double.parseDouble(sc.nextLine().trim());
                if (Double.isFinite(value) && value >= 0) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // Ask again for invalid input.
            }
            System.out.println("Enter a valid non-negative amount.");
        }
    }

    static String readText(String message) {
        while (true) {
            System.out.print(message);
            String value = sc.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    static void addSampleItems() {
        data.menu.add(new FoodItem(1, "Paneer Biryani", "Main Course", 180, 10));
        data.menu.add(new FoodItem(2, "Veg Burger", "Snacks", 100, 15));
        data.menu.add(new FoodItem(3, "Cold Coffee", "Drinks", 90, 12));
        data.menu.add(new FoodItem(4, "Masala Dosa", "Breakfast", 80, 10));
        data.menu.add(new FoodItem(5, "Paneer Roll", "Snacks", 120, 5));
    }

    static FoodItem findFood(int id) {
        for (FoodItem food : data.menu) {
            if (food.id == id) {
                return food;
            }
        }
        return null;
    }

    static Order findOrder(int id) {
        for (Order order : data.orders) {
            if (order.id == id) {
                return order;
            }
        }
        return null;
    }

    static void showMenu() {
        System.out.println("\nFOOD MENU");
        System.out.printf("%-5s %-22s %-15s %10s %8s%n",
                "ID", "Name", "Category", "Price", "Stock");

        for (FoodItem food : data.menu) {
            System.out.printf("%-5d %-22s %-15s %10.2f %8d%n",
                    food.id, food.name, food.category,
                    food.price, food.stock);
        }
    }

    static void placeOrder() {
        showMenu();

        Customer customer = new Customer(
                readText("\nCustomer name: "),
                readText("Phone number: "));

        ArrayList<OrderItem> cart = new ArrayList<>();

        while (true) {
            int id = readInt("Enter food ID (0 to finish): ");

            if (id == 0) {
                break;
            }

            FoodItem food = findFood(id);

            if (food == null) {
                System.out.println("Food item not found.");
                continue;
            }

            int quantity = readInt("Enter quantity: ");

            if (quantity <= 0 || quantity > food.stock) {
                System.out.println("Invalid quantity or insufficient stock.");
                continue;
            }

            boolean rescue = false;

            if (food.rescueAvailable) {
                System.out.println("Smart Meal Rescue price: Rs."
                        + String.format("%.2f", food.getRescuePrice()));
                System.out.print("Use this discounted price? (y/n): ");
                rescue = sc.nextLine().trim().equalsIgnoreCase("y");
            }

            double price = rescue ? food.getRescuePrice() : food.price;

            cart.add(new OrderItem(food.name, quantity, price, rescue));
            System.out.println("Item added to cart.");
        }

        if (cart.isEmpty()) {
            System.out.println("No items ordered.");
            return;
        }

        System.out.println("\nORDER SUMMARY");
        for (OrderItem item : cart) {
            System.out.printf("%-22s x %-3d Rs.%.2f%n",
                    item.name, item.quantity, item.getAmount());
        }

        double subtotal = 0;
        for (OrderItem item : cart) {
            subtotal += item.getAmount();
        }

        System.out.printf("Subtotal: Rs.%.2f%n", subtotal);
        System.out.printf("Tax (5%%): Rs.%.2f%n", subtotal * 0.05);
        System.out.printf("Total: Rs.%.2f%n", subtotal * 1.05);

        System.out.print("Confirm order? (y/n): ");

        if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("Order cancelled.");
            return;
        }

        for (OrderItem item : cart) {
            FoodItem food = null;

            for (FoodItem f : data.menu) {
                if (f.name.equals(item.name)) {
                    food = f;
                    break;
                }
            }

            if (food == null || food.stock < item.quantity) {
                System.out.println("Stock changed. Please place the order again.");
                return;
            }
        }

        for (OrderItem item : cart) {
            for (FoodItem food : data.menu) {
                if (food.name.equals(item.name)) {
                    food.stock -= item.quantity;
                    break;
                }
            }
        }

        Order order = new Order(data.nextOrderId++, customer, cart);
        data.orders.add(order);
        saveData();

        System.out.println("\nOrder placed successfully!");
        printBill(order);
    }

    static void printBill(Order order) {
        System.out.println("\nFOODFLOW TAX INVOICE");
        System.out.println("Order ID: " + order.id);
        System.out.println("Customer: " + order.customer.name);
        System.out.println("Phone: " + order.customer.phone);
        System.out.println("Date: " + order.date);
        System.out.println("---------------------------------------------");

        for (OrderItem item : order.items) {
            System.out.printf("%-22s x %-3d Rs.%.2f%s%n",
                    item.name, item.quantity, item.getAmount(),
                    item.rescue ? " (Rescue)" : "");
        }

        System.out.println("---------------------------------------------");
        System.out.printf("Subtotal: Rs.%.2f%n", order.getSubtotal());
        System.out.printf("Tax (5%%): Rs.%.2f%n", order.getTax());
        System.out.printf("TOTAL: Rs.%.2f%n", order.getTotal());
        System.out.println("Status: " + order.status);
    }

    static void trackOrder() {
        
    }
        

    static void updateOrderStatus() {

    }
        

    static void manageMenu() {
        System.out.println("\nMENU MANAGEMENT");
        System.out.println("1. Add food item");
        System.out.println("2. Change price");
        System.out.println("3. Update stock");
        System.out.println("4. Toggle Smart Meal Rescue");

        int choice = readInt("Enter choice: ");

        if (choice == 1) {
            int id = 1;
            for (FoodItem food : data.menu) {
                id = Math.max(id, food.id + 1);
            }

            String name = readText("Food name: ");
            String category = readText("Category: ");
            double price = readDouble("Price: ");
            int stock = readInt("Available portions: ");

            if (price <= 0 || stock < 0) {
                System.out.println("Price must be positive and stock non-negative.");
                return;
            }

            data.menu.add(new FoodItem(id, name, category, price, stock));
            saveData();
            System.out.println("Food item added with ID " + id + ".");

        } else if (choice >= 2 && choice <= 4) {
            showMenu();
            int id = readInt("Enter food ID: ");
            FoodItem food = findFood(id);

            if (food == null) {
                System.out.println("Food item not found.");
                return;
            }

            if (choice == 2) {
                double price = readDouble("New price: ");
                if (price <= 0) {
                    System.out.println("Price must be positive.");
                    return;
                }
                food.price = price;
                System.out.println("Price updated.");
            } else if (choice == 3) {
                int stock = readInt("New stock quantity: ");
                if (stock < 0) {
                    System.out.println("Stock cannot be negative.");
                    return;
                }
                food.stock = stock;
                System.out.println("Stock updated.");
            } else {
                food.rescueAvailable = !food.rescueAvailable;
                System.out.println("Smart Meal Rescue is now "
                        + (food.rescueAvailable ? "ON." : "OFF."));
            }

            saveData();
        } else {
            System.out.println("Invalid choice.");
        }
    }

    static void orderHistory() {

    }
        

    static void salesReport() {
        double sales = 0;
        int completed = 0;
        int rescuePortions = 0;
        HashMap<String, Integer> sold = new HashMap<>();

        for (Order order : data.orders) {
            sales += order.getTotal();

            if (order.status.equals("COMPLETED")) {
                completed++;
            }

            for (OrderItem item : order.items) {
                sold.put(item.name,
                        sold.getOrDefault(item.name, 0) + item.quantity);

                if (item.rescue) {
                    rescuePortions += item.quantity;
                }
            }
        }

        System.out.println("\nSALES REPORT");
        System.out.println("Total orders: " + data.orders.size());
        System.out.println("Completed orders: " + completed);
        System.out.printf("Total order value: Rs.%.2f%n", sales);
        System.out.println("Smart Meal Rescue portions sold: " + rescuePortions);

        String bestSeller = "No sales yet";
        int highest = 0;

        for (Map.Entry<String, Integer> entry : sold.entrySet()) {
            if (entry.getValue() > highest) {
                highest = entry.getValue();
                bestSeller = entry.getKey();
            }
        }

        System.out.println("Best-selling dish: " + bestSeller);
        System.out.println("Portions sold by dish:");

        for (Map.Entry<String, Integer> entry : sold.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    static void mealRescue() {
        System.out.println("\nSMART MEAL RESCUE");
        System.out.println("Discounted dishes with available stock:");

        boolean found = false;

        for (FoodItem food : data.menu) {
            if (food.rescueAvailable && food.stock > 0) {
                System.out.printf("%d. %s | Regular: Rs.%.2f"
                                + " | Rescue: Rs.%.2f | Portions: %d%n",
                        food.id, food.name, food.price,
                        food.getRescuePrice(), food.stock);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No rescue deals are available right now.");
        }

        System.out.println("To enable a deal, use Menu Management.");
    }

    static void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(FILE_NAME))) {
            out.writeObject(data);
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    static void loadData() {
        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(file))) {
            data = (RestaurantData) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Could not load saved data. Starting fresh.");
            data = new RestaurantData();
        }
    }
}