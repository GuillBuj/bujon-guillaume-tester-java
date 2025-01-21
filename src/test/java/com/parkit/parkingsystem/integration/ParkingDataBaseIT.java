package com.parkit.parkingsystem.integration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {
   
    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        int parkingSpotAvailableBefore = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);


        parkingService.processIncomingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");

        assertNotNull(ticket);
        assertEquals("ABCDEF", ticket.getVehicleRegNumber());
        assertNotNull(ticket.getInTime());
        assertNull(ticket.getOutTime());
        assertNotEquals(parkingSpotAvailableBefore, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
    }

    @Test
    public void testParkingLotExit(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        
        Ticket ticketIn = ticketDAO.getTicket("ABCDEF");
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        ticketIn.setInTime(inTime);
        ticketDAO.updateTicket(ticketIn);
        
        parkingService.processExitingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        int parkingSpotNumber = ticket.getParkingSpot().getId();
        assertNotNull(ticket);
        assertNotNull(ticket.getOutTime());
        assertTrue(ticket.getOutTime().after(ticket.getInTime()));
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
        assertTrue(parkingSpotDAO.isAvailable(parkingSpotNumber));
    }

    @Test
    public void testParkingLotExitRecurringUser(){
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        
        Ticket previousTicket = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        previousTicket.setParkingSpot(parkingSpot);
        previousTicket.setVehicleRegNumber("ABCDEF");
        previousTicket.setPrice(2);
        previousTicket.setInTime(new Date(System.currentTimeMillis() - (180 * 60 * 1000)));
        previousTicket.setOutTime(new Date(System.currentTimeMillis() - (90 * 60 * 1000)));
        ticketDAO.saveTicket(previousTicket);

        parkingService.processIncomingVehicle();
        
        Ticket ticketIn = ticketDAO.getTicket("ABCDEF");
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        ticketIn.setInTime(inTime);
        ticketDAO.updateTicket(ticketIn);
        
        parkingService.processExitingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        int parkingSpotNumber = ticket.getParkingSpot().getId();
        assertNotNull(ticket);
        assertNotNull(ticket.getOutTime());
        assertTrue(ticket.getOutTime().after(ticket.getInTime()));
        BigDecimal priceBigDecimal = BigDecimal.valueOf(((Fare.CAR_RATE_PER_HOUR * (100 - Fare.DISCOUNT_PCT))/100)).setScale(2,RoundingMode.HALF_DOWN);
        assertEquals(priceBigDecimal, BigDecimal.valueOf(ticket.getPrice()));
        assertTrue(parkingSpotDAO.isAvailable(parkingSpotNumber));
    }
}
