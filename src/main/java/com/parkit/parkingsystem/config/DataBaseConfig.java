package com.parkit.parkingsystem.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataBaseConfig {

    private static final Logger logger = LogManager.getLogger("DataBaseConfig");

    public Connection getConnection() throws ClassNotFoundException, SQLException {
        logger.info("Create DB connection");
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/prod","root","root");
    }

    // public Connection getConnection() throws ClassNotFoundException, SQLException {
    //     logger.info("***********************Tentative de connexion à la base de données...");
        
    //     try {
    //         // Charger le driver MySQL
    //         Class.forName("com.mysql.cj.jdbc.Driver");
    //         Connection connection = DriverManager.getConnection(
    //                 "jdbc:mysql://localhost:3306/prod?serverTimezone=Europe/Paris", "root", "root");
    
    //         // Vérification que la connexion est valide
    //         if (connection != null && connection.isValid(2)) {  // Timeout de validation de 2 secondes
    //             logger.info("***************************Connexion à la base de données réussie.");
    //         } else {
    //             logger.error("************************Échec de la connexion à la base de données.");
    //         }
    
    //         return connection;
    
    //     } catch (SQLException e) {
    //         // En cas d'erreur SQL, on log l'exception
    //         logger.error("**************************Erreur de connexion à la base de données: ", e);
    //         throw e;  // Propager l'exception pour que l'appelant puisse la gérer
    //     } catch (ClassNotFoundException e) {
    //         // Si le driver n'est pas trouvé, on log l'erreur
    //         logger.error("*************************Driver JDBC non trouvé : ", e);
    //         throw e;  // Propager l'exception
    //     }
    // }

    public void closeConnection(Connection con){
        if(con!=null){
            try {
                con.close();
                logger.info("Closing DB connection");
            } catch (SQLException e) {
                logger.error("Error while closing connection",e);
            }
        }
    }

    public void closePreparedStatement(PreparedStatement ps) {
        if(ps!=null){
            try {
                ps.close();
                logger.info("Closing Prepared Statement");
            } catch (SQLException e) {
                logger.error("Error while closing prepared statement",e);
            }
        }
    }

    public void closeResultSet(ResultSet rs) {
        if(rs!=null){
            try {
                rs.close();
                logger.info("Closing Result Set");
            } catch (SQLException e) {
                logger.error("Error while closing result set",e);
            }
        }
    }
}
