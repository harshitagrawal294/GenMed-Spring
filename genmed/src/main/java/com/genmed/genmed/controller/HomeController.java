package com.genmed.genmed.controller;

import com.genmed.genmed.model.Medicine;
import com.genmed.genmed.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;

@RequestMapping
@Controller
public class HomeController {

    @Autowired
    private SearchService searchService;

    public enum FilterByEnum {
        gen, cus
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/search**")
    public String search(
            @RequestParam(value="type", required=true) FilterByEnum type,
            @RequestParam(value="name", required=true) String med,
            Model result) {

            List<Medicine> medicines;

            if (type == FilterByEnum.gen){ medicines = searchService.searchByGenName(med); }
            else if (type == FilterByEnum.cus){ medicines = searchService.searchByCusName(med); }
            else { medicines = null; }

            result.addAttribute("medicines", medicines);

            return "search";
    }
}