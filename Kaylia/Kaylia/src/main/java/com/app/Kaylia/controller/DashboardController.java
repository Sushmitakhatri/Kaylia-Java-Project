package com.app.Kaylia.controller;

import com.app.Kaylia.model.ConfirmOrder;
import com.app.Kaylia.model.Products;
import com.app.Kaylia.model.SkinAnalysis;
import com.app.Kaylia.model.User;
import com.app.Kaylia.repository.ConfirmOrderRepo;
import com.app.Kaylia.repository.ProductsRepo;
import com.app.Kaylia.repository.SkinAnalysisRepository;
import com.app.Kaylia.repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

@Controller

public class DashboardController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ConfirmOrderRepo confirmOrderRepo;

    @Autowired
    private ProductsRepo productsRepo;

    @Autowired
    private SkinAnalysisRepository skinAnalysisRepository;


    @GetMapping("/dashboard")
    public String dashboardPage(HttpSession session, Model model){
        String email = (String) session.getAttribute("email");

        User user = userRepo.findByEmail(email);

        List<ConfirmOrder> completedOrders = confirmOrderRepo.findByUserId(user.getUserId());

        int totalOrders;
        double  totalSpent = 0;

        totalOrders = completedOrders.size();

        for(ConfirmOrder orders: completedOrders){
            totalSpent += orders.getTotalAmount();
        }

        model.addAttribute("fName", user.getFName());
        model.addAttribute("lName", user.getLName());
        model.addAttribute("email", email);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalSpent", totalSpent);

        return "dashboard";
    }

    @GetMapping("/dashboard-profile")
    public String dashboardProfileSection(HttpSession session, Model model){
        String email = (String) session.getAttribute("email");

        User user = userRepo.findByEmail(email);
        model.addAttribute("fName", user.getFName());
        model.addAttribute("lName", user.getLName());
        model.addAttribute("email", email);
        return "dashboard-profile";
    }

    @GetMapping("/dashboard-order")
    public String dashboardOrderSection(HttpSession session, Model model) {
        try {
            String email = (String) session.getAttribute("email");
            User user = userRepo.findByEmail(email);

            List<ConfirmOrder> completedOrders = confirmOrderRepo.findByUserId(user.getUserId());

            int totalOrders;
            double  totalSpent = 0;

            totalOrders = completedOrders.size();


            for(ConfirmOrder orders: completedOrders){
                totalSpent += orders.getTotalAmount();
            }

            model.addAttribute("totalOrders", totalOrders);
            model.addAttribute("totalSpent", totalSpent);
            model.addAttribute("pastOrders", completedOrders);

            return "dashboard-order";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //to get product name and price for order history dsiplay
    @GetMapping("/product-name/{id}")
    @ResponseBody
    public ResponseEntity<String> getProductName(@PathVariable int id){
        Products products = productsRepo.findById((long) id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return new ResponseEntity<>(products.getName(), HttpStatus.OK);
    }

    @GetMapping("/product-price/{id}")
    @ResponseBody
    public ResponseEntity<String> getProductPrice(@PathVariable int id){
        Products products = productsRepo.findById((long) id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return new ResponseEntity<>(String.valueOf(products.getPrice()), HttpStatus.OK);
    }

    @GetMapping("/dashboard-wishlist")
    public String dashboardWishListSection(Model model){
        return "dashboard-wishlist";
    }

    @GetMapping("/dashboard-skin-analysis")
    @Transactional
    public String dashboardSkinAnalysis(Model model, HttpSession session){
        User user = userRepo.findByEmail((String)session.getAttribute("email"));

        Optional<SkinAnalysis> skinAnalysis = skinAnalysisRepository.findSkinAnalysisByUser(user);

        if (skinAnalysis.isPresent()) {
            SkinAnalysis analysis = skinAnalysis.get();
            model.addAttribute("skinAnalysisDetail", analysis);
            return "dashboard-skin-analysis";
        }
        model.addAttribute("skinAnalysisDetail", null);
        return "dashboard-skin-analysis";
    }

}
