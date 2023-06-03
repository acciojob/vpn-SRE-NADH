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

    if(user.getConnected()){
        throw new Exception("Already connected");
    }
    if(user.getOriginalCountry().getCountryName().toString().equals(countryName.toUpperCase())){
        return user;
    }

    if(user.getServiceProviderList().isEmpty()){
        throw new Exception("Unable to connect");
    }

    List<ServiceProvider> serviceProviders = user.getServiceProviderList();
        ServiceProvider serviceProvidertmp=null;
        int lowestId = Integer.MAX_VALUE;
        Country countrytmp = null;
        for (ServiceProvider serviceProvider : serviceProviders){
            List<Country> countryList = serviceProvider.getCountryList();
            for (Country country1 : countryList){
                if (countryName.equalsIgnoreCase(country1.getCountryName().toString()) && lowestId>serviceProvider.getId()){
                    lowestId = serviceProvider.getId();
                    serviceProvidertmp = serviceProvider;
                    countrytmp = country1;
                }
            }
        }
//  ServiceProvider serviceProvidertmp=null;
//  Country countrytmp=null;
//    for(ServiceProvider serviceProvider:serviceProviders){
//        List<Country> countries = serviceProvider.getCountryList();
//        for(Country country:countries){
//            if(country.getCountryName().toString().equals(countryName.toUpperCase())){
//                serviceProvidertmp=serviceProvider;
//                countrytmp = country;
//                break;
//            }
//        }
//    }

     if(serviceProvidertmp==null){
         throw new Exception("Unable to connect");
     }

     Connection connection = new Connection();
     connection.setUser(user);

     user.setConnected(true);

     connection.setServiceProvider(serviceProvidertmp);
     user.getConnectionList().add(connection);

     serviceProvidertmp.getConnectionList().add(connection);

     String tmp = countrytmp.getCode()+"."+serviceProvidertmp.getId()+"."+user.getId();
    // System.out.println(tmp);
     user.setMaskedIp(tmp);
     User savedUser = userRepository2.save(user);
     return user;
    }
    @Override
    public User disconnect(int userId) throws Exception {
    User user = userRepository2.findById(userId).get();
    if(!user.getConnected()){
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
     if((!receiver.getConnected()) && (sender.getOriginalCountry().getCountryName().equals(receiver.getOriginalCountry().getCountryName()))){
         return sender;
     }
     Country recieverCountry=null;
     String arr[] = sender.getMaskedIp().split(".");
     ServiceProvider serviceProvider = serviceProviderRepository2.findById(Integer.parseInt(arr[1])).get();
     List<Country> countryList = serviceProvider.getCountryList();
     for(Country country:countryList){
         if(country.getCode().toString().equals(arr[0])){
             recieverCountry=country;
             break;
         }
     }

     if(recieverCountry.getCountryName().equals(sender.getOriginalCountry().getCountryName())){
         return sender;
     }
     try{
         sender = connect(senderId,recieverCountry.getCountryName().toString());
         return sender;
     }
     catch(Exception e) {
         throw new Exception("Cannot establish communication");
     }
    }
}
