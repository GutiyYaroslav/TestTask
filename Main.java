import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        User user1 = User.createUser("Alice", 32);
        User user2 = User.createUser("Bob", 19);
        User user3 = User.createUser("Charlie", 20);
        User user4 = User.createUser("John", 27);

        Product realProduct1 = ProductFactory.createRealProduct("Product A", 20.50, 10, 25);
        Product realProduct2 = ProductFactory.createRealProduct("Product B", 50, 6, 17);

        Product virtualProduct1 = ProductFactory.createVirtualProduct("Product C", 100, "xxx", LocalDate.of(2023, 5, 12));
        Product virtualProduct2 = ProductFactory.createVirtualProduct("Product D", 81.25, "yyy",  LocalDate.of(2024, 6, 20));

        List<Order> orders = new ArrayList<>() {{
            add(Order.createOrder(user1, List.of(realProduct1, virtualProduct1, virtualProduct2)));
            add(Order.createOrder(user2, List.of(realProduct1, realProduct2)));
            add(Order.createOrder(user3, List.of(realProduct1, virtualProduct2)));
            add(Order.createOrder(user4, List.of(virtualProduct1, virtualProduct2, realProduct1, realProduct2)));
        }};


        System.out.println("1. Create singleton class VirtualProductCodeManager \n");
        boolean isCodeUsed = VirtualProductCodeManager.getManager().isCodeUsed("yyy");
        System.out.println("Is code used: " + isCodeUsed + "\n");

        Product mostExpensive = getMostExpensiveProduct(orders);
        System.out.println("2. Most expensive product: " + mostExpensive + "\n");

        Product mostPopular = getMostPopularProduct(orders);
        System.out.println("3. Most popular product: " + mostPopular + "\n");

        double averageAge = calculateAverageAge(realProduct2, orders);
        System.out.println("4. Average age is: " + averageAge + "\n");

        Map<Product, List<User>> productUserMap = getProductUserMap(orders);
        System.out.println("5. Map with products as keys and list of users as value \n");
        productUserMap.forEach((key, value) -> System.out.println("key: " + key + " " + "value: " +  value + "\n"));

        List<Product> productsByPrice = sortProductsByPrice(List.of(realProduct1, realProduct2, virtualProduct1, virtualProduct2));
        System.out.println("6. a) List of products sorted by price: " + productsByPrice + "\n");
        List<Order> ordersByUserAgeDesc = sortOrdersByUserAgeDesc(orders);
        System.out.println("6. b) List of orders sorted by user agge in descending order: " + ordersByUserAgeDesc + "\n");

        Map<Order, Integer> result = calculateWeightOfEachOrder(orders);
        System.out.println("7. Calculate the total weight of each order \n");
        result.forEach((key, value) -> System.out.println("order: " + key + " " + "total weight: " +  value + "\n"));

    }

    public static Product getMostExpensiveProduct(List<Order> orders) {
        return orders.stream()
                .flatMap(order -> order.getProducts().stream())
                .max(Comparator.comparingDouble(Product::getPrice))
                .orElseThrow(() -> new IllegalStateException("No products found"));
    }

    public static Product getMostPopularProduct(List<Order> orders) {
        Map<Product, Integer> productCountMap = new HashMap<>();

        for (Order order : orders) {
            Set<Product> uniqueProducts = new HashSet<>(order.getProducts());

            for (Product product : uniqueProducts) {
                productCountMap.put(product, productCountMap.getOrDefault(product, 0) + 1);
            }
        }

        return productCountMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException("No products found"));
    }

    public static double calculateAverageAge(Product product, List<Order> orders) {
        List<Integer> ages = orders.stream()
                .filter(order -> order.getProducts().contains(product))
                .map(order -> order.getUser().getAge())
                .collect(Collectors.toList());

        if (ages.isEmpty()) {
            throw new IllegalStateException("No users found for the product");
        }

        double sum = 0;
        for (int age : ages) {
            sum += age;
        }

        return sum / ages.size();
    }

    public static Map<Product, List<User>> getProductUserMap(List<Order> orders) {
        Map<Product, List<User>> productUserMap = new HashMap<>();

        for (Order order : orders) {
            List<Product> products = order.getProducts();
            User user = order.getUser();

            for (Product product : products) {
                List<User> users = productUserMap.getOrDefault(product, new ArrayList<>());
                users.add(user);
                productUserMap.put(product, users);
            }
        }

        return productUserMap;
    }

    public static List<Product> sortProductsByPrice(List<Product> products) {
        return products.stream()
                .sorted(Comparator.comparingDouble(Product::getPrice))
                .collect(Collectors.toList());
    }

    public static List<Order> sortOrdersByUserAgeDesc(List<Order> orders) {
        return orders.stream()
                .sorted(Comparator.comparingInt((Order order) -> order.getUser().getAge()).reversed())
                .collect(Collectors.toList());
    }

    public static Map<Order, Integer> calculateWeightOfEachOrder(List<Order> orders) {
        Map<Order, Integer> result = new HashMap<>();

        for (Order order : orders) {
            int totalWeight = order.getProducts().stream()
                    .mapToInt(product -> {
                        if (product instanceof RealProduct) {
                            return ((RealProduct) product).getWeight();
                        } else {
                            return 0;
                        }
                    })
                    .sum();

            result.put(order, totalWeight);
        }

        return result;
    }

}

class User{
    private String name;
    private int age;

    private User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public static User createUser(String name, int age){
        return new User(name, age);
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
abstract class Product {
    private String name;
    private double price;

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}
class RealProduct extends Product {
    private int size;
    private int weight;

    public RealProduct(String name, double price, int size, int weight) {
        super(name, price);
        this.size = size;
        this.weight = weight;
    }

    public int getSize() {
        return size;
    }

    public int getWeight() {
        return weight;
    }
}

class VirtualProduct extends Product{
    private String code;
    private LocalDate expirationDate;

    public VirtualProduct(String name, double price, String code, LocalDate expirationDate) {
        super(name, price);
        this.code = code;
        this.expirationDate = expirationDate;
    }

    public String getCode() {
        return code;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }
}

class ProductFactory {
    private ProductFactory() {
    }

    public static Product createRealProduct(String name, double price, int size, int weight){
        return new RealProduct(name, price, size, weight);
    }

    public static Product createVirtualProduct(String name, double price, String code, LocalDate expirationDate){
        return new VirtualProduct(name, price, code, expirationDate);
    }
}

class Order {
    private User user;
    private List<Product> products;

    private Order(User user, List<Product> products) {
        this.user = user;
        this.products = products;
    }

    public static Order createOrder(User user, List<Product> products){
        return new Order(user, products);
    }

    public User getUser() {
        return user;
    }

    public List<Product> getProducts() {
        return products;
    }
}

class VirtualProductCodeManager {
    private static VirtualProductCodeManager manager;

    private VirtualProductCodeManager() {
    }

    public static synchronized VirtualProductCodeManager getManager() {
        if (manager == null) {
            manager = new VirtualProductCodeManager();
        }
        return manager;
    }

    public boolean isCodeUsed(String code){
        if(code.equals("xxx")){
            return true;
        }else if(code.equals("yyy")){
            return false;
        }else{
            throw new IllegalArgumentException("Invalid code");
        }
    }
}
