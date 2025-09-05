package com.app.Kaylia.controller;

import com.app.Kaylia.dto.OrderDisplayDto;
import com.app.Kaylia.model.ConfirmOrder;
import com.app.Kaylia.model.Products;
import com.app.Kaylia.model.User;
import com.app.Kaylia.repository.ConfirmOrderRepo;
import com.app.Kaylia.repository.ProductsRepo;
import com.app.Kaylia.repository.UserRepo;
import com.app.Kaylia.services.MailServices;
import com.app.Kaylia.services.ProductsServices;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Controller
public class PagesController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProductsRepo productsRepo;

    @Autowired
    private ProductsServices productsServices;

    @Autowired
    private MailServices mailServices;

    @Autowired
    private ConfirmOrderRepo confirmOrderRepo;

    @GetMapping("/")
    public String indexPage(Model model){
        model.addAttribute("activePage", "home");
        return "index";
    }



    @GetMapping("/products")
    public String productsPage(Model model){
        List<Products> products = productsServices.getAllProducts();

        // Create a list of truncated descriptions
        products.forEach(product -> {
            String description = product.getDescription();
            String[] words = description.split("\\s+");
            if (words.length > 8) {
                String truncated = String.join(" ", Arrays.copyOfRange(words, 0, 7)) + "...";
                product.setDescription(truncated); // temporarily replace description
            }
        });
        model.addAttribute("products", products);
        model.addAttribute("activePage", "products");
        return "products";
    }

    @GetMapping("/product-detail/{id}")
    public String productDetailPage(@PathVariable int id, Model model){
        System.out.println("Product id: "+id);
        Products theProduct = productsServices.getProduct(id);
        model.addAttribute("product", theProduct);
        return "product-detail";
    }

    @GetMapping("/new-arrivals")
    public String newArrivalsPage(Model model){
        List<Products> products = productsServices.getNewProducts();

        // Create a list of truncated descriptions
        products.forEach(product -> {
            String description = product.getDescription();
            String[] words = description.split("\\s+");
            if (words.length > 8) {
                String truncated = String.join(" ", Arrays.copyOfRange(words, 0, 7)) + "...";
                product.setDescription(truncated); // temporarily replace description
            }
        });
        model.addAttribute("products", products);
        model.addAttribute("activePage", "new-arrivals");
        return "new-arrivals";
    }

    @GetMapping("/about")
    public String aboutPage(Model model) {
        model.addAttribute("activePage", "about");
        return "about";
    }

    @GetMapping("/skin_analysis")
    public String skinAnalysisPage(Model model) {
        model.addAttribute("activePage", "skin_analysis");
        return "skin_analysis";
    }

    @GetMapping("/cart")
    public String cartPage() {
        return "cart";
    }

    @GetMapping("/checkout")
    public String checkoutPage(){
        return "checkout";
    }


    @GetMapping("/order-success/{orderId}")
    public String showOrderSuccess(@PathVariable int orderId, Model model, HttpSession session) {
        ConfirmOrder order = confirmOrderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        LocalDateTime localDateTime = order.getTimeOrdered();
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        model.addAttribute("orderDate", date);
        model.addAttribute("order", order);

        //send order confirmation mail
        mailServices.sendOrderConfirmationEmail(order, (String) session.getAttribute("email"));

        return "order-success";
    }

//ORder History
    @GetMapping("/order-history")
    @ResponseBody
    public ResponseEntity<?> getUserOrders(HttpSession session, Model model, Map map) {
        String email = (String) session.getAttribute("email");

        System.out.println("from order history" + email);

        User user = userRepo.findByEmail(email);

        List<ConfirmOrder> orders = confirmOrderRepo.findByUserId(user.getUserId());
        List<OrderDisplayDto> orderDisplayList = new ArrayList<>();

        for (ConfirmOrder order : orders) {
            OrderDisplayDto dto = new OrderDisplayDto();
            dto.setOrderId(order.getId());
            dto.setTimeOrdered(order.getTimeOrdered());
            dto.setTotalAmount(order.getTotalAmount());

            // fetch product info from IDs
            List<Products> products = productsRepo.findAllById(order.getProductIds());
            dto.setProducts(products);
            orderDisplayList.add(dto);
        }

        model.addAttribute("pastOrders", orderDisplayList);
        return ResponseEntity.ok(Map.of("message","Order loaded"));

    }

    //----for image load-----
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable int id) throws Exception {
        Products products = productsRepo.findById((long) id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        byte[] imageBytes = products.getImage();
        if (imageBytes == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG); // change to IMAGE_PNG if your images are png
        return new ResponseEntity<>(products.getImage(), headers, HttpStatus.OK);
    }


}

