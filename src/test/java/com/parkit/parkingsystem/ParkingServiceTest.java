package com.parkit.parkingsystem;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {
    @InjectMocks
    private ParkingService parkingService;

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(2, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void testProcessIncomingVehicle() {
        ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
        ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
        ArgumentCaptor<String> vehicleCaptor = ArgumentCaptor.forClass(String.class);

        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);

        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO, times(1)).updateParking(parkingSpotCaptor.capture());
        verify(ticketDAO, times(1)).saveTicket(ticketCaptor.capture());
        verify(ticketDAO, times(1)).getNbTicket(vehicleCaptor.capture());

        ParkingSpot parkingSpot = parkingSpotCaptor.getValue();
        assertNotNull(parkingSpot);
        assertFalse(parkingSpot.isAvailable());

        Ticket ticket = ticketCaptor.getValue();
        assertNotNull(ticket);
        assertEquals("ABCDEF", vehicleCaptor.getValue());
        assertEquals(0, ticket.getPrice());
        assertNotNull(ticket.getInTime());
        assertNull(ticket.getOutTime());
    }

    @Test
    public void testGetNextParkingNumberIfAvailable(){
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        
        ParkingSpot availableParkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertNotNull(availableParkingSpot);
        assertEquals(1,availableParkingSpot.getId());
        assertTrue(availableParkingSpot.isAvailable());
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
        when(inputReaderUtil.readSelection()).thenReturn(1);

        ParkingSpot availableParkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertNull(availableParkingSpot);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument(){
        when(inputReaderUtil.readSelection()).thenReturn(3);
        
        ParkingSpot availableParkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertNull(availableParkingSpot);
    }

    @Test
    public void processExitingVehicleTestUnableUpdate(){
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void processExitingVehicleTest() {
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(0);

        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).getTicket("ABCDEF");
        verify(ticketDAO, times(1)).getNbTicket("ABCDEF");
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
        // pas de verify sur fareCalculatorService.calculateFare(ticket, notFirstTime)
        // car testé ailleurs
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket.getOutTime(), "La date de sortie doit avoir été définie");
        assertTrue(ticket.getPrice() > 0, "Le prix ne doit plus être de 0");
    }
    
}
