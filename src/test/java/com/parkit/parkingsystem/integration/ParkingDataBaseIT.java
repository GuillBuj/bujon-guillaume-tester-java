package com.parkit.parkingsystem.integration;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {
    private static final Logger logger = LogManager.getLogger("ParkingDatabaseIT");


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
        dataBasePrepareService.clearDataBaseEntries();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        int parkingSpotAvailableBefore = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);


        parkingService.processIncomingVehicle();

        Ticket ticket =ticketDAO.getTicket("ABCDEF");

        assertNotNull(ticket);
        assertEquals("ABCDEF", ticket.getVehicleRegNumber());
        assertNotNull(ticket.getInTime());
        assertNull(ticket.getOutTime());
        assertNotEquals(parkingSpotAvailableBefore, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();
        
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));
        ticket.setInTime(inTime);
        ticketDAO.updateTicket(ticket);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        
        parkingService.processExitingVehicle();

        //TODO: check that the fare generated and out time are populated correctly in the database
        ticket = ticketDAO.getTicket("ABCDEF");
        int parkingSpotNumber = ticket.getParkingSpot().getId();
        System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-entree: "+ ticket.getInTime()+ "sortie: "+ ticket.getOutTime() +", prix: " + ticket.getPrice());
        assertNotNull(ticket);
        assertNotNull(ticket.getOutTime());
        assertTrue(ticket.getOutTime().after(ticket.getInTime()));
        assertTrue(ticket.getPrice()>=0);
        assertTrue(parkingSpotDAO.isAvailable(parkingSpotNumber));
    }
}
