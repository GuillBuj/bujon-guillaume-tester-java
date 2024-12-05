package com.parkit.parkingsystem.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.DBConstants;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;

public class TicketDAO {

    private static final Logger logger = LogManager.getLogger("TicketDAO");

    public DataBaseConfig dataBaseConfig = new DataBaseConfig();

    public boolean saveTicket(Ticket ticket) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            try {
                con = dataBaseConfig.getConnection();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            // Préparer la requête SQL pour insérer un ticket
            ps = con.prepareStatement(DBConstants.SAVE_TICKET);
    
            // Remplir les paramètres de la requête
            ps.setInt(1, ticket.getParkingSpot().getId());
            ps.setString(2, ticket.getVehicleRegNumber());
            ps.setDouble(3, ticket.getPrice());
            ps.setTimestamp(4, new Timestamp(ticket.getInTime().getTime()));
            ps.setTimestamp(5, (ticket.getOutTime() == null) ? null : new Timestamp(ticket.getOutTime().getTime()));
    
            // Exécuter la requête et obtenir le nombre de lignes affectées
            int rowsAffected = ps.executeUpdate();
           // Si une ligne a été insérée, on retourne true, sinon false
            return rowsAffected == 1;
        } catch (SQLException ex) {
            logger.error("Error saving ticket to the database", ex);
            return false;
        } finally {
            // Toujours fermer les ressources
            try {
                if (ps != null) {
                    ps.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                logger.error("Error closing resources", ex);
            }
        }
    }

    @SuppressWarnings("finally")
    public Ticket getTicket(String vehicleRegNumber) {
        Connection con = null;
        Ticket ticket = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.GET_TICKET);
            //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
            ps.setString(1,vehicleRegNumber);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                ticket = new Ticket();
                ParkingSpot parkingSpot = new ParkingSpot(rs.getInt(1), ParkingType.valueOf(rs.getString(6)),false);
                ticket.setParkingSpot(parkingSpot);
                ticket.setId(rs.getInt(2));
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(rs.getDouble(3));
                ticket.setInTime(rs.getTimestamp(4));
                ticket.setOutTime(rs.getTimestamp(5));
            }
            dataBaseConfig.closeResultSet(rs);
            dataBaseConfig.closePreparedStatement(ps);
        }catch (Exception ex){
            logger.error("Error fetching next available slot",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
            return ticket;
        }
    }

    public boolean updateTicket(Ticket ticket) {
        Connection con = null;
        try {
            con = dataBaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(DBConstants.UPDATE_TICKET);
            ps.setDouble(1, ticket.getPrice());
            if (ticket.getOutTime() != null) {
                ps.setTimestamp(2, new Timestamp(ticket.getOutTime().getTime()));
            } else {
                ps.setNull(2, java.sql.Types.TIMESTAMP);
            }
            ps.setTimestamp(3, new Timestamp(ticket.getInTime().getTime()));
            ps.setInt(4,ticket.getId());
            ps.executeUpdate();
            return true;
        }catch (Exception ex){
            logger.error("Error saving ticket info",ex);
        }finally {
            dataBaseConfig.closeConnection(con);
        }
        return false;
    }

    public int getNbTicket(String vehicleRegNumber){
        int nb=0;

        try (Connection con = dataBaseConfig.getConnection()) {
                PreparedStatement ps = con.prepareStatement(DBConstants.GET_NB_TICKET);
                ps.setString(1, vehicleRegNumber);
                try (ResultSet rs=ps.executeQuery()){   //verif si il y a une ligne
                    if (rs.next())
                        {nb=rs.getInt(1);}
                }
            } catch (ClassNotFoundException | SQLException ex) {
                logger.error("Error fetching ticket count for vehicle " + vehicleRegNumber, ex);
            }
            
        return nb;
    }
}
