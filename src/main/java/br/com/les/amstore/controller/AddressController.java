package br.com.les.amstore.controller;

import br.com.les.amstore.domain.Address;
import br.com.les.amstore.domain.Customer;
import br.com.les.amstore.domain.State;
import br.com.les.amstore.dto.CityDTO;
import br.com.les.amstore.service.IAddressService;
import br.com.les.amstore.service.IAddressTypeService;
import br.com.les.amstore.service.ICustomersService;
import br.com.les.amstore.service.IStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/customer/edit")
public class AddressController {

    @Autowired
    private ICustomersService customers;

    @Autowired
    private IStateService states;

    @Autowired
    private IAddressService addresses;

    @Autowired
    private IAddressTypeService addressTypes;

    @GetMapping("/state/{id}")
    public ResponseEntity getAddress(@PathVariable("id") State state) {

        List<CityDTO> cityDTOList = new ArrayList<>();

        state.getCities().forEach(city -> cityDTOList.add(new CityDTO(city.getId(), city.getDescription())));

        if(null != state.getId())
            return ResponseEntity.ok(cityDTOList);
        else
            return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/addresses")
    public ModelAndView listAddress(@PathVariable("id") Customer customer, Address address) {
        ModelAndView mv = new ModelAndView("/customers/listAddresses");

        mv.addObject(customer);
        mv.addObject(address);

        customers.isCurrentUserLoggedIn(customer.getId(), mv);
        return mv;
    }

    @GetMapping("/{id}/addresses/new")
    public ModelAndView newAddress(@PathVariable("id") Customer customer, Address address) {
        ModelAndView mv = new ModelAndView("/customers/newAddress");

        mv.addObject("addressesTypes", addressTypes.findAll());
        mv.addObject("states", states.findAll());
        mv.addObject(customer);
        mv.addObject(address);

        customers.isCurrentUserLoggedIn(customer.getId(), mv);
        return mv;
    }

    @PostMapping("/{id}/addresses/new")
    public ModelAndView createCustomerAddress(@PathVariable("id") Customer customer, @Valid Address address, BindingResult result, RedirectAttributes attributes) {
        ModelAndView mv = new ModelAndView("redirect:/customer/edit/" + customer.getId() + "/addresses/new");

        if(!customers.isCurrentUserLoggedIn(customer.getId()))
            result.addError(new ObjectError("customer", "Você não está autorizado a executar essa ação"));

        if(result.hasErrors() || null == customer.getId()){
            System.err.println("Deu erro");
            return newAddress(customer, address);
        }

        customer = customers.findById(customer.getId());
        address.setCustomer(customer);

        attributes.addFlashAttribute("message", "Endereço criado com sucesso!");

        addresses.saveAndFlush(address);

        return mv;
    }

    @GetMapping("/{id}/addresses/{address_id}/edit")
    public ModelAndView editAddress(@PathVariable("id") Customer customer, @PathVariable("address_id") Address address) {
        ModelAndView mv = new ModelAndView("/customers/newAddress");

        mv.addObject("addressesTypes", addressTypes.findAll());
        mv.addObject("states", states.findAll());
        mv.addObject(customer);
        mv.addObject(address);

        customers.isCurrentUserLoggedIn(customer.getId(), mv);
        return mv;
    }

    @PostMapping(value = "/{customer_id}/addresses/{id}/edit")
    public ModelAndView updateAddress(@PathVariable(value = "customer_id") Customer customer, @Valid Address address,
                                      BindingResult result, RedirectAttributes attributes) {
        if(!customers.isCurrentUserLoggedIn(customer.getId()))
            result.addError(new ObjectError("customer", "Você não está autorizado a executar essa ação"));

        if(result.hasErrors() || null == customer.getId())
            return newAddress(customer, address);

        customer = customers.findById(customer.getId());

        address.setCustomer(customer);

        ModelAndView mv = new ModelAndView("redirect:/customer/edit/" + customer.getId() + "/addresses/" + address.getId() + "/edit");

        attributes.addFlashAttribute("message", "Endereço atualizado com sucesso!");

        addresses.saveAndFlush(address);
        return mv;
    }

    @DeleteMapping("/{id}/addresses")
    public ModelAndView deleteAddress(@PathVariable(value = "id") Customer customer, @RequestParam String id, @RequestParam String address_id, RedirectAttributes attributes) {
        if(!customers.isCurrentUserLoggedIn(customer.getId()))
            return new ModelAndView("redirect:/");

        Address address = addresses.findById(Long.parseLong(address_id));
        address.delete();

        addresses.saveAndFlush(address);

        ModelAndView mv = new ModelAndView("redirect:/customer/edit/" + id + "/addresses");

        attributes.addFlashAttribute("message", "Endereço excluído com sucesso!");

        return mv;
    }

}
