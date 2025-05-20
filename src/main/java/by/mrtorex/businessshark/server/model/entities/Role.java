package by.mrtorex.businessshark.server.model.entities;

import com.google.gson.annotations.Expose;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

/**
 * Сущность, представляющая роль пользователя или системы.
 * Используется для разграничения прав доступа и определения функций пользователя.
 */
@Entity
@Table(name = "Roles")
@Getter
@Setter
@ToString
public class Role {

    /**
     * Уникальный идентификатор роли.
     * Автоматически генерируется при сохранении в базу данных.
     */
    @Id
    @Expose
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Уникальное имя роли.
     * Не может быть null.
     */
    @Expose
    @Column(unique = true, nullable = false)
    private String name;

    /**
     * Пустой конструктор, необходимый для работы JPA.
     */
    public Role() {}

    /**
     * Конструктор с параметрами для удобства создания объектов.
     *
     * @param id   уникальный идентификатор роли
     * @param name уникальное имя роли
     */
    @SuppressWarnings("unused")
    public Role(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Переопределённый метод сравнения объектов на равенство.
     * Учитывает возможную прокси-обёртку Hibernate для корректного сравнения.
     *
     * @param o объект для сравнения
     * @return true, если объекты равны по типу и идентификатору, иначе false
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ?
                ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() :
                o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() :
                this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Role role = (Role) o;
        return getId() != null && Objects.equals(getId(), role.getId());
    }

    /**
     * Переопределённый метод генерации хеш-кода.
     * Учитывает прокси-обёртку Hibernate для корректности.
     *
     * @return хеш-код объекта
     */
    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ?
                ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() :
                getClass().hashCode();
    }
}
