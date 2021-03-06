package br.com.les.amstore.service.serviceImpl;

import br.com.les.amstore.domain.*;
import br.com.les.amstore.dto.ChartDTO;
import br.com.les.amstore.dto.DataSetDTO;
import br.com.les.amstore.repository.*;
import br.com.les.amstore.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {
    @Autowired
    Orders orders;

    @Autowired
    Statuses statuses;

    @Autowired
    CreditCards creditCards;

    @Autowired
    Customers customers;

    @Autowired
    Games games;

    @Autowired
    Genders genders;

    @Override
    public List<Order> findAll() {

        return orders.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public Order findById(Long id) {
        return orders.findById(id).get();
    }

    @Override
    public Order saveAndFlush(Order object) {
        orders.saveAndFlush(object);
        return object;
    }

    @Override
    public Order saveAndFlush(Order order, BindingResult result) {
        fillOrderObject(order, result);

        if(result.hasErrors())
            return order;

        orders.saveAndFlush(order);

        order.getCustomer().getCart().getItemList().clear();

        customers.saveAndFlush(order.getCustomer());
        return order;
    }

    @Override
    public ChartDTO findAllByCreatedAtBetween(Date dateInitial, Date dateFinal, Integer searchType) {
        if(null == searchType)
            searchType = 0;

        List<Order> ordersFiltered = orders.findAllByCreatedAtBetweenOrderByCreatedAt(dateInitial, dateFinal);

        List<HashMap<String, Double>> orderValue = new ArrayList<>();

        Map<LocalDate, List<Order>> groupedByDate = ordersFiltered.stream()
                .collect(Collectors.groupingBy(order ->
                        Date.from(order.getCreatedAt().toInstant().truncatedTo(ChronoUnit.DAYS)).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()));

        ChartDTO chartDTO = new ChartDTO();
        List<DataSetDTO> listDataSetDTOS = new ArrayList<>();

        if(searchType.equals(0)){
            List<Game> allGames = games.findAll();


            for (Game game : allGames) {
                DataSetDTO dataSetDTO = new DataSetDTO();
                List<Double> doubleList = new ArrayList<>();
                dataSetDTO.setLabel(game.getTitle());


                for(List<Order> order : groupedByDate.values()) {
                    Integer amount = 0;
                    for(Order orderValueGroup : order) {
                        for (Item item : orderValueGroup.getItemList()) {
                            if(item.getGame().equals(game))
                                amount++;
                        }
                    }
                    doubleList.add(amount * game.getPrice());
                }

                dataSetDTO.setData(doubleList);
                listDataSetDTOS.add(dataSetDTO);
            }
        } else {
            List<Gender> allGenders = genders.findAll();

            for (Gender gender : allGenders) {
                DataSetDTO dataSetDTO = new DataSetDTO();
                List<Double> doubleList = new ArrayList<>();
                dataSetDTO.setLabel(gender.getName());


                for(List<Order> order : groupedByDate.values()) {
                    Integer amount = 0;
                    Game game = null;
                    for(Order orderValueGroup : order) {
                        for (Item item : orderValueGroup.getItemList()) {
                            if(item.getGame().getGenders().contains(gender)){
                                amount++;
                            }
                            game = item.getGame();
                        }
                    }
                    if(null != game)
                        doubleList.add(amount * game.getPrice());
                }

                dataSetDTO.setData(doubleList);
                listDataSetDTOS.add(dataSetDTO);
            }
        }

        chartDTO.setLabel(groupedByDate.keySet().stream().map(localDate -> localDate.toString()).collect(Collectors.toList()));
        chartDTO.setDatasets(listDataSetDTOS);

        return chartDTO;
    }

    @Override
    public List<HashMap<String, Double>> fillCardsIndex() {
        List<HashMap<String, Double>> cards = new ArrayList<>();

        List<Order> allOrders = orders.findAll();

        HashMap<String, Double> totalOrder = new HashMap<>();
        HashMap<String, Double> cardSingle = new HashMap<>();
        HashMap<String, Double> cardMultiple = new HashMap<>();
        Double totalSingleCreditCard = 0d;
        Double totalMultipleCreditCard = 0d;

        totalOrder.put("total das vendas", allOrders.stream().mapToDouble(o -> o.getTotal()).sum());

        for (Order order : allOrders) {
            if(order.getPaymentMethodList().size() > 1){
                totalMultipleCreditCard += 1;
            } else {
                totalSingleCreditCard += 1;
            }
        }

        cardSingle.put("Vendas (1 cartão de crédito)", totalSingleCreditCard);
        cardMultiple.put("Vendas (vários cartões de crédito)", totalMultipleCreditCard);

        cards.add(totalOrder);
        cards.add(cardSingle);
        cards.add(cardMultiple);

        return cards;
    }

    public Order fillOrderObject(Order order, BindingResult result){
        Status status = statuses.findByStatus("EM PROCESSAMENTO");
        order.setItemList(order.getCustomer().getCart().getItemList());
        Double costs = order.getShippingTax() + order.getItemList().stream()
                .mapToDouble(i -> i.getGame().getPrice() * i.getAmount().doubleValue()).sum();
        Wallet wallet = order.getCustomer().getWallet();

        order.getPaymentMethodList().forEach(p -> p.setCreditCard(creditCards.findById(p.getCreditCard().getId()).get()));

        order.setStatus(status);
        order.setTotal(BigDecimal.valueOf(costs - wallet.getValue())
                .setScale(2, RoundingMode.FLOOR)
                .doubleValue());


        if(null != order.getCoupon()){
            order.setTotal(BigDecimal.valueOf(costs - wallet.getValue() - order.getCoupon().getValue())
                    .setScale(2, RoundingMode.FLOOR)
                    .doubleValue());
        }

        Double total = order.getPaymentMethodList().stream().mapToDouble(paymentMethod -> paymentMethod.getPaymentValue()).sum();

        if(!total.equals(order.getTotal())){
            result.addError(new ObjectError("order", "Valor total e do pagamento são diferentes"));
            return order;
        }

        order.getItemList().forEach(g -> g.getGame().setAmount(g.getGame().getAmount() - g.getAmount()));

        if(costs > wallet.getValue()){
            wallet.setValue(0);
        } else {
            wallet.setValue(wallet.getValue() - costs);
        }

        if(null != order.getCoupon())
            order.getCoupon().setAmount(order.getCoupon().getAmount() - 1);

        return order;
    }

    @Override
    public Order updateOrder(Order order){
        return orders.saveAndFlush(order);
    }
}
