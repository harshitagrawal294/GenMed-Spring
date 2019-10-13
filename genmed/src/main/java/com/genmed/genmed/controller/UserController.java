package com.genmed.genmed.controller;


import com.genmed.genmed.model.*;
import com.genmed.genmed.repository.AddressDao;
import com.genmed.genmed.repository.OrderDao;
import com.genmed.genmed.repository.ShopDao;
import com.genmed.genmed.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
public class UserController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private ShopDao shopDao;

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private OrderDao orderDao;

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat tf = new SimpleDateFormat("HH:mm:ss");

    @RequestMapping("/self/profile")
    public String userProfile(Model model, Principal p) {
        int user_id = userDao.getUserIDByEmailID(p.getName());
        model.addAttribute("user", userDao.getUserDetailByID(user_id));
        model.addAttribute("addresses", addressDao.getAddressesByUserID(user_id));
        return "userProfile";
    }

    @GetMapping("/self/add_address")
    public String addAddress(Model model, Principal p) {
        model.addAttribute("address", new Address());
        return "userAddress";
    }

    @PostMapping("/self/add_address")
    public String addAddress(@ModelAttribute("address") Address a, Principal p) {
        int address_id = addressDao.saveAddress(a.getPlot_no(), a.getStreet(), a.getCity(), a.getDistrict(), a.getState(), a.getLatitude(), a.getLongitude());
        addressDao.saveAddressOfUser(address_id, a.getAddress_type(), userDao.getUserIDByEmailID(p.getName()));
        return "redirect:/self/profile";
    }

    @GetMapping("/self/add_phone")
    public String addPhone(Model model, Principal p){
        return "userPhone";
    }

    @PostMapping("/self/add_phone")
    public String addPhone(@RequestParam String phone_no, Principal p){
        int user_id = userDao.getUserIDByEmailID(p.getName());
        userDao.saveUserPhone(phone_no, user_id);
        return "redirect:/self/profile";
    }


    @GetMapping("/self/orders")
    public String userOrders(Model model, Principal p){
        int user_id = userDao.getUserIDByEmailID(p.getName());
        model.addAttribute("orders", userDao.getOrdersByUserID(user_id));
        return "userOrders";
    }

    @RequestMapping("/self/order/{order_id}")
    public String userOrderDetail(Model model, @PathVariable int order_id){
        model.addAttribute("order", orderDao.getOrderByID(order_id));
        return "userOrdersDetail";
    }

    @GetMapping("/self/order/{order_id}/add_review")
    public String userOrderAddReview(Model model, @PathVariable int order_id){
        model.addAttribute("order_id", order_id);
        model.addAttribute("reviews", new Reviews());
        return "userReview";
    }

    @PostMapping("/self/order/{order_id}/add_review")
    public String userOrderAddReview(@ModelAttribute("reviews") Reviews r, @PathVariable int order_id, Principal p) {
        userDao.saveUserReview(r.getComment(), r.getRating(), r.getOrder_id());
        return "redirect:/user/order/"+order_id;
    }

    @GetMapping("/self/add_shop")
    public String addShop(Model model, Principal p) {
        model.addAttribute("shop", new Shop());
        model.addAttribute("address", new Address());
        return "createShop";
    }

    @PostMapping("/self/add_shop")
    public String addShop(@ModelAttribute("shop") Shop s, @ModelAttribute("address") Address a, Principal p) {

        int address_id = addressDao.saveAddress(a.getPlot_no(), a.getStreet(), a.getCity(), a.getDistrict(), a.getState(), a.getLatitude(), a.getLongitude());
        int shop_id = shopDao.save(s.getShop_name(), s.getEmail_id(), s.getLicense(), address_id);
        int user_id = userDao.getUserIDByEmailID(p.getName());

        // Include code for checking if the same address is present in the database

        shopDao.addOwner(user_id, shop_id);
        userDao.changeRoleToShop(user_id);
        return "redirect:/self/shops";
    }

    @RequestMapping("/self/shops")
    public String selfShops(Model model, Principal p){
        User u = userDao.findByUsername(p.getName());
        model.addAttribute("user", u);
        model.addAttribute("shops", userDao.getShopsOfUser(u.getUser_id()));
        return "userShops";
    }


    @RequestMapping("/self/shop/{shop_id}/place_order")
    public String placeOrder(@ModelAttribute("order") Orders o, Principal p, @PathVariable int shop_id){
        Date now = new Date();
        int order_id = orderDao.save(
                df.format(now),tf.format(now),
                new BigDecimal(0.0), "ordering", shop_id,
                userDao.getUserIDByEmailID(p.getName())
        );
        return "redirect:/self/shop/"+shop_id+"/order/"+order_id+"/add_items";
    }

    @GetMapping("/self/shop/{shop_id}/order/{order_id}/add_items")
    public String addItems(Model m, Principal p, @PathVariable int shop_id, @PathVariable int order_id){
        m.addAttribute("item", new ItemsOrdered());
        m.addAttribute("shop_id", shop_id);
        m.addAttribute("order", orderDao.getOrderByID(order_id));
        m.addAttribute("items", shopDao.getInventoryByID(shop_id));
        return "addOrderItems";
    }

    @PostMapping("/self/shop/{shop_id}/order/{order_id}/add_items")
    public String addItems(@ModelAttribute("item") ItemsOrdered i, Principal p, @PathVariable int shop_id, @PathVariable int order_id){

        orderDao.addItemToOrder(order_id, i.getQuantity(), i.getAmount(), i.getItem_id(), shop_id);
        return "redirect:/self/shop/"+shop_id+"/order/"+order_id+"/add_items";
    }

    @RequestMapping("/self/cancel/order/{order_id}")
    public String cancelOrder(Model m, Principal p, @PathVariable int order_id){
        orderDao.removeOrder(order_id);
        return "redirect:/self/orders";
    }

    @RequestMapping("/self/shop/{shop_id}/order/{order_id}/complete")
    public String completeOrder(@PathVariable int shop_id, @PathVariable int order_id){
        orderDao.changeOrderStatus(order_id, "ordered");
        return "redirect:/self/order/"+order_id;
    }
}
