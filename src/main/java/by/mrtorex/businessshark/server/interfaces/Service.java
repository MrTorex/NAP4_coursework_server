package by.mrtorex.businessshark.server.interfaces;

import java.util.List;

/**
 * Универсальный интерфейс для сервисов, работающих с сущностями.
 *
 * @param <T> тип сущности
 */
public interface Service<T> {

    /**
     * Находит сущность по её идентификатору.
     *
     * @param id идентификатор сущности
     * @return найденная сущность или null, если не найдена
     */
    T findEntity(int id);

    /**
     * Сохраняет новую сущность.
     *
     * @param entity сущность для сохранения
     */
    void saveEntity(T entity);

    /**
     * Удаляет сущность.
     *
     * @param entity сущность для удаления
     */
    void deleteEntity(T entity);

    /**
     * Обновляет данные сущности.
     *
     * @param entity сущность с обновлёнными данными
     */
    void updateEntity(T entity);

    /**
     * Возвращает список всех сущностей.
     *
     * @return список сущностей
     */
    List<T> findAllEntities();
}
