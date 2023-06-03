package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{
    User user = userRepository2.findById(userId).get();
    if(user.isConnected()){
        throw new Exception("Already connected");
    }
    if(user.getCountry().toString().equals(countryName.toUpperCase())){
        return user;
    }

    if(user.getServiceProviderList().isEmpty()){
        throw new Exception("Unable to connect");
    }

    List<ServiceProvider> serviceProviders = user.getServiceProviderList();
    serviceProviders.sort(Comparator.comparingInt(ServiceProvider->ServiceProvider.getId()));
  ServiceProvider serviceProvidertmp=null;
  Country countrytmp=null;
    for(ServiceProvider serviceProvider:serviceProviders){
        List<Country> countries = serviceProvider.getCountryList();
        for(Country country:countries){
            if(country.getCountryName().toString().equals(countryName.toUpperCase())){
                serviceProvidertmp=serviceProvider;
                countrytmp = country;
                break;
            }
        }
    }
     if(serviceProvidertmp==null && countrytmp==null){
         throw new Exception("Unable to connect");
     }

     Connection connection = new Connection();
     connection.setUser(user);
     connection.setServiceProvider(serviceProvidertmp);
     user.getConnectionList().add(connection);
     serviceProvidertmp.getConnectionList().add(connection);
     String tmp = countrytmp.getCode()+"."+serviceProvidertmp.getId()+"."+user.getId();
     user.setMaskedIp(tmp);
     User savedUser = userRepository2.save(user);
     return user;
    }
    @Override
    public User disconnect(int userId) throws Exception {
    User user = userRepository2.findById(userId).get();
    if(!user.isConnected()){
        throw new Exception("Already disconnected");
    }
    user.setConnected(false);
    Connection connection = user.getConnectionList().get(0);
    ServiceProvider serviceProvider=connection.getServiceProvider();
    serviceProvider.getConnectionList().remove(connection);
    user.setMaskedIp(null);
    serviceProviderRepository2.save(serviceProvider);
    user.getConnectionList().remove(connection);
    return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
     User sender = userRepository2.findById(senderId).get();
     User receiver = userRepository2.findById(receiverId).get();
     if(!receiver.isConnected() && sender.getCountry().getCountryName().equals(receiver.getCountry().getCountryName())){
         return sender;
     }
     Country recieverCountry=null;
     String arr[] = sender.getMaskedIp().split(".");
     ServiceProvider serviceProvider = serviceProviderRepository2.findById(Integer.parseInt(arr[1])).get();
     List<Country> countryList = serviceProvider.getCountryList();
     for(Country country:countryList){
         if(country.getCode().equals(arr[0])){
             recieverCountry=country;
         }
     }
     if(recieverCountry.getCountryName().equals(sender.getCountry().getCountryName())){
         return sender;
     }

        if(sender.getServiceProviderList().isEmpty()){
            throw new Exception("Cannot establish communication");
        }



        List<ServiceProvider> serviceProviders = sender.getServiceProviderList();
        serviceProviders.sort(Comparator.comparingInt(ServiceProvider->ServiceProvider.getId()));
        ServiceProvider serviceProvidertmp=null;
        for(ServiceProvider senderProvider:serviceProviders){
            List<Country> countries = senderProvider.getCountryList();
            for(Country country:countries){
                if(country.getCountryName().toString().equals(recieverCountry.getCountryName().toString())){
                    serviceProvidertmp=serviceProvider;
                    break;
                }
            }
        }
        if(serviceProvidertmp==null){
            throw new Exception("Cannot establish communication");
        }


        Connection connection = new Connection();
        connection.setUser(sender);
        connection.setServiceProvider(serviceProvidertmp);
        sender.getConnectionList().add(connection);
        serviceProvidertmp.getConnectionList().add(connection);
        String tmp = recieverCountry.getCode()+"."+serviceProvidertmp.getId()+"."+sender.getId();
        sender.setMaskedIp(tmp);
        User savedUser = userRepository2.save(sender);
        return sender;
    }
}
