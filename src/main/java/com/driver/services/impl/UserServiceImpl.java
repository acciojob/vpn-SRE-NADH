package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception{
        HashSet<String> hs = AdminServiceImpl.converToHs();
        if(!hs.contains(countryName.toUpperCase())){
            throw new Exception("Country not found");
        }
        Country country = new Country();
        country.setCountryName(CountryName.valueOf(countryName.toUpperCase()));
        country.setCode(country.getCountryName().toCode());

        User user = new User();
        user.setConnected(false);
        user.setOriginalCountry(country);
        user.setUsername(username);
        user.setPassword(password);
        String tmp = country.getCode()+"."+user.getId();
        user.setOriginalIp(tmp);
        user.setMaskedIp(null);
        country.setUser(user);

        Country savedCountry = countryRepository3.save(country);
        return savedCountry.getUser();
    }

     @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
       User user = userRepository3.findById(userId).get();
       ServiceProvider serviceProvider = serviceProviderRepository3.findById(serviceProviderId).get();
       user.getServiceProviderList().add(serviceProvider);
       serviceProvider.getUsers().add(user);
       ServiceProvider serviceProvider1=serviceProviderRepository3.save(serviceProvider);
       return user;
    }
}
