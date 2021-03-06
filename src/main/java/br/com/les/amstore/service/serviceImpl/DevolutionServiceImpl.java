package br.com.les.amstore.service.serviceImpl;

import br.com.les.amstore.domain.Customer;
import br.com.les.amstore.domain.Devolution;
import br.com.les.amstore.domain.StatusDevolution;
import br.com.les.amstore.domain.Wallet;
import br.com.les.amstore.repository.Devolutions;
import br.com.les.amstore.repository.Orders;
import br.com.les.amstore.repository.Statuses;
import br.com.les.amstore.service.IDevolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DevolutionServiceImpl implements IDevolutionService {
    @Autowired
    Devolutions devolutions;

    @Autowired
    Statuses statuses;

    @Override
    public List<Devolution> findAll() {
        return devolutions.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public Devolution findById(Long id) {
        return devolutions.findById(id).get();
    }

    @Override
    public Devolution saveAndFlush(Devolution object) {
        return devolutions.saveAndFlush(object);
    }

    @Override
    public Devolution sendDevolutionRequest(Devolution devolution) {
        devolution.getOrder().setStatus(statuses.findByStatus("EM PROCESSO DE TROCA"));

        this.saveAndFlush(devolution);
        return devolution;
    }

    @Override
    public Devolution updateDevolutionRequest(Devolution devolution, Double valueWallet) {
        switch (devolution.getStatusDevolution()){
            case REFUSED:
                devolution.getOrder().setStatus(statuses.findByStatus("TROCA RECUSADA"));
                break;

            case ACCEPTED:
                Wallet wallet = devolution.getOrder().getCustomer().getWallet();
                wallet.setValue(wallet.getValue() + valueWallet);
                devolution.getOrder().setStatus(statuses.findByStatus("TROCA APROVADA"));
                break;

            case IN_PROCESS:
                devolution.getOrder().setStatus(statuses.findByStatus("EM PROCESSO DE TROCA"));
                break;

            default:
                break;
        }

        devolutions.saveAndFlush(devolution);

        return devolution;
    }
}
