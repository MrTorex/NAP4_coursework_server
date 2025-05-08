package by.mrtorex.businessshark.server.interfaces;

import java.util.List;

public interface Service<T> {
    T findEntity(int id);
    void saveEntity(T entity);
    void deleteEntity(T entity);
    void updateEntity(T entity);
    List<T> findAllEntities();
}
