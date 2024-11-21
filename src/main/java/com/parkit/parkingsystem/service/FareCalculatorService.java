package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inTimeMs =  ticket.getInTime().getTime();
        long outTimeMs =  ticket.getOutTime().getTime();

        double durationH =(double)(outTimeMs - inTimeMs)/3600000;

        if(durationH < 0.5){              //gratuit pour moins de 30 minutes
            ticket.setPrice(0);
        } else{
            double rate = discount ? (100-Fare.DISCOUNT_PCT)/100 : 1;
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    ticket.setPrice(durationH * Fare.CAR_RATE_PER_HOUR * rate);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(durationH * Fare.BIKE_RATE_PER_HOUR * rate);
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        }    
    }

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }
}