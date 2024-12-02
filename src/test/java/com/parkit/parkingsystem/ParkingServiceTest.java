package com.parkit.parkingsystem;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    // @Test
    // public void testProcessIncomingVehicle() {
    //     ArgumentCaptor<ParkingSpot> parkingSpotCaptor = ArgumentCaptor.forClass(ParkingSpot.class);
    //     ArgumentCaptor<Ticket> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
    //     ArgumentCaptor<String> vehicleCaptor = ArgumentCaptor.forClass(String.class);

    //     ParkingSpot availableParkingSpot = new ParkingSpot(2, ParkingType.CAR, true);
    //     int result = 1;
    //     when(inputReaderUtil.readSelection()).thenReturn(result);
    //     when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
    //     when(parkingService.getNextParkingNumberIfAvailable()).thenReturn(availableParkingSpot);

    //     when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);

    //     parkingService.processIncomingVehicle();

    //     verify(parkingSpotDAO, times(1)).updateParking(parkingSpotCaptor.capture()/* any(ParkingSpot.class) */);
    //     verify(ticketDAO, times(1)).saveTicket(ticketCaptor.capture()/* any(Ticket.class) */);
    //     verify(ticketDAO, times(1)).getNbTicket(vehicleCaptor.capture());

    //     ParkingSpot parkingSpot = parkingSpotCaptor.getValue();
    //     assertNotNull(parkingSpot, "Le parkingSpot ne doit pas être null");
    //     assertFalse(parkingSpot.isAvailable(), "La place ne doit plus être disponible");

    //     Ticket ticket = ticketCaptor.getValue();
    //     assertNotNull(ticket, "Le ticket ne doit pas être null");
    //     assertEquals("ABCDEF", vehicleCaptor.getValue(), "Le numéro du véhicule doit correspondre");
    //     assertEquals(0, ticket.getPrice(), 0, "Le prix doit être initialisé à 0");
    //     assertNotNull(ticket.getInTime(), "L'heure d'entrée doit être définie");
    //     assertNull(ticket.getOutTime(), "L'heure de sortie doit être null");
    // }

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
