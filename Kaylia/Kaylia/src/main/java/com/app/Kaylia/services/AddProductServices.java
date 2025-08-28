package com.app.Kaylia.services;

import com.app.Kaylia.model.Products;
import com.app.Kaylia.model.User;
import com.app.Kaylia.repository.ProductsRepo;
import com.app.Kaylia.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AddProductServices {

    @Autowired
    private ProductsRepo productsRepo;

    @Autowired
    private UserRepo userRepo;

    public Boolean addProduct(String name, double price, String description, String category, List<String> ingredients, List<String> benefits, String howtouse, MultipartFile image, String email) throws IOException {
        User activeUser = userRepo.findByEmail(email);

        Products addProduct = new Products();
        addProduct.setName(name);
        addProduct.setPrice(price);
        addProduct.setDescription(description);
        addProduct.setCategories(category);
        addProduct.setIngredients(ingredients);
        addProduct.setBenefits(benefits);
        addProduct.setHowToUse(howtouse);
        addProduct.setTimeCreated(LocalDateTime.now());
        addProduct.setCreatedBy(activeUser);

        if(image != null && !image.isEmpty()){
            addProduct.setImage(image.getBytes());
        }

        productsRepo.save(addProduct);

        return true;
    }
}
