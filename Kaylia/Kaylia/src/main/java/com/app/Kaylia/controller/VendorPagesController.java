package com.app.Kaylia.controller;

import com.app.Kaylia.model.Products;
import com.app.Kaylia.model.User;
import com.app.Kaylia.repository.ConfirmOrderRepo;
import com.app.Kaylia.repository.ProductsRepo;
import com.app.Kaylia.repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/vendor")
public class VendorPagesController {

    @Autowired
    private ProductsRepo productsRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ConfirmOrderRepo confirmOrderRepo;

    @GetMapping("/dashboard")
    @Transactional
    public String vendorDashboardPage(HttpSession session, Model model){
        User activeUser = userRepo.findByEmail((String) session.getAttribute("email"));

        List<Products> productsList = productsRepo.findByCreatedBy(activeUser);

        int totalProducts = 0;

        for(Products product: productsList){
            totalProducts += 1;
        }

        model.addAttribute("activeProducts", totalProducts);
        model.addAttribute("products", productsList);
        model.addAttribute("user", activeUser.getFName() + ' ' + activeUser.getLName());

        return "vendor-dashboard";
    }

    @GetMapping("/analytics")
    @Transactional
    public String vendorAnalyticsPage(HttpSession session,Model model){
        User activeUser = userRepo.findByEmail((String) session.getAttribute("email"));

        List<Products> productsList = productsRepo.findByCreatedBy(activeUser);

        int totalProducts = 0;

        for(Products product: productsList){
            totalProducts += 1;
        }

        model.addAttribute("activeProducts", totalProducts);
        model.addAttribute("products", productsList);

        return "vendor-analytics";
    }

    @GetMapping("/add-product")
    public String vendorAddProduct(){
        return "vendor-add-product";
    }

    @GetMapping("/manage-product")
    @Transactional
    public String vendorManageProduct(HttpSession session, Model model){
        User activeUser = userRepo.findByEmail((String) session.getAttribute("email"));

        List<Products> productsList = productsRepo.findByCreatedBy(activeUser);

        model.addAttribute("products", productsList);

        return "vendor-manage-product";
    }

    @GetMapping("/remove-product/{id}")
    @Transactional
    public String vendorRemoveProduct(@PathVariable Long id){
        productsRepo.removeProductsById(id);

        return "redirect:/vendor/manage-product";
    }

}
