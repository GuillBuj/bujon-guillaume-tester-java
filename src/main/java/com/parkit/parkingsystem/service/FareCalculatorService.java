package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inTimeMs =  ticket.getInTime().getTime();
        long outTimeMs =  ticket.getOutTime().getTime();

        double durationH =(double)(outTimeMs - inTimeMs)/3600000;

        if(durationH<0.5){
            ticket.setPrice(0);
        } else{
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    ticket.setPrice(durationH * Fare.CAR_RATE_PER_HOUR);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(durationH * Fare.BIKE_RATE_PER_HOUR);
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
        
    }
}