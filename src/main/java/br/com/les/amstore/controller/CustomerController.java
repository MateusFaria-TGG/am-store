package br.com.les.amstore.controller;


import br.com.les.amstore.domain.Address;
import br.com.les.amstore.domain.Customer;
import br.com.les.amstore.domain.Document;
import br.com.les.amstore.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    private ICustomersService customers;

    @Autowired
    private ICustomerTypeService customerTypes;

    @Autowired
    private IDocumentService documents;

    @Autowired
    private IDocumentTypeService documentTypes;

    @Autowired
    private IPersonService people;

    @Autowired
    private IStateService states;

    @Autowired
    private IAddressService addresses;

    @Autowired
    private IAddressTypeService addressTypes;

    @Autowired
    private ICityService cities;

    @GetMapping("/new")
    public ModelAndView newCostumer(Customer customer) {
        ModelAndView mv = new ModelAndView("/customers/newCustumer");
        customer.setCustomerType(customerTypes.findById(Long.parseLong("1")));

        mv.addObject(customer);
        mv.addObject("customerTypes", customerTypes.findAll());

        return mv;
    }

    @PostMapping("/new")
    public ModelAndView createCostumer(@Valid Customer customer, BindingResult result, RedirectAttributes attributes) {
        if(result.hasErrors()){
            return newCostumer(customer);
        }

        customers.saveAndFlush(customer);

        ModelAndView mv = new ModelAndView("redirect:/customer/edit/" + customer.getId() + "");

        mv.addObject("customerTypes", customerTypes.findAll());

        attributes.addFlashAttribute("message", "Usuário criado com sucesso!");

        return mv;
    }

    @GetMapping("/edit/{id}")
    public ModelAndView editCustomer(@PathVariable("id") Customer customer) {
        ModelAndView mv = new ModelAndView("/customers/newCustumer");

        mv.addObject("customerTypes", customerTypes.findAll());
        mv.addObject(customer);

        return mv;
    }

    @PostMapping("/edit/{id}")
    public ModelAndView updateCustomer(@Valid Customer customer, BindingResult result, RedirectAttributes attributes) {
        if(result.hasErrors()){
            return editCustomer(customer);
        }

        customers.saveAndFlush(customer);

        ModelAndView mv = new ModelAndView("redirect:/customer/edit/" + customer.getId());

        mv.addObject("customerTypes", customerTypes.findAll());
        mv.addObject(customer);

        attributes.addFlashAttribute("message", "Usuário atualizado com sucesso!");

        return mv;
    }

    @GetMapping("/edit/{id}/documents")
    public ModelAndView customerDocuments(@PathVariable("id") Customer customer) {
        ModelAndView mv = new ModelAndView("/customers/listCustumerDocuments");

        mv.addObject(customer);
        return mv;
    }

    @GetMapping("/edit/{id}/documents/new")
    public ModelAndView newCustomerDocuments(@PathVariable("id") Customer customer, Document document) {
        ModelAndView mv = new ModelAndView("/customers/newDocument");

        mv.addObject("documentTypes", documentTypes.findAll());
        mv.addObject(customer);
        mv.addObject(document);

        return mv;
    }

    @PostMapping("/edit/{id}/documents/new")
    public ModelAndView createCustomerDocument(@Valid Document document, Customer customer, BindingResult result, RedirectAttributes attributes) {
        if(result.hasErrors()){
            return newCustomerDocuments(customer, document);
        }

        document.setPerson(customer);
        documents.saveAndFlush(document);

        ModelAndView mv = new ModelAndView("redirect:/customer/edit/" + customer.getId() + "/documents/");
        mv.addObject("documentTypes", documentTypes.findAll());
        mv.addObject(customer);
        mv.addObject(document);

        attributes.addFlashAttribute("message", "Documento criado com sucesso!");

        return mv;
    }

    @GetMapping("/edit/{id}/documents/{id_doc}/edit")
    public ModelAndView editDocument(@PathVariable("id") Customer customer, @PathVariable("id_doc") Document document) {
        ModelAndView mv = new ModelAndView("/customers/newDocument");

        mv.addObject("documentTypes", documentTypes.findAll());
        mv.addObject(customer);
        mv.addObject(document);

        return mv;
    }

    @GetMapping("/edit/{id}/addresses")
    public ModelAndView listAddress(@PathVariable("id") Customer customer, Address address) {
        ModelAndView mv = new ModelAndView("/customers/listAddresses");

        mv.addObject(customer);
        mv.addObject(address);

        return mv;
    }

    @GetMapping("/edit/{id}/addresses/new")
    public ModelAndView newAddress(@PathVariable("id") Customer customer, Address address) {
        ModelAndView mv = new ModelAndView("/customers/newAddress");

        mv.addObject("addressesTypes", addressTypes.findAll());
        mv.addObject("states", states.findAll());
        mv.addObject(customer);
        mv.addObject(address);

        return mv;
    }

    @PostMapping("/edit/{id}/addresses/new")
    public ModelAndView createCustomerDocument(@PathVariable("id") Customer customer, @Valid Address address, BindingResult result, RedirectAttributes attributes) {
        ModelAndView mv = new ModelAndView("redirect:/customer/edit/" + customer.getId() + "/addresses/new");

        if(result.hasErrors() || null == customer.getId()){
            System.err.println("Deu erro");
            return newAddress(customer, address);
        }

        customer = customers.findById(customer.getId());
        address.setCustomer(customer);

        mv.addObject(customer);
        mv.addObject(address);

        attributes.addFlashAttribute("message", "Endereço criado com sucesso!");

        addresses.saveAndFlush(address);

        return mv;
    }

    @GetMapping("/edit/{id}/addresses/{address_id}/edit")
    public ModelAndView editAddress(@PathVariable("id") Customer customer, @PathVariable("address_id") Address address) {
        ModelAndView mv = new ModelAndView("/customers/newAddress");

        mv.addObject("addressesTypes", addressTypes.findAll());
        mv.addObject("states", states.findAll());
        mv.addObject(customer);
        mv.addObject(address);

        return mv;
    }

    @PostMapping(value = "/edit/{customer_id}/addresses/{id}/edit")
    public ModelAndView updateAddress(@PathVariable(value = "customer_id") Customer customer, @Valid Address address, BindingResult result, RedirectAttributes attributes) {
        if(result.hasErrors() || null == customer.getId()){
            System.err.println("Deu erro");
            return newAddress(customer, address);
        }

        customer = customers.findById(customer.getId());

        address.setCustomer(customer);

        ModelAndView mv = new ModelAndView("redirect:/customer/edit/" + customer.getId() + "/addresses/" + address.getId() + "/edit");

        mv.addObject(customer);
        mv.addObject(address);

        attributes.addFlashAttribute("message", "Endereço atualizado com sucesso!");

        addresses.saveAndFlush(address);
        return mv;
    }
}
