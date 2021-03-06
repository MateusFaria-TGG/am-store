package br.com.les.amstore.service.serviceImpl;

import br.com.les.amstore.domain.Game;
import br.com.les.amstore.repository.Games;
import br.com.les.amstore.service.IGameService;
import br.com.les.amstore.service.IGenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameServiceImpl implements IGenericService<Game>, IGameService {
    @Autowired
    Games games;

    @Override
    public List<Game> findAll() {
        return games.findAll(Sort.by("title"));
    }

    @Override
    public Game findById(Long id) {
        return games.findById(id).get();
    }

    @Override
    public Game saveAndFlush(Game object) {
        return games.saveAndFlush(object);
    }

    @Override
    public List<Game> findAllByActiveTrue() {
        return games.findAllByActiveTrue(Sort.by("title"));
    }
}
