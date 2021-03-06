package br.com.les.amstore.service;

import br.com.les.amstore.domain.Customer;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

public interface ICustomersService {
    public List<Customer> findAll();
    public Customer findById(Long id);
    public Customer saveAndFlush(Customer customer);
    Customer findByEmail(String email);
    Customer currentUserLoggedIn();
    void isCurrentUserLoggedIn(Long id, ModelAndView mv);
    boolean isCurrentUserLoggedIn(Long id);
}
