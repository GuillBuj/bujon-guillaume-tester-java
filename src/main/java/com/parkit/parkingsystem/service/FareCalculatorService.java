package com.parkit.parkingsystem.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
    Logger logger = LogManager.getLogger(FareCalculatorService.class);


    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException
                ("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inTimeMs = ticket.getInTime().getTime();
        long outTimeMs = ticket.getOutTime().getTime();
        double durationH = (double) (outTimeMs - inTimeMs) / 3600000;

        if (durationH < 0.5) { // free for less than half an hour
            ticket.setPrice(0);
        } else {
            double rate = discount ? (100 - Fare.DISCOUNT_PCT) / 100 : 1;
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    ticket.setPrice(durationH * Fare.CAR_RATE_PER_HOUR * rate);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(durationH * Fare.BIKE_RATE_PER_HOUR * rate);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }
            logger.debug("Price before rounding: " + ticket.getPrice());
            roundTicketPrice(ticket);
            logger.debug("Price after rounding: " + ticket.getPrice());
        }
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }

    private void roundTicketPrice(Ticket ticket) {
        double preRoundedPrice = Math.round(ticket.getPrice()*1000.0)/1000.0;
        logger.debug("Price after first rounding: " + preRoundedPrice);
        BigDecimal priceBigDecimal = BigDecimal.valueOf(preRoundedPrice).setScale(2, RoundingMode.HALF_DOWN);
        ticket.setPrice(priceBigDecimal.doubleValue());
    }
}