package com.app.Kaylia.controller;

import com.app.Kaylia.dto.RegisterUserDto;
import com.app.Kaylia.model.ConfirmOrder;
import com.app.Kaylia.model.Products;
import com.app.Kaylia.model.User;
import com.app.Kaylia.repository.ConfirmOrderRepo;
import com.app.Kaylia.repository.ProductsRepo;
import com.app.Kaylia.repository.UserRepo;
import com.app.Kaylia.services.MailServices;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminPagesController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MailServices mailServices;

    @Autowired
    private ProductsRepo productsRepo;

    @Autowired
    private ConfirmOrderRepo confirmOrderRepo;

    @GetMapping("/dashboard")
    @Transactional
    public String adminDashboardPage(Model model){

        List<User> userList = userRepo.findAll();
        List<Products> productsList = productsRepo.findAll();
        List<ConfirmOrder> orderList = confirmOrderRepo.findAll();

        int totalUsers = 0;
        int totalOrders = 0;
        int allProducts = 0;
        double revenue = 0;

        for(User user: userList){
            totalUsers += 1;
        }

        for(ConfirmOrder confirmedOrders: orderList){
            totalOrders += 1;
            revenue += confirmedOrders.getTotalAmount();
        }

        for (Products product : productsList){
            allProducts += 1;
        }

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("allProducts", allProducts);
        model.addAttribute("revenue", revenue);

        return "admin-dashboard";
    }

    @GetMapping("/add-product")
    @Transactional
    public String addProductPage(HttpSession session, Model model){

        User user = userRepo.findByEmail((String) session.getAttribute("email"));


        List<Products> productsList = productsRepo.findByCreatedBy(user);


        model.addAttribute("products", productsList);
        return "add-product";
    }

    @GetMapping("/manage-product")
    @Transactional
    public String adminManageProduct(HttpSession session, Model model){
        User activeUser = userRepo.findByEmail((String) session.getAttribute("email"));

        List<Products> productsList = productsRepo.findByCreatedBy(activeUser);

        model.addAttribute("products", productsList);

        return "admin-manage-product";
    }

    @GetMapping("/remove-product/{id}")

    @Transactional

    public String adminRemoveProduct(@PathVariable Long id){

        productsRepo.removeProductsById(id);

        return "redirect:/admin/manage-product";
    }

    @GetMapping("/create-user")
    public String createUserPage(Model model){

        List<User> userList = userRepo.findAll();

        int totalUser = 0;
        int superAdmins = 0;
        int admins = 0;
        int vendors = 0;

        //to get total users and role users
        for(User user: userList){

            switch (user.getRole()){

                case "ROLE_SUPER_ADMIN":
                    superAdmins += 1;
                    break;
                case "ROLE_ADMIN":
                    admins += 1;
                    break;
                case "ROLE_VENDOR":
                    vendors += 1;
                    break;
                case "ROLE_USER":
                    totalUser += 1;
                    break;
                default:
                    break;
            }
        }

        model.addAttribute("totalUsers", totalUser);
        model.addAttribute("superAdmins", superAdmins);
        model.addAttribute("admins", admins);
        model.addAttribute("vendors", vendors);

        return "create-user";
    }

    @GetMapping("/manage-user")

    public String manageUserPage(HttpSession session, Model model){

        List<User> userList = userRepo.findAll();

        int totalUser = 0;
        int superAdmins = 0;
        int admins = 0;
        int vendors = 0;

        //to get total users and role users
        for(User user: userList){
            switch (user.getRole()){
                case "ROLE_SUPER_ADMIN":
                    superAdmins += 1;
                    break;
                case "ROLE_ADMIN":
                    admins += 1;
                    break;
                case "ROLE_VENDOR":
                    vendors += 1;
                    break;
                case "ROLE_USER":
                    totalUser += 1;
                    break;
                default:
                    break;
            }
        }

        model.addAttribute("totalUsers", totalUser);
        model.addAttribute("superAdmins", superAdmins);
        model.addAttribute("admins", admins);
        model.addAttribute("vendors", vendors);

        model.addAttribute("users", userList);

        return "admin-manage-user";
    }

    @GetMapping("/remove-user/{id}")
    @Transactional
    public String removeUser(@PathVariable int id){

        userRepo.removeUserByUserId(id);

        return "redirect:/admin/manage-user";
    }

    @GetMapping("/role-check")
    @ResponseBody
    public ResponseEntity<?> roleCheck(HttpSession session){
        String email = (String) session.getAttribute("email");
        System.out.println("from role check " + email);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not logged in"));
        }

        User theAdmin = userRepo.findByEmail(email);
        if (theAdmin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }
        System.out.println(theAdmin.getRole());
        return ResponseEntity.ok(Map.of("role", theAdmin.getRole()));
    }

    @PostMapping("/create-user")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserDto newUser){

        Optional<User> isUser = userRepo.findUserByEmail(newUser.getEmail());

        if(isUser.isPresent()){
            //checks existing email
            return ResponseEntity.badRequest().body(Map.of("error", "Email Already Registered"));
        }else{
            //create new if no existing email
            User registerUser = new User();

            registerUser.setFName(newUser.getFName());
            registerUser.setLName(newUser.getLName());
            registerUser.setEmail(newUser.getEmail());
            if(newUser.getRole().equals("admin")){
                registerUser.setRole("ROLE_ADMIN");
            }else if (newUser.getRole().equals("super-admin")){
                registerUser.setRole("ROLE_SUPER_ADMIN");
            }else {
                registerUser.setRole("ROLE_VENDOR");
            }

            BCryptPasswordEncoder bCrypt = new BCryptPasswordEncoder();
            String encPwd = bCrypt.encode(newUser.getPassword());
            registerUser.setPassword(encPwd);

            userRepo.save(registerUser);

            // Send email with raw password
            mailServices.sendCredentialsEmail(registerUser, newUser.getPassword());

            return ResponseEntity.ok(Map.of("message" , "Successfully Registered, " + newUser.getRole()));
        }
    }

    //----------- for testing image get from db --------------------
    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable int id) throws Exception {
        Products products = productsRepo.findById((long) id)
                .orElseThrow(() -> new RuntimeException("Product not found"));


        byte[] imageBytes = products.getImage();
        if (imageBytes == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(imageBytes);
    }

    // This serves the Thymeleaf page
    @GetMapping("/images")
    public String listProducts(Model model) {
        model.addAttribute("products", productsRepo.findAll());
        return "image"; // Thymeleaf template: products.html
    }
}


