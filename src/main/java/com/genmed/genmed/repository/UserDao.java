package com.genmed.genmed.repository;

import com.genmed.genmed.model.Orders;
import com.genmed.genmed.model.Shop;
import com.genmed.genmed.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Transactional
@Repository
public class UserDao {

    @Autowired
    JdbcTemplate jt;

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jt = jdbcTemplate;
    }

    private DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    private DateFormat tf = new SimpleDateFormat("HH:mm:ss");

    public boolean userExists(String email_id) {
        String query = "select count(*) from user where email_id=?";
        int cnt = jt.queryForObject(query, Integer.class, email_id);
        if(cnt == 0) return false;
        else return true;
    }

    public List<String> getPhoneByID(int user_id){
        String query = "select phone_no from userPhone where user_id="+user_id;
        List<String> res = new ArrayList<String>();
        List<Map<String,Object>> rs = jt.queryForList(query);
        for( Map<String,Object> r:rs ) {
            String s = (String) r.get("phone_no");
            res.add(s);
        }
        return res;
    }

    public User findByUsername(String email_id) {
        String query = "select * from user where email_id='"+email_id+"'";
        return jt.queryForObject(query, new RowMapper<User>() {

            public User mapRow(ResultSet row, int i) throws SQLException {
                User u = new User();
                u.setUser_id(row.getInt("user_id"));
                u.setEmail_id(row.getString("email_id"));
                u.setFirst_name(row.getString("first_name"));
                u.setLast_name(row.getString("last_name"));
                u.setEmail_id(row.getString("email_id"));
                u.setPassword(row.getString("password"));
                u.setRole(row.getString("role"));
                return u;
            }
        });
    }

    public void save(String f_name,String l_name,String email_id,String password, String role) {
        String query = "insert into user(first_name, last_name, email_id, password,role) values (?,?,?,?,?)";
        jt.update(query, f_name, l_name, email_id, password, role);
    }

    public void saveUserPhone(String phone_no, Integer user_id) {
        String query = "insert into userPhone values (?,?)";
        jt.update(query, phone_no, user_id);
    }

    public void deleteUserPhone(String phone_no, Integer user_id) {
        String query = "delete from userPhone where phone_no=? and user_id=?";
        jt.update(query, phone_no, user_id);
    }

    public void saveUserReview(String comment, BigDecimal rating, Integer order_id) {
        String query = "insert into reviews(comment, rating, order_id) values (?,?,?)";
        jt.update(query, comment, rating, order_id);
    }

    public void deleteUserReview(Integer review_id) {
        String query = "delete from reviews where review_id=?";
        jt.update(query, review_id);
    }

    public int getUserIDByEmailID(String email_id) {
        String query = "select user_id from user where email_id=?";
        return jt.queryForObject(query, Integer.class, email_id);
    }

    public User getUserDetailByID(int user_id) {
        String query = "select * from user where user_id="+user_id;
        return jt.queryForObject(query, new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet row, int i) throws SQLException {
                User u = new User();
                u.setEmail_id(row.getString("email_id"));
                u.setFirst_name(row.getString("first_name"));
                u.setLast_name(row.getString("last_name"));
                u.setEmail_id(row.getString("email_id"));
                u.setPassword(row.getString("password"));
                return u;
            }
        });
    }

    public List<Orders> getOrdersByUserID(int user_id){
        String query = "select * from orders where user_id="+user_id;
        return jt.query(query, new RowMapper<Orders>() {
            @Override
            public Orders mapRow(ResultSet r, int i) throws SQLException {
                Orders o = new Orders();
                o.setOrder_id(r.getInt("order_id"));
                o.setOrder_date(df.format(r.getDate("order_date")));
                o.setOrder_time(tf.format(r.getTime("order_time")));
                o.setBill_amount(r.getBigDecimal("bill_amount"));
                o.setStatus(r.getString("order_status"));
                o.setShop_id(r.getInt("shop_id"));
                o.setUser_id(r.getInt("user_id"));
                return o;
            }
        });
    }

    public void changeRoleToShop(int user_id){
        String query = "update user set role='shop' where user_id="+user_id;
        jt.update(query);
    }

    public List<Shop> getShopsOfUser(int user_id) {
        String query = "select s.shop_name, s.shop_id from shop s,owner o where s.shop_id=o.shop_id and o.user_id="+user_id;
        List<Shop> res = new ArrayList<Shop>();
        List<Map<String,Object>> rs = jt.queryForList(query);
        for ( Map<String, Object> r:rs) {
            Shop s = new Shop();
            s.setShop_name((String) r.get("shop_name"));
            s.setShop_id((int) r.get("shop_id"));
            res.add(s);
        }
        return res;
    }
}
