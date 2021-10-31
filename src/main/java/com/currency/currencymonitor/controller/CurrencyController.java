package com.currency.currencymonitor.controller;

import com.currency.currencymonitor.model.Currency;
import com.currency.currencymonitor.model.History;
import com.currency.currencymonitor.repository.CurrencyRepository;
import com.currency.currencymonitor.repository.HistoryRepository;
import com.currency.currencymonitor.response.CurrencyListResponse;
import com.currency.currencymonitor.response.CurrencyPriceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping(value="/api")
public class CurrencyController {

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    HistoryRepository historyRepository;

    @PostMapping(value = "/currency",consumes = {"application/json"})
    public ResponseEntity<List<Currency>> updateCurrencies(@RequestBody List<Currency> currencies){
        try {
            List<Currency> newCurrencies = new ArrayList<>();
            for (Currency currency : currencies){
                String name = currency.getName();
                Optional<Currency> foundCurrency = currencyRepository.findByName(name);
                Currency _currency;
                if(foundCurrency.isPresent()){
                    _currency = foundCurrency.get();
                    if(_currency.getTimestamp() < currency.getTimestamp()){
                        _currency.addToHistories(new History(
                                currency.getPrice(),
                                currency.getTimestamp()
                        ));
                        _currency.setPrice(currency.getPrice());
                        _currency.setGrowth(currency.getGrowth());
                        _currency.setTimestamp(currency.getTimestamp());
                    }
                } else{
                    _currency = currency;
                    _currency.addToHistories(new History(
                            currency.getPrice(),
                            currency.getTimestamp()));
                }
                newCurrencies.add(_currency);
            }
            //System.out.println(newCurrencies);
            currencyRepository.saveAll(newCurrencies);
            return new ResponseEntity<>(newCurrencies, HttpStatus.ACCEPTED);
        } catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/currency")
    public ResponseEntity<Currency> getCurrencyInfo(@RequestParam Long id){
        Optional<Currency> currency = currencyRepository.findById(id);
        if (currency.isPresent()){
            return new ResponseEntity<>(currency.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/currencies")
    public ResponseEntity<CurrencyListResponse> getCurrencies(@RequestParam(name = "type") String type,
                                                      @RequestParam(name = "limit") int limit,
                                                      @RequestParam(name = "offset") int offset,
                                                      @RequestParam(name = "name", required = false) String name,
                                                      @RequestParam(name = "orderBy") String orderBy,
                                                      @RequestParam(name = "direction") String direction){
        try{
            Sort sorting = (direction.toUpperCase().equals("ASC")) ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending();
            Pageable paging = PageRequest.of(offset / limit, limit, sorting);
            Page<Currency> pagedResult;
            if(name != null){
                pagedResult = currencyRepository.findAllByTypeAndNameContainingIgnoreCase(type, name, paging);
            } else {
                pagedResult = currencyRepository.findAllByType(type, paging);
            }
            CurrencyListResponse response = new CurrencyListResponse();
            response.setItems(pagedResult.getContent());
            response.setTotal_count(currencyRepository.findQuantityByType(type));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/currency-updates")
    public ResponseEntity<CurrencyPriceResponse> getCurrencyUpdates(@RequestParam long id){
        Optional<Currency> currency = currencyRepository.findById(id);

        if (currency.isPresent()){
            Currency _currency = currency.get();
            History prevHistory = historyRepository.findByCurrencyLastPrice(_currency.getId()).get(0);
            CurrencyPriceResponse response = new CurrencyPriceResponse(
                    prevHistory.getPrice(),
                    _currency.getPrice(),
                    _currency.getTimestamp()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/currency-historical")
    public ResponseEntity<List<History>> getHistoryOf(@RequestParam long id){
        Optional<Currency> currency = currencyRepository.findById(id);

        if (currency.isPresent()){
            Currency _currency = currency.get();
            List<History> response = historyRepository.findByCurrencyOrderByTimestampDesc(_currency);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
