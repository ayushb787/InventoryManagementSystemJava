package org.demo;

import java.util.*;

public class InventoryManager
{
    private static final int REORDER_LEVEL = 10;

    private final Map<String, Product> productCatalog;
    private final Map<String, PriorityQueue<Product>> categoryCatalog;

    public InventoryManager() {
        productCatalog = new HashMap<>();
        categoryCatalog = new TreeMap<>();
    }

    public void addOrUpdateProduct(String productId, String productName, String categoryName, int stock) {
        if (productId == null || productId.isEmpty()) {
            System.out.println("Invalid product ID.");
            return;
        }
        if (productName == null || productName.isEmpty()) {
            System.out.println("Invalid product name.");
            return;
        }
        if (categoryName == null || categoryName.isEmpty()) {
            System.out.println("Invalid category name.");
            return;
        }
        if (stock < 0) {
            System.out.println("Stock cannot be negative.");
            return;
        }

        Product product = new Product(productId, productName, categoryName, stock);

        if (productCatalog.containsKey(productId)) {
            Product existingProduct = productCatalog.get(productId);
            removeFromCategory(existingProduct);

            existingProduct.setProductName(productName);
            existingProduct.setCategoryName(categoryName);
            existingProduct.setStock(stock);
            addToCategory(existingProduct);

            System.out.println("Updated: " + existingProduct);

            if (stock < REORDER_LEVEL) {
                System.out.println("Alert: Product \"" + productName + "\" is running low. Current stock: " + stock + ". Reorder soon.");
            }
        } else {
            productCatalog.put(productId, product);
            addToCategory(product);
            System.out.println("Added: " + product);

            if (stock < REORDER_LEVEL) {
                System.out.println("Alert: Product \"" + productName + "\" is low in stock. Current stock: " + stock + ". Consider restocking.");
            }
        }
    }

    public void removeProduct(String productId) {
        if (productId == null || productId.isEmpty()) {
            System.out.println("Invalid product ID.");
            return;
        }

        if (productCatalog.containsKey(productId)) {
            Product product = productCatalog.remove(productId);
            removeFromCategory(product);
            System.out.println("Removed: " + product);
        } else {
            System.out.println("Product ID '" + productId + "' not found.");
        }
    }

    public List<Product> getProductsByCategory(String categoryName) {
        if (categoryName == null || categoryName.isEmpty()) {
            System.out.println("Invalid category name.");
            return Collections.emptyList();
        }

        PriorityQueue<Product> products = categoryCatalog.getOrDefault(categoryName, new PriorityQueue<>());
        if (products.isEmpty()) {
            System.out.println("No products found in the category: '" + categoryName + "'.");
            return Collections.emptyList();
        }

        System.out.println("Products in category '" + categoryName + "':");
        return new ArrayList<>(products);
    }

    public List<Product> getTopKProducts(int topK) {
        if (topK <= 0) {
            System.out.println("Invalid number. 'k' must be positive.");
            return Collections.emptyList();
        }

        PriorityQueue<Product> maxHeap = new PriorityQueue<>((a, b) -> b.getStock() - a.getStock());
        maxHeap.addAll(productCatalog.values());

        List<Product> topProducts = new ArrayList<>();
        while (topK-- > 0 && !maxHeap.isEmpty()) {
            topProducts.add(maxHeap.poll());
        }

        if (topProducts.isEmpty()) {
            System.out.println("No products to display.");
            return Collections.emptyList();
        }

        System.out.println("Top " + topK + " products by stock quantity:");
        return topProducts;
    }

    public void combineInventories(InventoryManager otherInventory) {
        if (otherInventory == null) {
            System.out.println("Cannot combine with a null inventory.");
            return;
        }

        System.out.println("Combining with another inventory...");
        for (Product otherProduct : otherInventory.productCatalog.values()) {
            if (productCatalog.containsKey(otherProduct.getProductId())) {
                Product existingProduct = productCatalog.get(otherProduct.getProductId());
                if (otherProduct.getStock() > existingProduct.getStock()) {
                    removeFromCategory(existingProduct);
                    existingProduct.setStock(otherProduct.getStock());
                    addToCategory(existingProduct);
                    System.out.println("Updated product with higher stock: " + existingProduct);
                }
            } else {
                addOrUpdateProduct(otherProduct.getProductId(), otherProduct.getProductName(), otherProduct.getCategoryName(), otherProduct.getStock());
                System.out.println("Added new product: " + otherProduct);
            }
        }
    }

    private void addToCategory(Product product) {
        categoryCatalog.putIfAbsent(product.getCategoryName(), new PriorityQueue<>((a, b) -> b.getStock() - a.getStock()));
        categoryCatalog.get(product.getCategoryName()).add(product);
    }

    private void removeFromCategory(Product product) {
        PriorityQueue<Product> products = categoryCatalog.get(product.getCategoryName());
        if (products != null) {
            products.remove(product);
            if (products.isEmpty()) {
                categoryCatalog.remove(product.getCategoryName());
            }
        }
    }

    static class Product {
        private String productId;
        private String productName;
        private String categoryName;
        private int stock;

        public Product(String productId, String productName, String categoryName, int stock) {
            this.productId = productId;
            this.productName = productName;
            this.categoryName = categoryName;
            this.stock = stock;
        }

        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public int getStock() { return stock; }
        public void setStock(int stock) { this.stock = stock; }

        @Override
        public String toString() {
            return "Product{" +
                    "productId='" + productId + '\'' +
                    ", productName='" + productName + '\'' +
                    ", categoryName='" + categoryName + '\'' +
                    ", stock=" + stock +
                    '}';
        }
    }

    public static void main(String[] args) {
        InventoryManager inventory = new InventoryManager();

        System.out.println("Adding Products:");
        inventory.addOrUpdateProduct("1", "Laptop", "Electronics", 50);
        inventory.addOrUpdateProduct("2", "Chair", "Furniture", 20);
        inventory.addOrUpdateProduct("3", "Apple", "Groceries", 5);

        System.out.println("\nUpdating Products:");
        inventory.addOrUpdateProduct("1", "Laptop", "Electronics", 10);

        System.out.println("\nRemoving Product:");
        inventory.removeProduct("2");

        System.out.println("\nProducts in Category:");
        System.out.println("Electronics: " + inventory.getProductsByCategory("Electronics"));

        System.out.println("\nTop 2 Products:");
        System.out.println(inventory.getTopKProducts(2));

        System.out.println("\nMerging Inventories:");
        InventoryManager anotherInventory = new InventoryManager();
        anotherInventory.addOrUpdateProduct("4", "Table", "Furniture", 30);
        anotherInventory.addOrUpdateProduct("1", "Laptop", "Electronics", 60);
        inventory.combineInventories(anotherInventory);

        System.out.println("\nAfter Merging:");
        System.out.println("Electronics: " + inventory.getProductsByCategory("Electronics"));
        System.out.println("Clothing: " + inventory.getProductsByCategory("Clothing"));
        System.out.println("Groceries: " + inventory.getProductsByCategory("Groceries"));
        System.out.println("Furniture: " + inventory.getProductsByCategory("Furniture"));
    }
}
