package by.mrtorex.businessshark.server.config;

import by.mrtorex.businessshark.server.model.entities.*;
import lombok.Getter;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

@Getter
public class SessionConfig {
    private static SessionConfig sessionConfig;

    private final SessionFactory sessionFactory;

    private SessionConfig() {
        sessionFactory = new Configuration().
                addAnnotatedClass(User.class).
                addAnnotatedClass(Person.class).
                addAnnotatedClass(Role.class).
                addAnnotatedClass(Company.class).
                addAnnotatedClass(Stock.class).
                buildSessionFactory();
    }

    synchronized public static SessionConfig getInstance() {
        if (sessionConfig == null) {
            sessionConfig = new SessionConfig();
        }

        return sessionConfig;
    }
}

