package lk.chandrika_stores.asset.brand.controller;



import lk.chandrika_stores.asset.brand.entity.Brand;
import lk.chandrika_stores.asset.brand.service.BrandService;
import lk.chandrika_stores.asset.category.controller.CategoryRestController;
import lk.chandrika_stores.asset.item.entity.enums.MainCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/brand")
public class BrandController {
    private final BrandService brandService;

    @Autowired
    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("brands", brandService.findAll());
        return "brand/brand";
    }

    @GetMapping("/add")
    public String form(Model model) {
        model.addAttribute("addStatus", true);
        model.addAttribute("brand", new Brand());
        model.addAttribute("mainCategories", MainCategory.values());
        model.addAttribute("urlMainCategory", MvcUriComponentsBuilder
            .fromMethodName(CategoryRestController.class, "getCategoryByMainCategory", "")
            .build()
            .toString());

        return "brand/addBrand";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("addStatus", false);
        model.addAttribute("brand", brandService.findById(id));model.addAttribute("urlMainCategory", MvcUriComponentsBuilder
            .fromMethodName(CategoryRestController.class, "getCategoryByMainCategory", "")
            .build()
            .toString());

        model.addAttribute("mainCategories", MainCategory.values());
        return "brand/addBrand";
    }

    @PostMapping(value = {"/save", "/update"})
    public String persist(@Valid @ModelAttribute Brand brand, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {


        Brand name = null;

        if ( brand.getName() != null && brand.getId() == null ) {
            name = brandService.findByName(brand.getName());
        }
        if ( name != null ) {
            ObjectError error = new ObjectError("name",
                                                "Their is already Brand on same name . System message ");
            bindingResult.addError(error);
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("addStatus", true);
            model.addAttribute("brand", brand);model.addAttribute("urlMainCategory", MvcUriComponentsBuilder
                .fromMethodName(CategoryRestController.class, "getCategoryByMainCategory", "")
                .build()
                .toString());

            model.addAttribute("mainCategories", MainCategory.values());
            return "brand/addBrand";
        }

        try {
            brandService.persist(brand);
        } catch ( Exception e ) {
            ObjectError error = new ObjectError("brand",
                                                "Please resolve following erros . System message "+e.getCause().getCause().getMessage());
            bindingResult.addError(error);
            e.printStackTrace();
            if (bindingResult.hasErrors()) {
                model.addAttribute("addStatus", true);
                model.addAttribute("brand", brand);model.addAttribute("urlMainCategory", MvcUriComponentsBuilder
                    .fromMethodName(CategoryRestController.class, "getCategoryByMainCategory", "")
                    .build()
                    .toString());

                model.addAttribute("mainCategories", MainCategory.values());
                return "brand/addBrand";
            }
        }
        return "redirect:/brand";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, Model model) {
        brandService.delete(id);
        return "redirect:/brand";
    }
}
