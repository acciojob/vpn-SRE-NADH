package com.driver.services.impl;

import com.driver.model.Admin;
import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.repository.AdminRepository;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    AdminRepository adminRepository1;

    @Autowired
    ServiceProviderRepository serviceProviderRepository1;

    @Autowired
    CountryRepository countryRepository1;

    @Override
    public Admin register(String username, String password) {

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(password);
        Admin savedAdmin = adminRepository1.save(admin);
        return savedAdmin;
    }

    @Override
    public Admin addServiceProvider(int adminId, String providerName) {
        Admin admin = adminRepository1.findById(adminId).get();
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setName(providerName);
        serviceProvider.setAdmin(admin);
        admin.getServiceProviders().add(serviceProvider);
        Admin savedAdmin = adminRepository1.save(admin);
        return admin;
    }

    @Override
    public ServiceProvider addCountry(int serviceProviderId, String countryName) throws Exception{
       HashSet<String> hs = converToHs();
       if((!hs.contains(countryName.toUpperCase()))){
           throw new Exception("Country not found");
       }
       ServiceProvider serviceProvider = serviceProviderRepository1.findById(serviceProviderId).get();
       Country country = new Country();
       country.setCountryName(CountryName.valueOf(countryName.toUpperCase()));
       country.setServiceProvider(serviceProvider);
       country.setCode(country.getCountryName().toCode());
       country.setUser(null);
       serviceProvider.getCountryList().add(country);
       ServiceProvider savedServiceProvider = serviceProviderRepository1.save(serviceProvider);
       return serviceProvider;
    }
   public static HashSet<String> converToHs(){
       HashSet<String> hs = new HashSet<>();
       hs.add("IND");
       hs.add("JPN");
       hs.add("USA");
       hs.add("AUS");
       hs.add("CHI");
       return hs;
   }
}
